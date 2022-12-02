package Netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;

public class NettyUtil {

	public static byte[] bytebufToByteArray(ByteBuf byteBuf, boolean shouldChangeReaderIndex) {
		byte[] ans = new byte[byteBuf.readableBytes()];

		if (shouldChangeReaderIndex) {
			byteBuf.readBytes(ans);
		} else {
			int arrayOffset = byteBuf.arrayOffset();
			byteBuf.getBytes(arrayOffset, ans);
		}
		return ans;
	}

	public static <T> void setChannelAttribute(Channel channel, String key, T value) {
		AttributeKey<T> attrKey = AttributeKey.valueOf(key);
		Attribute<T> attrValue = channel.attr(attrKey);
		attrValue.set(value);
	}

	public static <T> T getChannelAttribute(Channel channel, String key) {
		AttributeKey<T> attrKey = AttributeKey.valueOf(key);
		if (channel.hasAttr(attrKey)) {
			return channel.attr(attrKey).get();
		} else {
			return null;
		}
	}

	public static String getChannelIP(Channel channel) {
		try {
			InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
			String ip = socketAddress.getAddress().getHostAddress();
			return ip;
		} catch (Exception e) {
			return "0.0.0.0";
		}
	}

	public static ChannelFuture sendMsg(Channel channel, String msg) {
		final ByteBuf bb = Unpooled.wrappedBuffer(msg.getBytes());
		return channel.writeAndFlush(bb);
	}

}
