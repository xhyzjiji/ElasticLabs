package ObjectDefinition.ownership;

import ObjectDefinition.animal.Animal;

public class PetHospital implements PetGoing {

    // 每个宠物除了本身属性外，还需要记录入院和被收养的信息

    // 医院收养的宠物
    @Override public void addPet(Animal pet) {

    }

    // 医院被领养的宠物
    @Override public void rmPet(String name) {

    }
}
