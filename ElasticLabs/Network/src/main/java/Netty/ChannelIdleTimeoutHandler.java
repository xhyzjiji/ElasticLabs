package Netty;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class ChannelIdleTimeoutHandler extends ChannelDuplexHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ChannelIdleTimeoutHandler.class);

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleState e = ((IdleStateEvent) evt).state();
			if (e == IdleState.ALL_IDLE) {
				LOG.info("channel {} rw idle timeout",
						 (String)NettyUtil.getChannelAttribute(ctx.channel(), ContainerConstants.ATTR_CLIENTID));
				ctx.close();
			}
		} else {
			LOG.info("channel {} trig event {}", ctx.channel().attr(AttributeKey.valueOf(ContainerConstants.ATTR_CLIENTID)),
					evt.getClass().getName());
		}
		super.userEventTriggered(ctx, evt);
	}

}
