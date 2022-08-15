package Netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.util.concurrent.Callable;

public class SendTask implements Callable<Boolean> {

    private final Channel channel;
    private final ByteBuf content;

    public SendTask(Channel channel, ByteBuf content) {
        this.channel = channel;
        this.content = content;
    }

    @Override
    public Boolean call() throws Exception {
        if (channel.isActive()) {
            if (channel.isWritable()) {
                channel.writeAndFlush(content);
                return true;
            }
        }
        return false;
    }
}
