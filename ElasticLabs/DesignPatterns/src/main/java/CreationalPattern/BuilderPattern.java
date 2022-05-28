package CreationalPattern;

import ObjectDefinition.animal.Cat;
import ObjectDefinition.constance.Sex;

public class BuilderPattern {

    interface Builder<T> {
        T build();
    }

    public static class CatBuilder implements Builder<Cat> {
        private Cat cat = new Cat();
        public static CatBuilder builder() {
            return new CatBuilder();
        }

        @Override public Cat build() {
            return cat;
        }

        public CatBuilder name(String name) {
            cat.setName(name);
            return this;
        }

        public CatBuilder sex(Sex sex) {
            cat.setSex(sex);
            return this;
        }

        public CatBuilder type(Cat.CatType catType) {
            cat.setCatType(catType);
            return this;
        }
    }

    public static void main(String[] args) {
        Cat myCat = CatBuilder.builder().name("Banben").sex(Sex.Male).build();
        myCat.shout();
    }
}
