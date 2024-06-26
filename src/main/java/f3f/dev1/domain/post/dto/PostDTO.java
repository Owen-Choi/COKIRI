package f3f.dev1.domain.post.dto;

import f3f.dev1.domain.category.model.Category;

import f3f.dev1.domain.member.model.Member;
import f3f.dev1.domain.model.TradeStatus;
import f3f.dev1.domain.post.model.Post;
import f3f.dev1.domain.tag.model.PostTag;
import f3f.dev1.domain.trade.model.Trade;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static f3f.dev1.domain.comment.dto.CommentDTO.*;
import static f3f.dev1.domain.member.dto.MemberDTO.*;
import static f3f.dev1.domain.postImage.dto.PostImageDTO.*;


public class PostDTO {
    // C : Create 담당 DTO

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostSaveRequest {


        private String title;

        private String content;
        private Boolean tradeEachOther;
        @NotNull
        private Long authorId;
        private Long price;
        private String productCategory;
        private String wishCategory;
        @NotNull
        private List<String> tagNames;
        private List<String> images;
        private String thumbnail;


        public Post toEntity(Member author, Category product, Category wish, List<PostTag> postTags) {
            return Post.builder()
                    .thumbnailImgPath(this.thumbnail)
                    .tradeEachOther(tradeEachOther)
                    .productCategory(product)
                    .content(this.content)
                    .wishCategory(wish)
                    .postTags(postTags)
                    .title(this.title)
                    .author(author)
                    .price(price)
                    .build();
        }
    }

    // R : read 담당 DTO들


    // 메인화면에서 조회에 사용될 DTO.
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchPostRequest {
        String productCategory;
        String wishCategory;
        private List<String> tagNames;
        String minPrice;
        String maxPrice;
    }

    // 최적화 문제 및 페이징을 위해 태그는 별도의 검색 로직으로 빼두겠다.
    // 그에 따라 태그를 제외한 검색 요청 DTO를 새로 만들겠음.
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchPostRequestExcludeTag {
        String productCategory;
        String wishCategory;
        String minPrice;
        String maxPrice;
        TradeStatus tradeStatus;
    }

    // U : Update 담당 DTO들

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdatePostRequest {

        private String title;

        private String content;

        private Boolean tradeEachOther;

        private Long authorId;

        private Long price;

        private String productCategory;

        private String wishCategory;

        private List<String> tagNames;

        private List<String> images;

        private String thumbnail;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeletePostRequest {
        @NotNull
        private Long id;
        @NotNull
        private Long authorId;
    }
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    // member, scrap 등 외부에서 사용하는 post 관련 DTO
    public static class PostInfoDto{

        private Long id;
        private String title;

        private String content;

        private Boolean tradeEachOther;

        private String authorNickname;

        private String wishCategory;

        private Long price;

        private String productCategory;

        private TradeStatus tradeStatus;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    // 단순 조회를 위한 가벼운 DTO
    // 당장은 안쓰이는데 일단 유지하겠다.
    public static class PostInfoDtoForGET {
        private Long id;
        private String title;
        private String content;
        private String thumbnail;
        private String authorNickname;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostSearchResponseDto implements Serializable {
        private Long id;
        private String title;
        private String content;
        private String thumbnail;
        private String authorNickname;
        private String productCategory;
        private String createdTime;
        private Long messageRoomCount;
        private String wishCategory;
        private Boolean isScrap;
        private Long scrapCount;
        private Long price;

        public PostSearchResponseDto(Long id, String title, String content, String thumbnail, String authorNickname,
                                     String productCategory, LocalDateTime createdTime, Long messageRoomCount,
                                     String wishCategory, boolean isScrap, Long scrapCount, Long price) {
            this.id = id;
            this.title = title;
            this.price = price;
            this.content = content;
            this.isScrap = isScrap;
            this.thumbnail = thumbnail;
            this.scrapCount = scrapCount;
            this.wishCategory = wishCategory;
            this.authorNickname = authorNickname;
            this.productCategory = productCategory;
            this.messageRoomCount = messageRoomCount;
            this.createdTime = createdTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostInfoDtoWithTag{

        private Long id;
        private String title;

        private String content;

        private Boolean tradeEachOther;

        private String authorNickname;

        private String wishCategory;

        private String productCategory;

        private Long price;

        private TradeStatus tradeStatus;

        private List<String> tagNames;

        private List<String> images;

        private String thumbnail;

        private Long scrapCount;

        private Long messageRoomCount;

        private LocalDateTime createdTime;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SinglePostInfoDto{

        private Long id;
        private String title;

        private String content;

        private Boolean tradeEachOther;

        private UserInfoWithAddress userInfoWithAddress;

        private String wishCategory;

        private String productCategory;

        private Long price;

        private TradeStatus tradeStatus;

        private List<CommentInfoDto> commentInfoDtoList;

        private List<String> tagNames;

        private List<String> images;

        private Long scrapCount;

        private Long messageRoomCount;

        private LocalDateTime createdTime;

        private boolean isScrap;
    }


}
