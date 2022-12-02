package Netty;

import Netty.LabTool.DeadLockSimulator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

public class RequestHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final Peer peer;
    private Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private final Map<String, Processor> processorMap;

    public interface Processor {
        void process(ChannelHandlerContext channelHandlerContext, String cmdVal);
    }

    public RequestHandler(Peer peer) {
        this.peer = peer;
        this.processorMap = new HashMap<>();
        if (peer instanceof Server) {
            Server server = (Server)peer;
            processorMap.put("register", (channelHandlerContext, cmdVal) -> NettyUtil.setChannelAttribute(channelHandlerContext.channel(), ContainerConstants.ATTR_CLIENTID, cmdVal));
            processorMap.put("send", (channelHandlerContext, cmdVal) -> {
                int subCmdDelimiterIndex = cmdVal.indexOf(":");
                if (subCmdDelimiterIndex > 0) {
                    String subCmd = cmdVal.substring(0, subCmdDelimiterIndex);
                    String subCmdVal = cmdVal.substring(subCmdDelimiterIndex + 1);
                    Iterator<Channel> channelIterator = server.channelGroup.iterator();
                    while (channelIterator.hasNext()) {
                        Channel c = channelIterator.next();
                        if (StringUtils.equals(NettyUtil.getChannelAttribute(c, ContainerConstants.ATTR_CLIENTID), subCmd)) {
                            NettyUtil.sendMsg(c, subCmdVal + "\n");
                        }
                    }
                }
            });
            processorMap.put("broadcast", (channelHandlerContext, cmdVal) -> {
                Iterator<Channel> channelIterator = server.channelGroup.iterator();
                while (channelIterator.hasNext()) {
                    Channel c = channelIterator.next();
                    NettyUtil.sendMsg(c, cmdVal + "\n");
                }
            });
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        String line = "";
        if (byteBuf.hasArray()) {
            line = new String(byteBuf.array());
        } else {
            int len = byteBuf.readableBytes();
            byte[] byteArray = new byte[len];
            byteBuf.getBytes(byteBuf.readerIndex(), byteArray, 0, len);
            line = new String(byteArray);
        }

        if (peer instanceof Server) {
            logger.info("server receive: {}", line);
            int commandDelimiterIndex = line.indexOf(":");
            if (commandDelimiterIndex > 0) {
                String command = line.substring(0, commandDelimiterIndex);
                String content = line.substring(commandDelimiterIndex + 1);
                switch (command) {
                    case "register":
                        processorMap.get("register").process(channelHandlerContext, content);
                        break;
                    case "send":
                        processorMap.get("send").process(channelHandlerContext, content);
                        break;
                    case "broadcast":
                        processorMap.get("broadcast").process(channelHandlerContext, content);
                        break;
                    default:
                        channelHandlerContext.channel().writeAndFlush(line);
                        logger.info("command={}, content={}", command, content);
                        break;
                }
            } else {
                channelHandlerContext.channel().writeAndFlush(line);
            }
        } else {
            final String lineContent = line;
            DeadLockSimulator.triggerDeadLock(() -> {
                Client client = (Client)peer;
                logger.info("client {} receive: {}", client.getClientId(), lineContent);
                return null;
            });
        }
    }
}
