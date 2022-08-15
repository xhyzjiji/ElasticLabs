package Netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static Netty.ContainerConstants.CHANNEL_IDLE_TIMEOUT;

public class Client implements Peer {
    private static final AtomicInteger CLINET_ID_GENER = new AtomicInteger(1);
    private Logger logger = LoggerFactory.getLogger(Client.class);

    private ContainerConstants.Role role = ContainerConstants.Role.CLIENT;

    private AtomicBoolean running = new AtomicBoolean(false);
    private String clientId = "Client-" + CLINET_ID_GENER.getAndIncrement();
    private Bootstrap bootstrap;
    private EventLoopGroup ioGroup;
    private Channel clientChannel;

    public void start() {
        if (running.compareAndSet(false, true)) {
            logger.info("{} start...", clientId);
            this.ioGroup = new NioEventLoopGroup(new ThreadFactory() {
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
                .option(ChannelOption.SO_RCVBUF, 4 * 1024)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline()
                                .addLast(new IdleStateHandler(0, 0, CHANNEL_IDLE_TIMEOUT))
                                .addLast(new ChannelIdleTimeoutHandler())
//                                .addLast(new DelimiterBasedFrameDecoder(5 * 1024, Delimiters.lineDelimiter()))
                                .addLast(new RequestHandler(Client.this));
                    }
                });
            try {
                this.clientChannel = bootstrap.connect("localhost", 8888).sync().channel();
                logger.info("{} start successfully, client channel id={}", clientId, clientChannel.id());
            } catch (Exception e) {
                logger.info("{} start error", clientId, e);
                ioGroup.shutdownGracefully();
                running.set(false);
                throw new RuntimeException(e);
            }
        }
    }

    public void stop() {
        if (running.compareAndSet(true, false)) {
            logger.info("{} stop...", clientId);
            clientChannel.close();
            ioGroup.shutdownGracefully();
            logger.info("{} stop successfully", clientId);
        }
    }

    public void send(String line) {
        clientChannel.writeAndFlush(Unpooled.wrappedBuffer(line.getBytes()));
    }
}
