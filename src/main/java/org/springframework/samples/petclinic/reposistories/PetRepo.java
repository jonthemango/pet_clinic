import java.util.List;

import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.owner.PetType;

class PetRepo implements PetRepository {

    @Override
    public List<PetType> findPetTypes() {
        return null;
    }

    @Override
    public Pet findById(Integer id) {
        return null;
    }

    @Override
    public void save(Pet pet) {

    }

}