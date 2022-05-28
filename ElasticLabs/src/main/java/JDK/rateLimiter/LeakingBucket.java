package JDK.rateLimiter;

/**
 * 漏桶算法，桶容量为n，桶流出速率为l
 * 当T1时刻桶用量为m(m<=n)，T2时刻(T2>T1)入流量为x
 * 首先，T2时刻桶的余量为：left = Math.min(n, n - m + (T2-T1)x)
 * 当x<left，则通过，否则被禁止
 */
public class LeakingBucket {

    private final int capacity;
    private final int maxQPS;
    private final float rate;
    private long lastAccessTs = System.currentTimeMillis();
    private int left;

    public LeakingBucket(int capacity, int maxQPS) {
        this.capacity = capacity;
        this.maxQPS = maxQPS;
        this.rate = (float)maxQPS/1000;
        this.left = capacity;
    }

    public synchronized boolean acquire(int permits) {
        long currTs = System.currentTimeMillis();
        int exp = Math.round(left + (currTs - lastAccessTs) * rate);
        if (exp > 0) {
            lastAccessTs = currTs;
        }
        int l = Math.min(capacity, exp);
        l = (l > 0 ? l : 0);
        if (l >= permits) {
            left = l - permits;
            return true;
        } else {
            return false;
        }
    }

    public boolean acquire() {
        return acquire(1);
    }

    public static void main(String[] args) throws Exception {
        LeakingBucket rateLimiter = new LeakingBucket(5, 1);
        int i = 0;
        while (i < 200) {
            if (rateLimiter.acquire()) {
                System.out.println(i++);
            } else {
                Thread.sleep(1);
            }
        }
    }

}
