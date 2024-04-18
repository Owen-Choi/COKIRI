package f3f.dev1.domain.tag.dao;

import java.util.List;

public interface PostTagCustomRepository {
  void savePostTagWithBulk(Long postId, List<Long> tagsId);
}
