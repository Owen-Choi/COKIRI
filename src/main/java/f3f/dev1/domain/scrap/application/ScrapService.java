package f3f.dev1.domain.scrap.application;

import f3f.dev1.domain.member.dao.MemberRepository;
import f3f.dev1.domain.member.exception.NotAuthorizedException;
import f3f.dev1.domain.member.model.Member;
import f3f.dev1.domain.post.dao.PostRepository;
import f3f.dev1.domain.scrap.dao.ScrapPostRepository;
import f3f.dev1.domain.scrap.dto.ScrapPostDTO;
import f3f.dev1.domain.post.model.Post;
import f3f.dev1.domain.post.model.ScrapPost;
import f3f.dev1.domain.scrap.dao.ScrapPostRepositoryImpl;
import f3f.dev1.domain.scrap.dao.ScrapRepository;
import f3f.dev1.domain.scrap.exception.DuplicateScrapByUserIdException;
import f3f.dev1.domain.scrap.exception.NotFoundPostInScrapException;
import f3f.dev1.domain.scrap.model.Scrap;
import f3f.dev1.global.error.exception.NotFoundByIdException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static f3f.dev1.domain.scrap.dto.ScrapDTO.*;
import static f3f.dev1.domain.scrap.dto.ScrapPostDTO.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final MemberRepository memberRepository;
    private final ScrapPostRepository scrapPostRepository;
    private final PostRepository postRepository;

    // 스크랩 생성 메서드
    @Transactional
    public void createScrap(CreateScrapDTO createScrapDTO) {
        if (scrapRepository.existsByMemberId(createScrapDTO.getUser().getId())) {
            throw new DuplicateScrapByUserIdException();
        }

        Scrap scrap = createScrapDTO.toEntity();
        scrapRepository.save(scrap);

    }

    // 스크랩에 있는 포스트조회 메서드
    @Transactional(readOnly = true)
    public Page<GetUserScrapPost> getUserScrapPosts(Long memberId, Pageable pageable) {
        Scrap scrapByUserId = scrapRepository.findScrapByMemberId(memberId).orElseThrow(NotFoundByIdException::new);
        return scrapPostRepository.findUserScrapPost(scrapByUserId.getId(), pageable);
    }

    // 스크랩에 관심 포스트 추가 메소드
    // 세션에서 받아온 유저와 프론트에서 넘어온 유저가 다르면 예외 던지게 처리함
    @Transactional
    public CreateScrapPostDTO addScrapPost(AddScrapPostDTO addScrapPostDTO, Long memberId) {
        if (!addScrapPostDTO.getUserId().equals(memberId)) {
            throw new NotAuthorizedException();
        }

        Scrap scrap = scrapRepository.findScrapByMemberId(addScrapPostDTO.getUserId()).orElseThrow(NotFoundByIdException::new);
        Post post = postRepository.findById(addScrapPostDTO.getPostId()).orElseThrow(NotFoundByIdException::new);
        Optional<ScrapPost> byScrapIdAndPostId = scrapPostRepository.findByScrapIdAndPostId(scrap.getId(), addScrapPostDTO.getPostId());
        if (byScrapIdAndPostId.isPresent()) {
            log.info("already created scrap post");
            return byScrapIdAndPostId.get().toCreateScrapPostDTO();
        } else {
            ScrapPost scrapPost = ScrapPost.builder().post(post).scrap(scrap).build();
            scrapPostRepository.save(scrapPost);
            CreateScrapPostDTO createScrapPostDTO = scrapPost.toCreateScrapPostDTO();

            log.info("scrap post created " + scrapPost.getId());
            return createScrapPostDTO;
        }


    }

    // 스크랩에 있는 포스트 삭제 메서드
    // 세션에서 받아온 유저와 프론트에서 넘어온 유저가 다르면 예외 던지게 처리할 예정
    @Transactional
    public String deleteScrapPost(DeleteScrapPostDTO deleteScrapPostDTO, Long memberId) {
        if (!deleteScrapPostDTO.getUserId().equals(memberId)) {
            throw new NotAuthorizedException();
        }
        Member user = memberRepository.findById(memberId).orElseThrow(NotFoundByIdException::new);
        Post post = postRepository.findById(deleteScrapPostDTO.getPostId()).orElseThrow(NotFoundByIdException::new);
        Scrap scrap = scrapRepository.findScrapByMemberId(user.getId()).orElseThrow(NotFoundByIdException::new);
        ScrapPost scrapPost = scrapPostRepository.findByScrapIdAndPostId(scrap.getId(), post.getId()).orElseThrow(NotFoundPostInScrapException::new);
        scrapPostRepository.delete(scrapPost);


        return "DELETE";
    }

}
