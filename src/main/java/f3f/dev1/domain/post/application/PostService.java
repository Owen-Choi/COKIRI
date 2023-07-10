package f3f.dev1.domain.post.application;

import com.amazonaws.services.s3.AmazonS3Client;
import f3f.dev1.domain.category.dao.CategoryRepository;
import f3f.dev1.domain.category.exception.NotFoundProductCategoryNameException;
import f3f.dev1.domain.category.exception.NotFoundWishCategoryNameException;
import f3f.dev1.domain.category.model.Category;
import f3f.dev1.domain.comment.dao.CommentRepository;
import f3f.dev1.domain.comment.model.Comment;
import f3f.dev1.domain.member.dao.MemberCustomRepositoryImpl;
import f3f.dev1.domain.member.dao.MemberRepository;
import f3f.dev1.domain.member.exception.NotAuthorizedException;
import f3f.dev1.domain.member.model.Member;
import f3f.dev1.domain.model.TradeStatus;
import f3f.dev1.domain.post.dao.PostCustomRepositoryImpl;
import f3f.dev1.domain.post.dao.PostRepository;
import f3f.dev1.domain.post.exception.NotContainAuthorInfoException;
import f3f.dev1.domain.post.exception.NotMatchingAuthorException;
import f3f.dev1.domain.post.model.Post;
import f3f.dev1.domain.postImage.application.PostImageService;
import f3f.dev1.domain.postImage.model.PostImage;
import f3f.dev1.domain.scrap.dao.ScrapPostRepository;
import f3f.dev1.domain.tag.application.PostTagService;
import f3f.dev1.domain.tag.application.TagService;
import f3f.dev1.domain.tag.dao.PostTagRepository;
import f3f.dev1.domain.tag.model.PostTag;
import f3f.dev1.domain.trade.dao.TradeRepository;
import f3f.dev1.domain.trade.model.Trade;
import f3f.dev1.global.error.exception.NotFoundByIdException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static f3f.dev1.domain.comment.dto.CommentDTO.CommentInfoDto;
import static f3f.dev1.domain.member.dto.MemberDTO.GetUserPost;
import static f3f.dev1.domain.member.dto.MemberDTO.UserInfoWithAddress;
import static f3f.dev1.domain.post.dto.PostDTO.*;
import static f3f.dev1.domain.trade.dto.TradeDTO.CreateTradeDto;
import static f3f.dev1.global.common.constants.RedisCacheConstants.POST_LIST_WITHOUT_TAG;
import static f3f.dev1.global.common.constants.RedisCacheConstants.POST_LIST_WITH_TAG;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final TradeRepository tradeRepository;
    private final CommentRepository commentRepository;
    private final ScrapPostRepository scrapPostRepository;
    private final CategoryRepository categoryRepository;
    private final PostTagRepository postTagRepository;

    private final TagService tagService;
    private final PostImageService postImageService;
    private final PostTagService postTagService;

    // Custom repository
    private final PostCustomRepositoryImpl postCustomRepository;
    private final MemberCustomRepositoryImpl memberCustomRepository;

    private final AmazonS3Client amazonS3Client;

    // TODO 게시글 사진 개수 제한 걸기
    @Transactional
    public Long savePost(PostSaveRequest postSaveRequest, Long currentMemberId) {

        Member member = memberRepository.findById(postSaveRequest.getAuthorId()).orElseThrow(NotFoundByIdException::new);
        List<PostTag> resultsList = new ArrayList<>();
        Category productCategory = categoryRepository.findCategoryByName(postSaveRequest.getProductCategory()).orElseThrow(NotFoundProductCategoryNameException::new);
        Category wishCategory = categoryRepository.findCategoryByName(postSaveRequest.getWishCategory()).orElseThrow(NotFoundWishCategoryNameException::new);
        memberRepository.findById(currentMemberId).orElseThrow(NotFoundByIdException::new);

        Post post = postSaveRequest.toEntity(member, productCategory, wishCategory, resultsList);
//        member.getPosts().add(post);
        postRepository.save(post);

        Trade trade = CreateTradeDto.builder().sellerId(member.getId()).postId(post.getId()).build().toEntity(member, post);
        tradeRepository.save(trade);

        tagService.addTagsToPost(post.getId(), postSaveRequest.getTagNames());

        if(postSaveRequest.getImages() != null) {
            List<String> images = postSaveRequest.getImages();
            postImageService.savePostImages(images, post.getId());
        }
        return post.getId();
    }

    @Transactional(readOnly = true)
    public Page<PostSearchResponseDto> findOnlyWithTradeStatus (Long currentMemberId, TradeStatus tradeStatus, Pageable pageable) {
        Page<Post> all = postCustomRepository.findPostsWithTradeStatus(tradeStatus, pageable);
        List<PostSearchResponseDto> resultList = new ArrayList<>();
        if(currentMemberId != null) {
            Member member = memberRepository.findById(currentMemberId).orElseThrow(NotFoundByIdException::new);
            for (Post post : all) {
                boolean isScrap = scrapPostRepository.existsByScrapIdAndPostId(member.getScrap().getId(), post.getId());
                resultList.add(post.toSearchResponseDto((long)post.getMessageRooms().size(), (long)post.getScrapPosts().size(),isScrap));
            }
        } else {
            for (Post post : all) {
                resultList.add(post.toSearchResponseDto((long) post.getMessageRooms().size(), (long) post.getScrapPosts().size(), false));
            }
        }
        return new PageImpl<>(resultList, pageable, all.getTotalElements());
    }


