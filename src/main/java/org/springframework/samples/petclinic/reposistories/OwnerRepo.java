import java.util.Collection;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;

class OwnerRepo implements OwnerRepository {
    public OwnerRepo() {
        
    }

    @Override
    public Collection<Owner> findByLastName(String lastName) {
        return null;
    }

    @Override
    public Owner findById(Integer id) {
        return null;
    }

    @Override
    public Collection<Owner> findAll() {
        return null;
    }

    @Override
    public void save(Owner owner) {

    }


}