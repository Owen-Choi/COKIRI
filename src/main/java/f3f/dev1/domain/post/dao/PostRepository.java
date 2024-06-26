package f3f.dev1.domain.post.dao;

import f3f.dev1.domain.category.model.Category;
import f3f.dev1.domain.member.model.Member;
import f3f.dev1.domain.model.TradeStatus;
import f3f.dev1.domain.post.model.Post;
import f3f.dev1.domain.tag.model.PostTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostCustomRepository {

    List<Post> findAll();
    boolean existsById(Long id);
    Optional<Post> findById(Long id);
    boolean existsByAuthorId(Long authorId);
    List<Post> findByAuthorId(Long authorId);
    List<Post> findByPostTagsIn(List<PostTag> postTags);
    void deleteById(Long id);
    Page<Post> findAll(Pageable pageable);

}
