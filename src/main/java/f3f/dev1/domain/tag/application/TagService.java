package f3f.dev1.domain.tag.application;

import f3f.dev1.domain.post.dao.PostRepository;
import f3f.dev1.domain.post.model.Post;
import f3f.dev1.domain.tag.dao.PostTagRepository;
import f3f.dev1.domain.tag.dao.TagRepository;
import f3f.dev1.global.error.exception.NotFoundByIdException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final PostRepository postRepository;
    private final PostTagRepository postTagRepository;

    /*
        C : create
        태그 자체의 생성과 게시글에 태그를 추가하는 2가지 경우로 분리
     */

    @Transactional
    public void addTagsToPost(Long postId, List<String> tagNames) {
        Post post = postRepository.findById(postId).orElseThrow(NotFoundByIdException::new);
        tagRepository.saveTagWithBulk(tagNames);
        List<Long> tagsId = tagRepository.findByNameIn(tagNames).stream().map(tag -> tag.getId()).collect(Collectors.toList());
        postTagRepository.savePostTagWithBulk(post.getId(), tagsId);
    }


}
