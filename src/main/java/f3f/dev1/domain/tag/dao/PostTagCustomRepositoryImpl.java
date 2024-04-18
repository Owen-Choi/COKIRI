package f3f.dev1.domain.tag.dao;

import f3f.dev1.domain.post.model.Post;
import f3f.dev1.domain.tag.model.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.util.List;

@RequiredArgsConstructor
public class PostTagCustomRepositoryImpl implements PostTagCustomRepository{

  private final JdbcTemplate jdbcTemplate;
  @Override
  public void postTagBulkInsert(Long postId, List<Long> tagsId) {
    String sql = "INSERT INTO post_tag (post_id, tag_id)\n"
        + "SELECT (?), (?)\n"
        + "WHERE NOT EXISTS (\n"
        +"    SELECT 2 FROM post_tag WHERE tag_id = ?);";

    jdbcTemplate.batchUpdate(sql, tagsId, tagsId.size(), (PreparedStatement ps, Long tagId) -> {
      ps.setLong(1, postId);
      ps.setLong(2, tagId);
      ps.setLong(3, tagId);
    });
  }
}
