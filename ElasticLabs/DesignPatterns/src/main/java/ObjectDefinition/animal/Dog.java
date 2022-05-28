package ObjectDefinition.animal;

import ObjectDefinition.constance.Sex;

public class Dog implements Animal {

    public enum DogType {

    }

    private String name;
    private DogType dogType;
    private Sex sex;

    public void setName(String name) {
        this.name = name;
    }

    public DogType getDogType() {
        return dogType;
    }

    public void setDogType(DogType dogType) {
        this.dogType = dogType;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    @Override public String getName() {
        return name;
    }

    @Override public void eat() {
        System.out.println(name + "eat dog food");
    }

    @Override public void shout() {
        System.out.println("wo! wo! wo~wo~wo!");
    }

}
