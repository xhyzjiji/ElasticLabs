package Netty.Issues;

import Netty.ContainerConstants;
import Netty.Peer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ObjectClient implements Peer {
    private static final AtomicInteger CLINET_ID_GENER = new AtomicInteger(1);
    private Logger logger = LoggerFactory.getLogger(ObjectClient.class);

    private ContainerConstants.Role role = ContainerConstants.Role.CLIENT;

    private AtomicBoolean running = new AtomicBoolean(false);
    private final String clientId = "Client-" + CLINET_ID_GENER.getAndIncrement();
    private Bootstrap bootstrap;
    private EventLoopGroup ioGroup;
    private static EventLoopGroup handlerGroup;
    private Channel clientChannel;

    public String getClientId() {
        return clientId;
    }

    public Channel getClientChannel() {
        return clientChannel;
    }

    static {
        handlerGroup = new NioEventLoopGroup(3, new ThreadFactory() {
            private final AtomicInteger ID = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "client_async_executor-" + ID.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        });
    }

    public void start() {
        if (running.compareAndSet(false, true)) {
            logger.info("{} start...", clientId);
            this.ioGroup = new NioEventLoopGroup(4, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, clientId);
                }
            }); // 多个channel可以复用一个group，内部轮询均摊channel
            this.bootstrap = new Bootstrap();

            bootstrap.group(ioGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_RCVBUF, 10 * 1024)
                    .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(100 * 1024, 1000 * 1024))
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.SINGLE_EVENTEXECUTOR_PER_GROUP, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
//                                .addLast(new IdleStateHandler(0, 0, CHANNEL_IDLE_TIMEOUT))
//                                .addLast(new ChannelIdleTimeoutHandler())
//                                .addLast(new DelimiterBasedFrameDecoder(5 * 1024, Delimiters.lineDelimiter()))
//                                    .addLast("connection handler", new ConnectionHandler())
                                    .addLast("client encoder", new ObjectEncoder())
                                    .addLast("client decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)))
                                    .addLast(handlerGroup, "client handler", new MessageObjectHandler(ObjectClient.this));
                        }
                    });
            try {
                this.clientChannel = bootstrap.connect("localhost", 8888).sync().channel();
//                this.clientChannel.config().setAutoRead(true);
                logger.info("{} start successfully, client channel id={}", clientId, clientChannel.id());
            } catch (Exception e) {
                logger.info("{} start error", clientId, e);
                ioGroup.shutdownGracefully();
                running.set(false);
                throw new RuntimeException(e);
            }

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    handlerGroup.shutdownGracefully();
                }
            });
        }
    }

    public void stop() {
        if (running.compareAndSet(true, false)) {
            logger.info("{} stop...", clientId);
            clientChannel.close();
            ioGroup.shutdownGracefully();
//            handlerGroup.shutdownGracefully();
            logger.info("{} stop successfully", clientId);
        }
    }

    public void regist() {
        RegistrationObject registrationObject = new RegistrationObject();
        registrationObject.setId(clientId);
        clientChannel.writeAndFlush(registrationObject);
    }

    public void send(MessageObject messageObject) {
        clientChannel.writeAndFlush(messageObject);
    }
}
