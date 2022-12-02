package Netty.Issues;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class IssueDemoMain {

    protected static final Lock lock = new ReentrantLock();
    protected static final Condition condition = lock.newCondition();

    public static void main(String[] args) throws Exception {
        ObjectServer server = new ObjectServer();
        ObjectClient client1 = new ObjectClient();
        ObjectClient client2 = new ObjectClient();
        try {
            server.start();

            Thread.sleep(1 * 1000);

            client1.start();
            client1.regist();
            client2.start();
            client2.regist();

            Thread.sleep(1 * 1000);

            for (int i = 0; i < 5; i++) {
                server.send(client1.getClientId(), MessageObject.newMessageObject(100 /** 1024 * 1024*/, false));
                server.send(client2.getClientId(), MessageObject.newMessageObject(100 /** 1024 * 1024*/, false));
            }

//            client1.send(MessageObject.newMessageObject(200, false));

            server.send(client1.getClientId(), MessageObject.newMessageObject(100 /** 1024 * 1024*/, true));
            server.send(client2.getClientId(), MessageObject.newMessageObject(100 /** 1024 * 1024*/, true));

//            lock.lock();
//            condition.await();
//            lock.unlock();
            Thread.sleep(3000);
        } finally {
            client1.stop();
            client2.stop();
            server.stop();
        }
    }

}
