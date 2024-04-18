package f3f.dev1.domain.tag.dao;

import f3f.dev1.domain.tag.model.Tag;

import java.util.List;

public interface TagCustomRepository {
  void saveTagWithBulk(List<String> tagNames);
}
