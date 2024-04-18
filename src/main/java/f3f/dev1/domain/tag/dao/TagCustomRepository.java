package f3f.dev1.domain.tag.dao;

import java.util.List;

public interface TagCustomRepository {
  void tagBulkInsert(List<String> tagNames);
}
