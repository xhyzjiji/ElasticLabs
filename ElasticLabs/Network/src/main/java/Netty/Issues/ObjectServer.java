package Netty.Issues;

import Netty.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ObjectServer implements Peer {

    private Logger logger = LoggerFactory.getLogger(ObjectServer.class);

    private AtomicBoolean running = new AtomicBoolean(false);
    private ServerBootstrap serverBootstrap;
    private Channel serverChannel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup ioGroup;
    private static EventLoopGroup registrationGroup;
    private static EventLoopGroup messageGroup;

    private Map<String, Channel> clientChannels = new HashMap<>();

    static {
        registrationGroup = new NioEventLoopGroup(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "server_registration");
                t.setDaemon(true);
                return t;
            }
        });
        messageGroup = new NioEventLoopGroup(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "server_message");
                t.setDaemon(true);
                return t;
            }
        });
    }

    public void start() {
        if (running.compareAndSet(false, true)) {
            logger.info("server start...");
            this.bossGroup = new NioEventLoopGroup(1, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r,"server-boss");
                }
            });
            this.ioGroup = new NioEventLoopGroup(1/*Math.max(2, Runtime.getRuntime().availableProcessors() * 2 + 1)*/, new ThreadFactory() {
                private final AtomicInteger IO_GROUP_ID_GENERATOR = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r,"server-io" + IO_GROUP_ID_GENERATOR.getAndIncrement());
                }
            });

            this.serverBootstrap = new ServerBootstrap();
            this.serverBootstrap.group(bossGroup, ioGroup)
                    .channel(NioServerSocketChannel .class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
//                            pipeline.addFirst("idleStateHandler", new IdleStateHandler(0, 0, CHANNEL_IDLE_TIMEOUT/*second*/));
//                            pipeline.addAfter("idleStateHandler", "idleTimeoutHandler", new ChannelIdleTimeoutHandler());
                            pipeline.addLast("containerStatHandler", new ConnectionHandler());
//                            pipeline.addLast("frameDecoder", new DelimiterBasedFrameDecoder(128 * 1024, Delimiters.lineDelimiter()));
                            pipeline.addLast("objectEncoder", new ObjectEncoder());
                            pipeline.addLast("objectDecoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null/*this.getClass().getClassLoader()*/)));
                            pipeline.addLast(registrationGroup, "registrationHandler", new RegistrationObjectHandler(ObjectServer.this));
                            pipeline.addLast(messageGroup, "requestHandler", new MessageObjectHandler(ObjectServer.this));
                            pipeline.addLast("exceptionHandler", new ExceptionHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, ContainerConstants.SO_BACKLOG)
                    .childOption(ChannelOption.SO_REUSEADDR, ContainerConstants.SO_REUSEADDR)
                    .childOption(ChannelOption.TCP_NODELAY, ContainerConstants.TCP_NODELAY)
                    .childOption(ChannelOption.SO_KEEPALIVE, ContainerConstants.SO_KEEPALIVE)
                    .childOption(ChannelOption.SO_SNDBUF, 10 * 1024)
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(100 * 1024, 1000 * 1024))
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            try {
                ChannelFuture future = this.serverBootstrap.bind(8888);
                // wait for setup
                future.sync();
                this.serverChannel = future.channel();
                logger.info("server start successfully, server channel id={}", serverChannel.id());
            } catch (InterruptedException ie) {
                logger.error("An interruptedException was caught while initializing server", ie);
                Thread.currentThread().interrupt();
            } catch (Throwable te) {
                logger.error("A throwable was caught while intializing server", te);
            }

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    registrationGroup.shutdownGracefully();
                    messageGroup.shutdownGracefully();
                }
            });
        }
    }

    public void stop() {
        if (running.compareAndSet(true, false)) {
            logger.info("server stop...");
            this.serverChannel.close().awaitUninterruptibly();

            this.bossGroup.shutdownGracefully();
            this.ioGroup.shutdownGracefully();
//            registrationGroup.shutdownGracefully();
//            messageGroup.shutdownGracefully();
            logger.info("server stop successfully");
        }
    }

    public void send(String clientId, MessageObject messageObject) throws Exception {
        Channel channel = clientChannels.get(clientId);
        boolean sent = false;
        while (!sent) {
            if (!channel.isWritable()) {
                Thread.sleep(100);
            } else {
                channel.writeAndFlush(messageObject);
                sent = true;
            }
        }
    }

    public void registClient(String clientId, Channel channel) {
        clientChannels.put(clientId, channel);
    }

}
