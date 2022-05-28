package ObjectDefinition.ownership;

import ObjectDefinition.animal.Animal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class Family implements PetGoing {

    private List<Animal> pets = new ArrayList<>();

    public List<Animal> getPets() {
        return pets;
    }

    public void setPets(List<Animal> pets) {
        this.pets = pets;
    }

    @Override public void addPet(Animal pet) {
        Optional<Animal> opt = pets.stream().filter(p -> pet.getName().equals(p.getName())).findAny();
        if (opt.isPresent()) {
            throw new IllegalArgumentException("Pet name " + opt.get().getName() + " is already exist.");
        }
        pets.add(pet);
    }

    @Override public void rmPet(String name) {
        Iterator<Animal> petIterator = pets.iterator();
        while (petIterator.hasNext()) {
            if (name.equals(petIterator.next().getName())) {
                petIterator.remove();
                break;
            }
        }
    }
}
