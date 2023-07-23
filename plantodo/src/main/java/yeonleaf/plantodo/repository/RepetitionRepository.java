package yeonleaf.plantodo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yeonleaf.plantodo.domain.Repetition;

@Repository
public interface RepetitionRepository extends JpaRepository<Repetition, Long> {
}
