package Netty.Issues;

import Netty.Peer;
import Serializer.JSONUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistrationObjectHandler extends SimpleChannelInboundHandler<RegistrationObject> {

    private static final Logger log = LoggerFactory.getLogger(RegistrationObjectHandler.class);

    private final Peer peerType;

    public RegistrationObjectHandler(Peer peerType) {
        this.peerType = peerType;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RegistrationObject msg) throws Exception {
        if (peerType instanceof ObjectServer) {
            ((ObjectServer) peerType).registClient(msg.getId(), ctx.channel());
            log.info("server recieve registrationObject: {}", JSONUtil.toJsonStringSilent(msg, false));
        } else {
            log.info("ignore registrationObject: {} because I am not a server", JSONUtil.toJsonStringSilent(msg, false));
        }
    }
}
