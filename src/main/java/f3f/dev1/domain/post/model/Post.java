package f3f.dev1.domain.post.model;

import f3f.dev1.domain.category.model.Category;
import f3f.dev1.domain.comment.model.Comment;
import f3f.dev1.domain.member.model.Member;
import f3f.dev1.domain.message.model.MessageRoom;
import f3f.dev1.domain.model.BaseTimeEntity;
import f3f.dev1.domain.model.TradeStatus;
import f3f.dev1.domain.post.dto.PostDTO;
import f3f.dev1.domain.tag.model.PostTag;
import f3f.dev1.domain.trade.model.Trade;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static f3f.dev1.domain.post.dto.PostDTO.*;

@Getter
@NoArgsConstructor
@Entity
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue
    @Column(name = "post_id")
    private Long id;

    private String title;

    private String content;

    // 끼리끼리 거래 여부
    private Boolean tradeEachOther;

    @OneToOne(mappedBy = "post")
    private Trade trade;

    @ManyToOne
    @JoinColumn(name = "productCategory_id")
    private Category productCategory;

    @ManyToOne
    @JoinColumn(name = "wishCategory_id")
    private Category wishCategory;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member author;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<MessageRoom> messageRooms = new ArrayList<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<PostTag> postTags = new ArrayList<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<ScrapPost> scrapPosts = new ArrayList<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Comment> comments = new ArrayList<>();

    public void updatePostInfos(UpdatePostRequest updatePostRequest) {
        this.title = updatePostRequest.getTitle();
        this.content = updatePostRequest.getContent();
        this.postTags = updatePostRequest.getPostTags();
        this.productCategory = updatePostRequest.getProductCategory();
        this.wishCategory = updatePostRequest.getWishCategory();
    }

    @Builder
    public Post(Long id, String title, String content, Boolean tradeEachOther, Category productCategory, Category wishCategory, Member author) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.tradeEachOther = tradeEachOther;
        this.productCategory = productCategory;
        this.wishCategory = wishCategory;
        this.author = author;
    }

    public PostInfoDto toInfoDto() {
        return PostInfoDto.builder()
                .id(this.id)
                .authorNickname(this.author.getNickname())
                .content(this.content)
                .title(this.title)
                .productCategory(this.productCategory.getName())
                .wishCategory(this.wishCategory.getName())
                .tradeEachOther(this.tradeEachOther)
                .tradeStatus(this.trade.getTradeStatus())
                .build();
    }


}