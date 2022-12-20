package f3f.dev1.domain.message.model;

import f3f.dev1.domain.model.BaseTimeEntity;
import f3f.dev1.domain.post.model.Post;
import f3f.dev1.domain.member.model.Member;
import f3f.dev1.domain.trade.model.Trade;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class MessageRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "messageRoom_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name="seller_id")
    private Member seller;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private Member buyer;

    @OneToMany(mappedBy = "messageRoom", fetch = FetchType.LAZY)
    private List<Message> messages = new ArrayList<>();

    @Builder
    public MessageRoom(Long id, Post post, Member seller, Member buyer) {
        this.id = id;
        this.post = post;
        this.seller = seller;
        this.buyer = buyer;
    }
}
