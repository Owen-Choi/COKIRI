package f3f.dev1.domain.tag.dao;

import f3f.dev1.domain.post.model.Post;
import f3f.dev1.domain.tag.model.Tag;

import java.util.List;

public interface PostTagCustomRepository {
  void postTagBulkInsert(Long postId, List<Long> tagsId);
}
