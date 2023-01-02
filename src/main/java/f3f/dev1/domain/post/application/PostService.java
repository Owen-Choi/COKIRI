package f3f.dev1.domain.post.application;

import f3f.dev1.domain.category.dao.CategoryRepository;
import f3f.dev1.domain.category.model.Category;
import f3f.dev1.domain.member.model.Member;
import f3f.dev1.domain.post.dao.PostRepository;
import f3f.dev1.domain.post.exception.NotFoundPostListByAuthor;
import f3f.dev1.domain.post.exception.NotMatchingAuthorException;
import f3f.dev1.domain.post.model.Post;
import f3f.dev1.domain.member.dao.MemberRepository;
import f3f.dev1.domain.tag.dao.PostTagRepository;
import f3f.dev1.domain.tag.model.PostTag;
import f3f.dev1.domain.trade.dao.TradeRepository;
import f3f.dev1.global.error.exception.NotFoundByIdException;
import f3f.dev1.global.util.DeduplicationUtils;
import f3f.dev1.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

import static f3f.dev1.domain.post.dto.PostDTO.*;

@Service
@Validated
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final TradeRepository tradeRepository;
    private final CategoryRepository categoryRepository;
    private final PostTagRepository postTagRepository;

    @Transactional
    public Long savePost(PostSaveRequest postSaveRequest, Long currentMemberId) {

        // TODO memberId를 추가로 받기 때문에 관련 로직 작성해야함
        Member member = memberRepository.findById(postSaveRequest.getAuthorId()).orElseThrow(NotFoundByIdException::new);
        List<PostTag> resultsList = new ArrayList<>();
        Category productCategory = categoryRepository.findById(postSaveRequest.getProductCategoryId()).orElseThrow(NotFoundByIdException::new);
        Category wishCategory = categoryRepository.findById(postSaveRequest.getWishCategoryId()).orElseThrow(NotFoundByIdException::new);
        memberRepository.findById(currentMemberId).orElseThrow(NotFoundByIdException::new);
        if(!member.getId().equals(currentMemberId)) {
            throw new NotMatchingAuthorException("요청자가 현재 로그인한 유저가 아닙니다");
        }
        List<String> tagNames = postSaveRequest.getTagNames();
        for (String tagName : tagNames) {
            List<PostTag> postTags = postTagRepository.findByTagName(tagName);
            resultsList.addAll(postTags);
        }

        // 태그 명은 중복될 수 없으니 resultsList에 대해서는 중복 제거를 진행하지 않겠다.

        Post post = postSaveRequest.toEntity(member, productCategory, wishCategory, resultsList);
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
            PostInfoDto responseEach = post.toInfoDto();
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
            PostInfoDto responseEach = post.toInfoDto();
            response.add(responseEach);
        }
        return response;
    }

    @Transactional(readOnly = true)
    public List<PostInfoDto> findPostsWithConditions(String productCategoryName, String wishCategoryName, List<String> tagNames) {
        List<Post> resultPostList = new ArrayList<>();
        List<PostInfoDto> response = new ArrayList<>();

        if(!tagNames.isEmpty()) {
            // 카테고리 정보는 없고 태그로만 검색하는 경우
            if(productCategoryName.equals("") && wishCategoryName.equals("")) {
                for(int i=0; i<tagNames.size(); i++) {
                    List<PostTag> postTags = postTagRepository.findByTagName(tagNames.get(i));
                    List<Post> posts = postRepository.findByPostTagsIn(postTags);
                    if(i == 0) {
                        resultPostList.addAll(posts);
                    } else {
                        resultPostList.retainAll(posts);
                    }
                }
                // 올린 상품 카테고리 정보와 태그만 있고, 희망 상품 카테고리 정보는 없이 검색한 경우
            } else if(!productCategoryName.equals("") && wishCategoryName.equals("")) {
                for(int i=0; i<tagNames.size(); i++) {
                    List<PostTag> postTags = postTagRepository.findByTagName(tagNames.get(i));
                    List<Post> posts = postRepository.findByProductCategoryNameAndPostTagsIn(productCategoryName, postTags);
                    if(i == 0) {
                        resultPostList.addAll(posts);
                    } else {
                        resultPostList.retainAll(posts);
                    }
                }
                // 올린 상품 카테고리 정보는 없고, 희망 상품 카테고리 정보와 태그로만 검색한 경우
            } else if(productCategoryName.equals("") && !wishCategoryName.equals("")) {
                for(int i=0; i<tagNames.size(); i++) {
                    List<PostTag> postTags = postTagRepository.findByTagName(tagNames.get(i));
                    List<Post> posts = postRepository.findByWishCategoryNameAndPostTagsIn(wishCategoryName, postTags);
                    if(i == 0) {
                        resultPostList.addAll(posts);
                    } else {
                        resultPostList.retainAll(posts);
                    }
                }
                // 올린 상품 카테고리와 희망 상품 카테고리, 태그 모두 사용해서 검색한 경우
            } else if(!productCategoryName.equals("") && !wishCategoryName.equals("")) {
                for(int i=0; i<tagNames.size(); i++) {
                    List<PostTag> postTags = postTagRepository.findByTagName(tagNames.get(i));
                    List<Post> posts = postRepository.findByProductCategoryNameAndWishCategoryNameAndPostTagsIn(productCategoryName, wishCategoryName, postTags);
                    if(i == 0) {
                        resultPostList.addAll(posts);
                    } else {
                        resultPostList.retainAll(posts);
                    }
                }
            }

        } else if(tagNames.isEmpty()) {
            // 올린 상품 카테고리와 희망 상품 카테고리, 태그정보 모두 없이 검색한 경우 - 전체 조회 결과로 반환
            if(productCategoryName.equals("") && wishCategoryName.equals("")) {
                List<Post> all = postRepository.findAll();
                resultPostList.addAll(all);
            // 올린 상품 카테고리만 있고 희망 상품 카테고리와 태그는 없이 검색하는 경우
            } else if(!productCategoryName.equals("") && wishCategoryName.equals("")) {
                List<Post> postsFromProductCategoryName = postRepository.findByProductCategoryName(productCategoryName);
                resultPostList.addAll(postsFromProductCategoryName);
                // 올린 상품 카테고리와 태그는 없고 희망 상품 카테고리만 사용하여 검색하는 경우
            } else if(productCategoryName.equals("") && !wishCategoryName.equals("")) {
                List<Post> postsFromWishProductCategoryName = postRepository.findByWishCategoryName(wishCategoryName);
                resultPostList.addAll(postsFromWishProductCategoryName);
            } else if(!productCategoryName.equals("") && !wishCategoryName.equals("")) {
                List<Post> posts = postRepository.findByProductCategoryNameAndWishCategoryName(productCategoryName, wishCategoryName);
                resultPostList.addAll(posts);
            }
        }
            // 지금까지 resultPostList를 위에서 필터링하여 만들었다.
            // 여기서부터는 필터링된 resultPostList를 postInfoDto로 바꿔서 리스트에 추가하는 파트
            for (Post post : resultPostList) {
                PostInfoDto responseEach = post.toInfoDto();
                response.add(responseEach);
            }
        return response;
    }

    // TODO 거래 가능한 게시글만 검색하기

    @Transactional(readOnly = true)
    public PostInfoDto findPostById(Long id) {
        Post post = postRepository.findById(id).orElseThrow(NotFoundByIdException::new);
//        // TODO 거래 가능 상태인지 확인하기
//        Trade trade = tradeRepository.findByPostId(post.getId()).orElseThrow(NotFoundByIdException::new);
        PostInfoDto response = post.toInfoDto();
        return response;
    }

    /* TODO
        U : 게시글 업데이트
     */

    @Transactional
    public PostInfoDto updatePost(UpdatePostRequest updatePostRequest, Long memberId) {
        Post post = postRepository.findById(updatePostRequest.getPostId()).orElseThrow(NotFoundByIdException::new);
        // 게시글 변경 정보가 기존이랑 똑같다면 (변화가 없다면) 예외를 터트리려 했는데, 그럴 필요가 없어보여서 일단은 검증하지 않겠다
        // 근데 또 예외는 던져놓고 처리를 안하는 방법도 있으니 이건 피드백을 받아 볼 예정
        // post.updatePostInfos(updatePostRequest);
        Category productCategory = categoryRepository.findById(updatePostRequest.getProductCategoryId()).orElseThrow(NotFoundByIdException::new);
        Category wishCategory = categoryRepository.findById(updatePostRequest.getWishCategoryId()).orElseThrow(NotFoundByIdException::new);
        post.updatePostInfos(updatePostRequest, productCategory, wishCategory);

        PostInfoDto response = post.toInfoDto();
        return response;
    }

    @Transactional
    public String deletePost(DeletePostRequest deletePostRequest, Long memberId) {
        // 먼저 해당 게시글이 존재하는지 검증
        Post post = postRepository.findById(deletePostRequest.getPostId()).orElseThrow(NotFoundByIdException::new);
        // 그 후 작성자가 요청자와 동일인물인지 검증
        Member author = post.getAuthor();
        // TODO Id로만 비교하는게 좀 걸린다. 그렇다고 비밀번호 검증은 너무 투머치 같기도 하다
        if(!author.getId().equals(deletePostRequest.getRequesterId())) {
            throw new NotMatchingAuthorException("게시글 작성자가 아닙니다.");
        }
        postRepository.deleteById(deletePostRequest.getPostId());
        return "DELETE";
    }

}