//    @Cacheable(value = AUTHOR_POST_LIST, key = "#authorId")
    @Transactional(readOnly = true)
    public Page<GetUserPost> findPostByAuthorId(Long authorId, Pageable pageable) {
//        List<GetUserPost> collect = postRepository.getUserPostById(authorId, pageable).stream().map(GetUserPost::new).collect(Collectors.toList());
        Page<PostRepository.GetUserPostInterface> userPostById = postRepository.getUserPostById(authorId, pageable);
        List<GetUserPost> collect = userPostById.stream().map(GetUserPost::new).collect(Collectors.toList());
        return new PageImpl<>(collect, pageable, userPostById.getTotalElements());
    }

    // TODO 고려해야할 것 : 캐싱 동기화(sync), 비회면 조건부 캐싱
    // TODO 쿼리DSL에서 Enum 클래스의 세부 필드를 where절에서 비교할 수 없다. 따라서 서비스 로직 (자바코드) 단에서 직접 거래 가능 여부를 체크해줘야 할 것 같다.
    // + 캐시 만료, 삭제 시점
    // pageable 관련 key값은 현재 페이지 수만 추가해뒀다. 각 페이지마다 보여주는 데이터의 수가 같아야만 한다.
    @Cacheable(value = POST_LIST_WITHOUT_TAG, key = "#request.productCategory + '_' + #request.wishCategory + '_' + #request.minPrice + '_' + #request.maxPrice + '_' + 'p' + #pageable.getPageNumber()")
    @Transactional(readOnly = true)
    public Page<PostSearchResponseDto> findPostsByCategoryAndPriceRange(SearchPostRequestExcludeTag request, Long currentMemberId, Pageable pageable) {
        List<PostSearchResponseDto> list = new ArrayList<>();
        Page<Post> dtoPages = postCustomRepository.findPostDTOByConditions(request, pageable);
        // 조회하는 사용자가 로그인된 회원인 경우
        if(currentMemberId != null) {
            Member member = memberRepository.findById(currentMemberId).orElseThrow(NotFoundByIdException::new);
            for (Post post : dtoPages) {
                if(member.getScrap() != null) {
                    boolean isScrap = scrapPostRepository.existsByScrapIdAndPostId(member.getScrap().getId(), post.getId());
                    PostSearchResponseDto build = post.toSearchResponseDto((long)post.getMessageRooms().size(), (long)post.getScrapPosts().size(), isScrap);
                    list.add(build);
                } else {
                    PostSearchResponseDto build = post.toSearchResponseDto((long)post.getMessageRooms().size(), (long)post.getScrapPosts().size(), false);
                    list.add(build);
                }

            }
        }
        // 조회하는 사용자가 비회원일 경우
        else {
            for (Post post : dtoPages) {
                PostSearchResponseDto build = post.toSearchResponseDto((long)post.getMessageRooms().size(), (long)post.getScrapPosts().size(), false);
                list.add(build);
            }
        }
        return new PageImpl<>(list, pageable, dtoPages.getTotalElements());
    }

    @Cacheable(value = POST_LIST_WITH_TAG, keyGenerator = "customKeyGenerator")
    @Transactional(readOnly = true)
    public Page<PostSearchResponseDto> findPostsWithTagNameList(List<String> tagNames, Long currentMemberId, TradeStatus tradeStatus, Pageable pageable) {
        Page<Post> dtoList = postCustomRepository.findPostsByTags(tagNames, tradeStatus, pageable);
        List<PostSearchResponseDto> resultList = new ArrayList<>();
        if(currentMemberId != null) {
            Member member = memberRepository.findById(currentMemberId).orElseThrow(NotFoundByIdException::new);
            for (Post post : dtoList) {
                boolean isScrap = scrapPostRepository.existsByScrapIdAndPostId(member.getScrap().getId(), post.getId());
                PostSearchResponseDto build = post.toSearchResponseDto((long)post.getMessageRooms().size(), (long)post.getScrapPosts().size(), isScrap);
                resultList.add(build);
            }
        } else {
            for (Post post : dtoList) {
                PostSearchResponseDto build = post.toSearchResponseDto((long)post.getMessageRooms().size(), (long)post.getScrapPosts().size(), false);
                resultList.add(build);
            }
        }
        return new PageImpl<>(resultList, pageable, dtoList.getTotalElements());
    }

    @Transactional(readOnly = true)
    public SinglePostInfoDto findPostById(Long id, Long currentMemberId) {
        Post post = postRepository.findById(id).orElseThrow(NotFoundByIdException::new);
        // TODO 거래 가능 상태인지 확인하기
        List<String> tagNames = new ArrayList<>();
        List<PostTag> postTags = postTagRepository.findByPost(post);
        for (PostTag postTag : postTags) {
            tagNames.add(postTag.getTag().getName());
        }

        List<Comment> comments = commentRepository.findByPostId(post.getId());
        List<CommentInfoDto> commentInfoDtoList = new ArrayList<>();
        for (Comment comment : comments) {
            commentInfoDtoList.add(comment.toInfoDto());
        }

        UserInfoWithAddress userInfo = memberCustomRepository.getUserInfo(post.getAuthor().getId());
        List<String> postImages = new ArrayList<>();
        for (PostImage postImage : post.getPostImages()) {
            postImages.add(postImage.getImgPath());
        }

        // 비회원일 경우 member를 조회하면 오류가 발생한다. 따라서 null 여부를 체크하고, 이에 따라 로직이 분기해야 한다.
        if(currentMemberId != null) {
            Member member = memberRepository.findById(currentMemberId).orElseThrow(NotFoundByIdException::new);
            // 프론트의 요청으로 로그인한 사용자가 조회한 게시글을 스크랩했을 경우 표시를 해줘야 한다.
            // 하지만 사용자의 스크랩이 존재하지 않은 경우 (이론상 불가능한 경우지만) 이에 대한 예외도 처리 해주어야 한다.
            if(member.getScrap() != null) {
                boolean isScrap = scrapPostRepository.existsByScrapIdAndPostId(member.getScrap().getId(), id);
                SinglePostInfoDto response = post.toSinglePostInfoDto(tagNames, (long) post.getScrapPosts().size(), (long) post.getMessageRooms().size(), userInfo, commentInfoDtoList, postImages, isScrap);
                return response;
            }
        }
        SinglePostInfoDto response = post.toSinglePostInfoDto(tagNames, (long) post.getScrapPosts().size(), (long) post.getMessageRooms().size(), userInfo, commentInfoDtoList, postImages, false);
        return response;
    }

    /* TODO
        U : 게시글 업데이트
     */

    @Transactional
    public void updatePostWithPatch(UpdatePostRequest updatePostRequest, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(NotFoundByIdException::new);


        if(updatePostRequest.getTagNames() != null) {
            postTagService.updatePostTagWithPatch(postId, updatePostRequest);
        }
        if(updatePostRequest.getImages() != null) {
            postImageService.updatePostImagesWithPatch(postId, updatePostRequest.getImages());
        }

        if(updatePostRequest.getAuthorId() == null) {
            throw new NotContainAuthorInfoException();
        }

        if(!post.getAuthor().getId().equals(updatePostRequest.getAuthorId())) {
            throw new NotMatchingAuthorException("게시글 작성자가 아닙니다.");
        }

        /*
            프론트와 의견 상충 과정에서 아래와 같은 코드 구조가 탄생함.
            프론트는 특정 필드의 변경사항을 인지하고 바디에 추가하는게 소요가 크다고 했고,
            나는 불필요한 쿼리의 수를 최대한 줄이고싶었다.
            그래서 결과적으로 PUT도 PATCH도 아닌 업데이트 메서드가 구현됨 :
            수정된 필드 뿐만 아니라 수정되지 않은 필드로 함께 바디로 넘어온다.
            하지만 그 중에서 변동사항이 있는 값만 수정된다.

            이렇게 여러번 쿼리를 별도로 날리는 것 보다 한번에 일괄로 처리하는게 나으려나?
         */

        if(updatePostRequest.getTitle() != null && !post.getTitle().equals(updatePostRequest.getTitle())) {
            post.updateTitle(updatePostRequest.getTitle());
        }

        if(updatePostRequest.getPrice() != null && !post.getPrice().equals(updatePostRequest.getPrice())) {
            post.updatePrice(updatePostRequest.getPrice());
        }

        if(updatePostRequest.getProductCategory() != null && !post.getProductCategory().getName().equals(updatePostRequest.getProductCategory())) {
            Category productCategory = categoryRepository.findCategoryByName(updatePostRequest.getProductCategory()).orElseThrow(NotFoundByIdException::new);
            post.updateProductCategory(productCategory);
        }

        if(updatePostRequest.getWishCategory() != null && !post.getWishCategory().getName().equals(updatePostRequest.getWishCategory())) {
            Category wishCategory = categoryRepository.findCategoryByName(updatePostRequest.getWishCategory()).orElseThrow(NotFoundByIdException::new);
            post.updateWishCategory(wishCategory);
        }

        if(updatePostRequest.getThumbnail() != null && !post.getThumbnailImgPath().equals(updatePostRequest.getThumbnail())) {
            String thumbnail = updatePostRequest.getThumbnail();
            post.updateThumbnail(thumbnail);
        }

        //의성 추가
        if(updatePostRequest.getPrice() != null && !post.getPrice().equals(updatePostRequest.getPrice())) {
            post.updatePrice(updatePostRequest.getPrice());
        }
        //의성 추가
        if(updatePostRequest.getContent() != null && !post.getContent().equals(updatePostRequest.getContent())) {
            post.updateContent(updatePostRequest.getContent());
        }

    }

    @Transactional
    public String deletePost(DeletePostRequest deletePostRequest, Long currentMemberId) {
        // 먼저 해당 게시글이 존재하는지 검증
        Post post = postRepository.findById(deletePostRequest.getId()).orElseThrow(NotFoundByIdException::new);
        // 그 후 작성자가 요청자와 동일인물인지 검증
        Member author = post.getAuthor();
        if(!author.getId().equals(currentMemberId)) {
            throw new NotAuthorizedException("요청자가 현재 로그인한 유저가 아닙니다");
        }
        if(!author.getId().equals(deletePostRequest.getAuthorId())) {
            throw new NotMatchingAuthorException("게시글 작성자가 아닙니다.");
        }

        deletePostImage(post.getPostImages());
        postRepository.delete(post);
        return "DELETE";
    }

    // 캐시 삭제 스케쥴러 등록
    // 고민이 된다. tag 없이 조회한 모든 게시글 캐시가 5초 단위로 다 지워지는데, 더 나은 방법이 있을 것만 같은 느낌이다.
    @CacheEvict(value = POST_LIST_WITHOUT_TAG, allEntries = true)
    @Scheduled(fixedDelay = 5 * 1000)   // 5초마다 호출
    public void removePostWithoutTagCache() {
    }

    @CacheEvict(value = POST_LIST_WITH_TAG, allEntries = true)
    @Scheduled(fixedDelay = 5 * 1000)   // 5초마다 호출
    public void removePostWithTagCache() {
    }

//    @CacheEvict(value = AUTHOR_POST_LIST, allEntries = true)
//    @Scheduled(fixedDelay = 5 * 1000)   // 5초마다 호출
//    public void removeAuthorPostListCache() {
//    }

    private void deletePostImage(List<PostImage> images) {
        String s3Bucket = "cokiri-image/image/profileImage";
        for (PostImage image : images) {
            String key = image.getImgPath().substring(image.getImgPath().lastIndexOf("/") + 1);
            amazonS3Client.deleteObject(s3Bucket, key);
        }

    }

}
