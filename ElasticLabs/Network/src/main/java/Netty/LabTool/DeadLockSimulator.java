package Netty.LabTool;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DeadLockSimulator {

    public static final Object lock = new Object();
    public static void triggerDeadLock(Supplier supplier) {
        synchronized (lock) {
            supplier.get();
        }
    }

}
