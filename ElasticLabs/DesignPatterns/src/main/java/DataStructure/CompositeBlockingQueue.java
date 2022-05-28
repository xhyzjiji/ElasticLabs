package DataStructure;

import com.sun.xml.internal.ws.util.CompletedFuture;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.apache.commons.collections4.CollectionUtils;

public class CompositeBlockingQueue<E> implements BlockingQueue<E> {

    private BlockingQueue<BlockingQueue<E>> historicQueue;
    private AtomicReference<BlockingQueue<E>> currentQueue;
    // 可以改成策略模式，自旋/锁形式自选或者外部实现自己的策略
    private AtomicBoolean spinLock = new AtomicBoolean(false);

    // 在这里实现
    private <T, R> R syncFunction(Function<T, R> function, T object) {
        do {
            boolean lock = spinLock.compareAndSet(false, true);
            if (lock) {
                try {
                    return function.apply(object);
                } finally {
                    spinLock.set(false);
                }
            } else {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ie) {
                    // swallow interrupted exception
                }
            }
        } while (true);
    }

    public CompositeBlockingQueue(BlockingQueue newQueue) {
        historicQueue = new LinkedBlockingQueue<>(10);
        currentQueue.set(newQueue);
    }

    public boolean changeQueue(BlockingQueue newQueue) {
        return syncFunction((Function<Void, Boolean>) unused -> {
            if (historicQueue.add(currentQueue.get())) {
                currentQueue.set(newQueue);
                return true;
            } else {
                return false;
            }
        }, null);
    }

    @Override public boolean add(E e) {
        return syncFunction((Function<Void, Boolean>) unused -> currentQueue.get().add(e), null);
    }

    @Override public boolean offer(E e) {
        return syncFunction((Function<Void, Boolean>) unused -> currentQueue.get().offer(e), null);
    }

    @Override public void put(E e) throws InterruptedException {
        CompletedFuture<Void> res = syncFunction((Function<Void, CompletedFuture<Void>>) unused -> {
            try {
                currentQueue.get().put(e);
                return new CompletedFuture<Void>(null, null);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
                return new CompletedFuture<Void>(null, ie);
            }
        }, null);
        try {
            res.get();
        } catch (Throwable te) {
            throw new RuntimeException(te);
        }
    }

    // 如果队列满可能导致多个线程自旋
    @Override public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        // Function接口不支持异常
        return syncFunction((Function<Void, Boolean>) unused -> {
            try {
                return currentQueue.get().offer(e, timeout, unit);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
                return false;
            }
        }, null);
    }

    @Override public E take() throws InterruptedException {
        CompletedFuture<E> res = syncFunction((Function<Void, CompletedFuture<E>>) unused -> {
            try {
                Iterator<BlockingQueue<E>> queueIterator = historicQueue.iterator();
                while (queueIterator.hasNext()) {
                    BlockingQueue<E> queue = queueIterator.next();
                    if (CollectionUtils.isEmpty(queue)) {
                        queueIterator.remove();
                    } else {
                        return new CompletedFuture<E>(queue.take(), null);
                    }
                }
                return new CompletedFuture<E>(currentQueue.get().take(), null);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
                return new CompletedFuture<>(null, ie);
            }
        }, null);
        try {
            return res.get();
        } catch (Throwable te) {
            throw new RuntimeException(te);
        }
    }

    @Override public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override public int remainingCapacity() {
        return 0;
    }

    @Override public boolean remove(Object o) {
        return false;
    }

    @Override public boolean contains(Object o) {
        return false;
    }

    @Override public int drainTo(Collection<? super E> c) {
        return 0;
    }

    @Override public int drainTo(Collection<? super E> c, int maxElements) {
        return 0;
    }

    @Override public E remove() {
        return null;
    }

    @Override public E poll() {
        return null;
    }

    @Override public E element() {
        return null;
    }

    @Override public E peek() {
        return null;
    }

    @Override public int size() {
        return 0;
    }

    @Override public boolean isEmpty() {
        return false;
    }

    @Override public Iterator<E> iterator() {
        return null;
    }

    @Override public Object[] toArray() {
        return new Object[0];
    }

    @Override public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override public boolean addAll(Collection<? extends E> c) {
        return false;
    }

    @Override public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override public void clear() {

    }
}
