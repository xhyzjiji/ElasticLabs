package CreationalPattern;

import ObjectDefinition.animal.Cat;
import java.util.Objects;

// 单例模式：如何产生一个类唯一对象
public class SingletonPattern {

    public static class DoubleCheckLoad {
        // 注意并发可见性
        private static volatile Cat catSingletonInstance;

        public static Cat getCatSingleInstance() {
            if (Objects.isNull(catSingletonInstance)) {
                synchronized (catSingletonInstance) {
                    if (Objects.isNull(catSingletonInstance)) {
                        DoubleCheckLoad.catSingletonInstance = new Cat();
                    }
                }
            }
            return catSingletonInstance;
        }
    }

    public static class StaticLoad {
        public static final Cat catSingletonInstance = new Cat();
    }

    public static void main(String[] args) {
        DoubleCheckLoad.getCatSingleInstance().shout();

        StaticLoad.catSingletonInstance.shout();
    }

}
