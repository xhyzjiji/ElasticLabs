package Netty.Issues;

import Netty.Peer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class MessageObjectHandler extends SimpleChannelInboundHandler<MessageObject> {

    private static final Logger log = LoggerFactory.getLogger(MessageObjectHandler.class);

    private final Peer peerType;
    private final Random random = new Random();

    public MessageObjectHandler(Peer peerType) {
        this.peerType = peerType;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageObject msg) throws Exception {
        if (peerType instanceof ObjectServer) {
            log.info("server receive bytes: {}", msg.getContent().length);
        } else if (peerType instanceof ObjectClient) {
//            Thread.sleep( random.nextInt(2000));
            log.info("client {} receive bytes: {}", ((ObjectClient) peerType).getClientId(), msg.getContent().length);
            if (msg.isLast()) {
                IssueDemoMain.lock.lock();
                IssueDemoMain.condition.signal();
                IssueDemoMain.lock.unlock();
            }
        } else {
            log.info("unknown type receive bytes:{}", msg.getContent().length);
        }
    }
}
