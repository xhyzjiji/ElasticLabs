package Netty;

import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Set;

@ChannelHandler.Sharable
public class ConnectionHandler extends ChannelDuplexHandler {

	private Logger logger = LoggerFactory.getLogger(ConnectionHandler.class);

	private static final Logger LOG = LoggerFactory.getLogger(ConnectionHandler.class);

	private final ChannelGroup channelGroup;
	private final Set<ChannelId> channelIds;

	public ConnectionHandler(ChannelGroup channelGroup, Set<ChannelId> channelIds) {
		this.channelGroup = channelGroup;
		this.channelIds = channelIds;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		channelGroup.add(ctx.channel());
		channelIds.add(ctx.channel().id());
		ctx.fireChannelActive();
		logger.info("server get client {}", ctx.channel().id());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Channel channel = ctx.channel();
		InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
		String ip = socketAddress.getAddress().getHostAddress();
		LOG.warn("channel {}, ip {} has no clientId, close normally", channel.id().asShortText(), ip);

		channelGroup.remove(ctx.channel());
		channelIds.remove(ctx.channel().id());
		ctx.fireChannelInactive();
		logger.info("server say bye to client {}", ctx.channel().id());
	}

	@Override
	public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        Channel channel = ctx.channel();
		LOG.info("client {} disconnect from server", channel.id().asShortText());
		ctx.disconnect(promise);
	}

	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        Channel channel = ctx.channel();
		LOG.info("server forwardly close client {}", channel.id().asShortText());
		ctx.close(promise);
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		InetSocketAddress socketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
		String ip = socketAddress.getAddress().getHostAddress();
		LOG.debug("client {} ip={} connect!", ctx.channel().id().asShortText(), ip);
		ctx.fireChannelRegistered();
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		InetSocketAddress socketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
		String ip = socketAddress.getAddress().getHostAddress();
		LOG.debug("client {} ip={} leave!", ctx.channel().id().asShortText(), ip);
		ctx.fireChannelUnregistered();
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		LOG.info("channel {} writable changed: {}", ctx.channel().id().asShortText(), ctx.channel().isWritable());
	}

}
