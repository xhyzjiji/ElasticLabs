package Netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class RequestHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final Peer peer;
    private Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    public RequestHandler(Peer peer) {
        this.peer = peer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
//        if (peer instanceof Client) {
//            Thread.sleep(10_000);
//        }

        String line = "";
        if (byteBuf.hasArray()) {
            line = new String(byteBuf.array());
        } else {
            int len = byteBuf.readableBytes();
            byte[] byteArray = new byte[len];
            byteBuf.getBytes(byteBuf.readerIndex(), byteArray, 0, len);
            line = new String(byteArray);
        }
        logger.info("receive: {}", line);

        final String content = line + "\n";
        final byte[] contentBytes = content.getBytes();
        final ByteBuf contentBuf = Unpooled.wrappedBuffer(contentBytes);
        if (peer instanceof Server) {
            Server server = (Server)peer;
            ChannelId cid = channelHandlerContext.channel().id();
            Iterator<Channel> iterator = server.channelGroup.iterator();
            while (iterator.hasNext()) {
                Channel channel = iterator.next();
                if (cid.equals(channel.id()) == false) {
                    contentBuf.retain();
                    try {
                        if (channel.isWritable() == false) {
                            logger.error("channel {} is unwritable now, gen sendTask", channel.id());
                            server.bq.add(new SendTask(channel, contentBuf));
                        } else {
                            logger.info("server pass msg to client channel {}, payload size {}Bytes", channel.id(), contentBytes.length);
                            channel.writeAndFlush(contentBuf);
                        }
                    } finally {
                        ReferenceCountUtil.release(contentBuf);
                    }
                }
            }
        }
    }
}
