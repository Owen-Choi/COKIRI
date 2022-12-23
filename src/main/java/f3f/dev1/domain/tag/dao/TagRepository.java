package f3f.dev1.domain.tag.dao;

import f3f.dev1.domain.tag.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findById(Long id);
    boolean existsByName(String name);
    boolean existsById(Long id);
}
