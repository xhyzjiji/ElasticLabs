package Netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static Netty.ContainerConstants.CHANNEL_IDLE_TIMEOUT;

public class Server implements Peer {

    private Logger logger = LoggerFactory.getLogger(Server.class);

    private final ContainerConstants.Role role = ContainerConstants.Role.SERVER;

    private AtomicBoolean running = new AtomicBoolean(false);
    protected ChannelGroup channelGroup;
    protected Set<ChannelId> channelIds = new HashSet<>();
    protected BlockingQueue<SendTask> bq = new ArrayBlockingQueue<>(100);
    private ServerBootstrap serverBootstrap;
    private Channel serverChannel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup ioGroup;

    public void start() {
        if (running.compareAndSet(false, true)) {
            logger.info("server start...");
            this.bossGroup = new NioEventLoopGroup(1, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r,"server-boss");
                }
            });
            this.ioGroup = new NioEventLoopGroup(Math.max(2, Runtime.getRuntime().availableProcessors() * 2 + 1), new ThreadFactory() {
                private final AtomicInteger IO_GROUP_ID_GENERATOR = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r,"server-io" + IO_GROUP_ID_GENERATOR.getAndIncrement());
                }
            });

            this.channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            this.serverBootstrap = new ServerBootstrap();
            this.serverBootstrap.group(bossGroup, ioGroup)
                    .channel(NioServerSocketChannel .class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();

                            pipeline.addFirst("idleStateHandler", new IdleStateHandler(0, 0, CHANNEL_IDLE_TIMEOUT/*second*/));
                            pipeline.addAfter("idleStateHandler", "idleTimeoutHandler", new ChannelIdleTimeoutHandler());
//                            pipeline.addLast("containerStatHandler", parent.getContainerStatHandler());
                            pipeline.addLast("connectionHandler", new ConnectionHandler(Server.this.channelGroup, Server.this.channelIds));
                            pipeline.addLast("frameDecoder", new DelimiterBasedFrameDecoder(128 * 1024, Delimiters.lineDelimiter()));
                            pipeline.addLast("requestHandler", new RequestHandler(Server.this));
                            pipeline.addLast("exceptionHandler", new ExceptionHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, ContainerConstants.SO_BACKLOG)
                    .childOption(ChannelOption.SO_REUSEADDR, ContainerConstants.SO_REUSEADDR)
                    .childOption(ChannelOption.TCP_NODELAY, ContainerConstants.TCP_NODELAY)
                    .childOption(ChannelOption.SO_KEEPALIVE, ContainerConstants.SO_KEEPALIVE)
                    .childOption(ChannelOption.SO_SNDBUF, 5 * 1024)
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(1024, 2048))
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

            new Thread() {
                @Override
                public void run() {
                    while (running.get()) {
                        BlockingQueue<SendTask> sendTasks = Server.this.bq;
                        SendTask st = sendTasks.peek();
                        if (Objects.nonNull(st)) {
                            try {
                                if (st.call()) {
                                    sendTasks.poll();
                                }
                            } catch (Exception e) {
                                logger.error("Exec SendTask error", e);
                            }
                        } else {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ie) {

                            }
                        }
                    }
                }
            }.start();
        }
    }

    public void stop() {
        if (running.compareAndSet(true, false)) {
            logger.info("server stop...");
            this.serverChannel.close().awaitUninterruptibly();
            // recycle session in addListener or channelInactive event
            int channelNum = channelGroup.size();
            this.channelGroup.close().awaitUninterruptibly();
            logger.info("{} children channels are all closed!", channelNum);

            this.bossGroup.shutdownGracefully();
            this.ioGroup.shutdownGracefully();
            logger.info("server stop successfully");
        }
    }

    public void sendAll(String line) {
        channelGroup.writeAndFlush(Unpooled.wrappedBuffer(line.getBytes()));
    }

}
