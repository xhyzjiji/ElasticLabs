package Netty.Issues;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public class MessageObject implements Serializable {

    private static final AtomicLong MSG_ID = new AtomicLong(0);
    private static final long serialVersionUID = -6512930261617957705L;
    private long msgId;
    private byte[] content;
    private boolean isLast;

    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }

    public static MessageObject newMessageObject(int size, boolean isLast) {
        MessageObject res = new MessageObject();
        res.setMsgId(MSG_ID.getAndIncrement());
        res.setContent(new byte[size]);
        res.setLast(isLast);
        return res;
    }
}
