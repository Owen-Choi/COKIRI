package f3f.dev1.domain.user.model;

import f3f.dev1.domain.comment.model.Comment;
import f3f.dev1.domain.message.model.Message;
import f3f.dev1.domain.message.model.MessageRoom;
import f3f.dev1.domain.model.Address;
import f3f.dev1.domain.post.model.Post;
import f3f.dev1.domain.scrap.model.Scrap;
import f3f.dev1.domain.trade.model.Trade;

import f3f.dev1.domain.user.dto.UserDTO.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static f3f.dev1.domain.user.dto.UserDTO.*;

@Entity
@Getter
@NoArgsConstructor
public class User extends UserBase {

    @Embedded
    private Address address;

    private String birthDate;

    private String phoneNumber;

    private String userName;

    private String nickname;

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Comment> comments = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE)
    private Scrap scrap;

    @OneToMany(mappedBy = "seller", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<MessageRoom> sellingRooms = new ArrayList<>();

    @OneToMany(mappedBy = "buyer", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<MessageRoom> buyingRooms = new ArrayList<>();

    @OneToMany(mappedBy = "sender", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Message> sendMessages = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Message> receivedMessages = new ArrayList<>();

    @OneToMany(mappedBy = "buyer", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Trade> buyingTrades = new ArrayList<>();

    @OneToMany(mappedBy = "seller", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Trade> sellingTrades = new ArrayList<>();

    @Builder
    public User(Long id, String email, String password,String username, Address address, String birthDate, String phoneNumber, String nickname) {
        super(id, email, password);
        this.userName = username;
        this.address = address;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.nickname = nickname;
    }

    public UserInfo toUserInfo() {
        return UserInfo.builder()
                .address(address)
                .userName(userName)
                .email(getEmail())
                .phoneNumber(phoneNumber)
                .nickname(nickname)
                .build();
    }

    public UpdateUserInfo updateUserInfo(UpdateUserInfo updateUserInfo) {
        address = updateUserInfo.getAddress();
        nickname = updateUserInfo.getNickname();

        return updateUserInfo;
    }
}
