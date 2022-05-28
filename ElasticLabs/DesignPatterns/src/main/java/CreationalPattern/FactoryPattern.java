package CreationalPattern;

import ObjectDefinition.animal.Animal;
import ObjectDefinition.animal.Cat;
import ObjectDefinition.animal.Dog;
import ObjectDefinition.constance.Sex;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class FactoryPattern {

    enum AnimalType {
        CAT,
        DOG;
    }

    // 反射工厂，性能开销比较大，但更加灵活
    public static abstract class ReflectioinFactory<T> {
        abstract T createAnimal(Class<? extends T> clazz, Object... parms);
    }
    public static class AnimalReflectionFactory extends ReflectioinFactory<Animal> {
        public static final AnimalReflectionFactory FACTORY = new AnimalReflectionFactory();
        @Override Animal createAnimal(Class<? extends Animal> clazz, Object... parms) {
            try {
                Class<?>[] parmetersType = new Class<?>[parms.length];
                int index = 0;
                for (Object param : parms) {
                    parmetersType[index++] = param.getClass();
                }
                Constructor<? extends Animal> constructor = clazz.getConstructor(parmetersType);
                Animal ans = constructor.newInstance(parms);
                return ans;
            } catch (NoSuchMethodException nsme) {
                throw new IllegalArgumentException("no adaptable construction for clazz " + clazz.getName());
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException ce) {
                throw new RuntimeException("animal creation error", ce);
            }
        }
    }

    // 简单工厂
    public static class SimpleFactory {
        public static Animal create(AnimalType animalType) {
            switch (animalType) {
                case CAT:
                    return new Cat();
                case DOG:
                    return new Dog();
                default:
                    throw new IllegalArgumentException("Unknown animal type: " + animalType.name());
            }
        }
    }

    // 抽象工厂，一个工厂生产配套的对象，比如从手机到手机部件，闭环生产
    public static class MultiFactory {

    }

    public static void main(String[] args) {
        Animal animal = SimpleFactory.create(AnimalType.CAT);
        animal.shout();

        animal = AnimalReflectionFactory.FACTORY.createAnimal(Cat.class, "banben", Cat.CatType.LI_HUA, Sex.Male);
        animal.shout();
    }

}
