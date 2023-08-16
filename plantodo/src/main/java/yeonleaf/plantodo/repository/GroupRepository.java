package yeonleaf.plantodo.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yeonleaf.plantodo.domain.Group;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    List<Group> findByPlanId(Long planId);

    @EntityGraph(attributePaths = {"plan", "repetition"})
    @Query("select g from Group g where g.plan.id = :planId")
    List<Group> findByPlanIdEntityGraph(Long planId);

    @EntityGraph(attributePaths = {"plan", "repetition"})
    @Query("select g from Group g where g.id = :id")
    Optional<Group> findByIdEntityGraph(@Param("id") Long id);

}
