package yeonleaf.plantodo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yeonleaf.plantodo.domain.Checkbox;

import java.util.List;

@Repository
public interface CheckboxRepository extends JpaRepository<Checkbox, Long> {

    List<Checkbox> findByGroupId(Long id);
}
