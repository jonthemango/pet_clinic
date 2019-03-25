import java.util.Collection;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;

class VetRepo implements VetRepository {

    @Override
    public Collection<Vet> findAll() throws DataAccessException {
        return null;
    }
}