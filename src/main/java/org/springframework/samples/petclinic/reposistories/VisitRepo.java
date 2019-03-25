import java.util.Collection;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitRepository;

class VisitRepo implements VisitRepository {

    @Override
    public void save(Visit visit) throws DataAccessException {

    }

    @Override
    public List<Visit> findByPetId(Integer petId) {
        return null;
    }

    @Override
    public Collection<Visit> findAll() {
        return null;
    }

}