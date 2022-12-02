package ThreadLab;

import net.openhft.affinity.AffinityLock;
import net.openhft.affinity.AffinityStrategies;

import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadAffinity {

    public static void main(String[] args) throws Exception {
        final AtomicBoolean shutdownReq = new AtomicBoolean(false);
        Thread t1 = new Thread() {
            @Override
            public void run() {
                try (AffinityLock al = AffinityLock.acquireLock(0)) {
                    // do some work while locked to a CPU.
                    while (shutdownReq.get() == false) {
                    }
                }
            }
        };
        Thread t2 = new Thread() {
            @Override
            public void run() {
                try (AffinityLock al = AffinityLock.acquireLock(1)) {
                    while (shutdownReq.get() == false) {
                        int i = 0;
                        while (i < 100) {
                            i++;
                        }
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException ie) {

                        }
                    }
                }
            }
        };
        t2.start();
        t1.start();

        Thread.sleep(300_000);
        shutdownReq.set(true);
        t1.join();
        t2.join();

        System.out.println("exit.");
    }

}
