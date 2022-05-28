package BehaviorPattern;

import Serializer.JSONUtil;
import java.util.concurrent.atomic.AtomicInteger;

public class ResponsibilityChainPattern {

    interface Handler<E> {
        boolean isAllow(E data);
        void handle(E data) throws Exception;
        boolean needFireNext();
    }

    public static class MyData {
        private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);
        private int id;
        private String msg;

        public MyData(String msg) {
            this.id = ID_GENERATOR.getAndIncrement();
            this.msg = msg;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }

    public static class OddHandler implements Handler<MyData> {
        @Override public boolean isAllow(MyData data) {
            return (data.id & 0x01) != 0;
        }

        @Override public void handle(MyData data) throws Exception {
            System.out.println("Odd Handler receive: " + JSONUtil.toJsonStringSilent(data, false));
        }

        @Override public boolean needFireNext() {
            return true;
        }
    }

    public static class EvenHandler implements Handler<MyData> {
        @Override public boolean isAllow(MyData data) {
            return (data.id & 0x01) == 0;
        }

        @Override public void handle(MyData data) throws Exception {
            System.out.println("Even Handler receive: " + JSONUtil.toJsonStringSilent(data, false));
        }

        @Override public boolean needFireNext() {
            return false;
        }
    }

    public static class NopHandler implements Handler {
        @Override public boolean isAllow(Object data) {
            return false;
        }

        @Override public void handle(Object data) throws Exception {
            // do nothing
        }

        @Override public boolean needFireNext() {
            return true;
        }
    }

    public static class HandlerNode {
        private Handler handler;
        private HandlerNode prev;
        private HandlerNode next;

        public Handler getHandler() {
            return handler;
        }

        public void setHandler(Handler handler) {
            this.handler = handler;
        }

        public HandlerNode getPrev() {
            return prev;
        }

        public void setPrev(HandlerNode prev) {
            this.prev = prev;
        }

        public HandlerNode getNext() {
            return next;
        }

        public void setNext(HandlerNode next) {
            this.next = next;
        }
    }
    public static class HandlerPipeline {
        private final HandlerNode head;
        private final HandlerNode tail;

        public HandlerPipeline() {
            this.head = new HandlerNode();
            this.tail = new HandlerNode();
            this.head.next = this.tail;
            this.head.prev = null;
            this.tail.prev = this.head;
            this.tail.next = null;
            this.head.handler = new NopHandler();
            this.tail.handler = new NopHandler();
        }

        public void addFirst(Handler handler) {
            HandlerNode newNode = new HandlerNode();
            newNode.handler = handler;
            HandlerNode temp = this.head.next;
            synchronized (this) {
                this.head.next = newNode;
                newNode.next = temp;
                newNode.prev = this.head;
                temp.prev = newNode;
            }
        }

        public synchronized void addLast(Handler handler) {
            HandlerNode newNode = new HandlerNode();
            newNode.handler = handler;
            HandlerNode temp = this.tail.prev;
            synchronized (this) {
                temp.next = newNode;
                newNode.next = this.tail;
                newNode.prev = temp;
                this.tail.prev = newNode;
            }
        }

        public void fireHeadToTail(MyData data) throws Exception {
            HandlerNode handlerNode = this.head;
            while (handlerNode != null) {
                Handler handler = handlerNode.handler;
                if (handler.isAllow(data)) {
                    handler.handle(data);
                }
                if (handlerNode.handler.needFireNext()) {
                    handlerNode = handlerNode.next;
                } else {
                    break;
                }
            }
        }

        public void fireTailToHead(MyData data) throws Exception {
            HandlerNode handlerNode = this.tail;
            while (handlerNode != null) {
                Handler handler = handlerNode.handler;
                if (handler.isAllow(data)) {
                    handler.handle(data);
                }
                if (handlerNode.handler.needFireNext()) {
                    handlerNode = handlerNode.prev;
                } else {
                    break;
                }
            }
        }

//        public Handler remove() {
//
//        }
//        public void addAfter(Handler left, Handler newHandler) {
//
//        }
    }

    public static void main(String[] args) throws Exception {
        HandlerPipeline handlerPipeline = new HandlerPipeline();
        handlerPipeline.addFirst(new OddHandler());
        handlerPipeline.addLast(new EvenHandler());

        handlerPipeline.fireHeadToTail(new MyData("1st msg"));
        handlerPipeline.fireTailToHead(new MyData("2nd msg"));
        handlerPipeline.fireTailToHead(new MyData("3rd msg"));
        handlerPipeline.fireHeadToTail(new MyData("4th msg"));
    }

}
