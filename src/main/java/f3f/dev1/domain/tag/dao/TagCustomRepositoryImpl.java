package f3f.dev1.domain.tag.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.util.List;

@RequiredArgsConstructor
public class TagCustomRepositoryImpl implements TagCustomRepository{

  private final JdbcTemplate jdbcTemplate;

  @Override
  public void tagBulkInsert(List<String> tagNames) {
    String sql = "INSERT INTO tag (name)\n"
        + "SELECT ? \n"
        + "WHERE NOT EXISTS (\n"
        + "   SELECT 1 FROM tag where name = ?);";

    jdbcTemplate.batchUpdate(sql, tagNames, tagNames.size(), (PreparedStatement ps, String tagName) -> {
      ps.setString(1, tagName);
      ps.setString(2, tagName);
    });
  }
}
