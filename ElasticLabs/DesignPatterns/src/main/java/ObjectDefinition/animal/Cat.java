package ObjectDefinition.animal;

import ObjectDefinition.constance.Sex;

public class Cat implements Animal {

    public enum CatType {
        LI_HUA;
    }

    private String name = "xxx";
    private CatType catType;
    private Sex sex;

    public Cat() {
    }

    public Cat(String name, CatType catType, Sex sex) {
        this.name = name;
        this.catType = catType;
        this.sex = sex;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CatType getCatType() {
        return catType;
    }

    public void setCatType(CatType catType) {
        this.catType = catType;
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
        System.out.println(name + "eat cat food");
    }

    @Override public void shout() {
        System.out.println(name + " make a noise: miao~~");
    }

}
