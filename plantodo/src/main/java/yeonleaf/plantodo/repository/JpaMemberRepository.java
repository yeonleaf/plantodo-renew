package yeonleaf.plantodo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yeonleaf.plantodo.domain.Member;

import java.util.List;
import java.util.Map;

@Repository
public interface JpaMemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByEmail(String email);
}