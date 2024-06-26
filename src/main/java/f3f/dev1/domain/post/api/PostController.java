package f3f.dev1.domain.post.api;

import f3f.dev1.domain.member.exception.NotAuthorizedException;
import f3f.dev1.domain.model.TradeStatus;
import f3f.dev1.domain.post.application.PostService;
import f3f.dev1.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static f3f.dev1.domain.member.dto.MemberDTO.GetUserPost;
import static f3f.dev1.domain.post.dto.PostDTO.*;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;


    // 내 게시글 조회
    @GetMapping("/user/posts")
    public ResponseEntity<Page<GetUserPost>> getUserPosts(Pageable pageable) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(postService.getUserPostDto(currentMemberId, pageable));
    }

    // 게시글 전체 조회 세분화 - 태그 제외 조건들 검색
    @GetMapping(value = "/post")
    public ResponseEntity<Page<PostSearchResponseDto>> getPostsWithConditionExcludeTags(
            @RequestParam(value= "productCategory", required = false, defaultValue = "") String productCategoryName,
            @RequestParam(value= "wishCategory", required = false, defaultValue = "") String wishCategoryName,
            @RequestParam(value = "minPrice", required = false, defaultValue = "") String minPrice,
            @RequestParam(value = "maxPrice", required = false, defaultValue = "") String maxPrice,
            @RequestParam(value="trade", defaultValue = "1") long trade,
            Pageable pageable) {
        Long currentMemberId = SecurityUtil.getCurrentNullableMemberId();
        TradeStatus tradeStatus = TradeStatus.findById(trade);
            SearchPostRequestExcludeTag request = SearchPostRequestExcludeTag.builder()
                    .productCategory(productCategoryName)
                    .wishCategory(wishCategoryName)
                    .tradeStatus(tradeStatus)
                    .minPrice(minPrice)
                    .maxPrice(maxPrice)
                    .build();
        Page<PostSearchResponseDto> pageDto = postService.findPostsByCategoryAndPriceRange(request, currentMemberId, pageable);
        return new ResponseEntity<>(pageDto, HttpStatus.OK);
    }


    /**
     * 태그를 포함해서 검색하는 api와 포함하지 않고 검색하는 api를 분리하는 이유는 프론트 측에서 아예 이 둘이 별도의 기능으로 제공되기 때문이다.
     * @param tagNames
     * @param trade
     * @param pageable
     * @return
     */
    @GetMapping(value = "/post/tagSearch")
    public ResponseEntity<Page<PostSearchResponseDto>> getPostsWithTagNames(
            @RequestParam(value = "tags", required = false, defaultValue = "") List<String> tagNames,
            @RequestParam(value="trade", required = true, defaultValue = "1") long trade,
            Pageable pageable) {
        Page<PostSearchResponseDto> resultList;
        // DB에서 찾아오는 행위가 아니라, enum 클래스에서 키 값으로 value를 찾아오는 코드이다. 헷갈리지 말자.
        TradeStatus tradeStatus = TradeStatus.findById(trade);
        Long currentMemberId = SecurityUtil.getCurrentNullableMemberId();
        if(!tagNames.isEmpty()) {
            resultList = postService.findPostsWithTagNameList(tagNames, currentMemberId, tradeStatus, pageable);
        } else {
            // TODO 태그 검색은 동적 쿼리로 작성을 못해서 분기를 나눴다. findAll에서 tradeStatus를 고려해주게 코드를 바꿔주면 될 것 같다.
//            resultList = postService.findAll(currentMemberId, pageable);
            resultList = postService.findOnlyWithTradeStatus(currentMemberId, tradeStatus, pageable);
        }
        return new ResponseEntity<>(resultList, HttpStatus.OK);
    }

    // 다른 사용자의 게시글을 열람할 때 사용되는데, 뭔가 구조가 이상하다.
    @GetMapping(value = "/post/user/{memberId}")
    public ResponseEntity<Page<GetUserPost>> getUserPostById(@PathVariable(name = "memberId") Long memberId, Pageable pageable) {
        return ResponseEntity.ok(postService.findPostByAuthorId(memberId, pageable));
    }

    // 게시글 작성
    @PostMapping(value = "/post")
    public ResponseEntity<Long> createPost(@RequestBody PostSaveRequest postSaveRequest) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Long postId = postService.savePost(postSaveRequest, currentMemberId);

        return new ResponseEntity<>(postId, HttpStatus.CREATED);
    }

    // 게시글 정보 조회
    // TODO 해당 게시글이 본인이 스크랩한 게시글인지 판단해줘야 함
    // scrapPost랑 scrap을 postId로 조인 걸고 userId로 존재하는 scrapPost 있는지 찾아보기 - 네이티브 쿼리로 짜야하나
    @GetMapping(value = "/post/{postId}")
    public ResponseEntity<SinglePostInfoDto> getPostInfo(@PathVariable(name = "postId") Long postId) {
        Long currentMemberId = SecurityUtil.getCurrentNullableMemberId();
        SinglePostInfoDto postInfoDto = postService.findPostById(postId, currentMemberId);
        return new ResponseEntity<>(postInfoDto, HttpStatus.OK);
    }


    // 게시글 정보 수정
    @PatchMapping(value = "/post/{postId}")
    public ResponseEntity<String> updatePostInfo(@PathVariable(name = "postId") Long postId, @RequestBody @Valid UpdatePostRequest updatePostRequest) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        // currentMemberId 관련 예외는 여기서 일괄 처리하겠다.
        if(!updatePostRequest.getAuthorId().equals(currentMemberId)) {
            throw new NotAuthorizedException("요청자가 현재 로그인한 유저가 아닙니다");
        }

        postService.updatePostWithPatch(updatePostRequest, postId);
        return new ResponseEntity<>("UPDATED", HttpStatus.OK);
    }

    // 게시글 삭제
    @DeleteMapping(value = "/post/{postId}")
    public ResponseEntity<String> deletePost(@RequestBody @Valid DeletePostRequest deletePostRequest,@PathVariable(name = "postId") Long postId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        String result = postService.deletePost(deletePostRequest, currentMemberId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
