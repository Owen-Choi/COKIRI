package f3f.dev1.domain.post.application;

import f3f.dev1.domain.member.model.Member;
import f3f.dev1.domain.post.dao.PostRepository;
import f3f.dev1.domain.post.exception.NotFoundPostListByAuthor;
import f3f.dev1.domain.post.exception.NotMatchingAuthorException;
import f3f.dev1.domain.post.model.Post;
import f3f.dev1.domain.member.dao.MemberRepository;
import f3f.dev1.domain.trade.dao.TradeRepository;
import f3f.dev1.domain.trade.model.Trade;
import f3f.dev1.global.error.exception.NotFoundByIdException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

import static f3f.dev1.domain.post.dto.PostDTO.*;
import static f3f.dev1.global.common.constants.ResponseConstants.*;

@Service
@Validated
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final TradeRepository tradeRepository;

    @Transactional
    public Long savePost(PostSaveRequest postSaveRequest) {

        // 유저 객체 받아와서 포스트 리스트에 추가해줘야 함
        // TODO Trade 객체를 어떻게 처리할지 아직 명확하지 않음
        Member member = memberRepository.findById(postSaveRequest.getAuthor().getId()).orElseThrow(NotFoundByIdException::new);

        /* TODO 카테고리 객체 받아와서 카테고리 리스트에 추가해줘야 함
            categoryRepository.findById(productCategory.getId()) ~
            해당 부분이 구현되면 추가하겠음
         */

        Post post = postSaveRequest.toEntity();
        member.getPosts().add(post);
        postRepository.save(post);
        return post.getId();
    }

    /* TODO
        R : read
        카테고리, 태그 필터링은 Post에서 할 수 없다. 각각의 서비스에서 구현하겠다.
        title 별로 조회도 필요할까? 검색엔진이 필요한가? - 피드백 결과 일단은 제외하는 걸로.
     */

    // 게시글 전체 조회
    public List<PostInfoDto> findAllPosts() {
        List<Post> allPosts = postRepository.findAll();
        List<PostInfoDto> response = new ArrayList<>();
        for (Post post : allPosts) {
            PostInfoDto responseEach = PostInfoDto.builder()
                    .title(post.getTitle())
                    .content(post.getContent())
                    .tradeEachOther(post.getTradeEachOther())
                    .authorNickname(post.getAuthor().getNickname())
                    .wishCategory(post.getWishCategory().getName())
                    .productCategory(post.getProductCategory().getName())
                    .tradeStatus(post.getTrade().getTradeStatus())
                    .build();
            response.add(responseEach);
        }
        return response;
    }

    // findByIdPostListDTO는 검색된 포스트 리스트를 가지고 있는 DTO이다.
    @Transactional(readOnly = true)
    public List<PostInfoDto> findPostByAuthor(Long authorId) {
        if(!postRepository.existsByAuthorId(authorId)) {
            throw new NotFoundPostListByAuthor("해당 작성자의 게시글이 없습니다.");
        }
        List<PostInfoDto> response = new ArrayList<>();
        List<Post> byAuthor = postRepository.findByAuthorId(authorId);
        for (Post post : byAuthor) {
            PostInfoDto responseEach = PostInfoDto.builder()
                    .title(post.getTitle())
                    .content(post.getContent())
                    .tradeEachOther(post.getTradeEachOther())
                    .authorNickname(post.getAuthor().getNickname())
                    .wishCategory(post.getWishCategory().getName())
                    .productCategory(post.getProductCategory().getName())
                    .tradeStatus(post.getTrade().getTradeStatus())
                    .build();
            response.add(responseEach);
        }
        return response;
    }

    @Transactional(readOnly = true)
    public PostInfoDto findPostById(Long id) {
        Post post = postRepository.findById(id).orElseThrow(NotFoundByIdException::new);
//        // TODO 거래 가능 상태인지 확인하기
//        Trade trade = tradeRepository.findByPostId(post.getId()).orElseThrow(NotFoundByIdException::new);
        PostInfoDto response = PostInfoDto.builder()
                .title(post.getTitle())
                .content(post.getContent())
                .tradeEachOther(post.getTradeEachOther())
                .authorNickname(post.getAuthor().getNickname())
                .wishCategory(post.getWishCategory().getName())
                .productCategory(post.getProductCategory().getName())
                .tradeStatus(post.getTrade().getTradeStatus())
                .build();
        return response;
    }

    /* TODO
        U : 게시글 업데이트
     */

    // TODO 피드백 받기 : 반환 타입을 Long (id)으로 하는 거랑 Post 객체 하나만 가지고 있는 DTO로 하는 것 중 뭐가 더 나은가
    @Transactional
    public PostInfoDto updatePost(UpdatePostRequest updatePostRequest) {
        Post post = postRepository.findById(updatePostRequest.getId()).orElseThrow(NotFoundByIdException::new);
        // 게시글 변경 정보가 기존이랑 똑같다면 (변화가 없다면) 예외를 터트리려 했는데, 그럴 필요가 없어보여서 일단은 검증하지 않겠다
        // 근데 또 예외는 던져놓고 처리를 안하는 방법도 있으니 이건 피드백을 받아 볼 예정
        /* TODO
            업데이트된 카테고리들이 유효한지 각각 Id로 확인한다.
            관련 기능이 구현되면 추가할 것이고, 지금은 유효하다고 가정하고 로직을 작성하겠음
         */

        post.updatePostInfos(updatePostRequest);
        PostInfoDto response = PostInfoDto.builder()
                .title(post.getTitle())
                .content(post.getContent())
                .tradeEachOther(post.getTradeEachOther())
                .authorNickname(post.getAuthor().getNickname())
                .wishCategory(post.getWishCategory().getName())
                .productCategory(post.getProductCategory().getName())
                .tradeStatus(post.getTrade().getTradeStatus())
                .build();
        return response;
    }

    @Transactional
    public String deletePost(DeletePostRequest deletePostRequest) {
        // 먼저 해당 게시글이 존재하는지 검증
        Post post = postRepository.findById(deletePostRequest.getId()).orElseThrow(NotFoundByIdException::new);
        // 그 후 작성자가 요청자와 동일인물인지 검증
        Member author = post.getAuthor();
        // TODO Id로만 비교하는게 좀 걸린다. 그렇다고 비밀번호 검증은 너무 투머치 같기도 하다
        if(!author.getId().equals(deletePostRequest.getRequester().getId())) {
            throw new NotMatchingAuthorException("게시글 작성자가 아닙니다.");
        }
        postRepository.deleteById(deletePostRequest.getId());
        return "DELETE";
    }

}