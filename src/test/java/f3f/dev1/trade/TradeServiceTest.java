package f3f.dev1.trade;

import f3f.dev1.domain.member.application.AuthService;
import f3f.dev1.domain.member.model.Member;
import f3f.dev1.domain.model.Address;
import f3f.dev1.domain.post.application.PostService;
import f3f.dev1.domain.post.dto.PostDTO.PostSaveRequest;
import f3f.dev1.domain.trade.application.TradeService;
import f3f.dev1.domain.trade.dao.TradeRepository;
import f3f.dev1.domain.trade.dto.TradeDTO.CreateTradeDto;
import f3f.dev1.domain.trade.model.Trade;
import f3f.dev1.domain.member.application.MemberService;
import f3f.dev1.domain.member.dao.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static f3f.dev1.domain.member.dto.MemberDTO.*;

@Transactional
@SpringBootTest
public class TradeServiceTest {
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TradeRepository tradeRepository;

    @Autowired
    MemberService memberService;

    @Autowired
    AuthService authService;

    @Autowired
    PostService postService;

    @Autowired
    TradeService tradeService;

    // 주소 오브젝트 생성
    public Address createAddress() {
        return Address.builder()
                .addressName("address")
                .postalAddress("13556")
                .latitude("37.49455")
                .longitude("127.12170")
                .build();
    }

    // 회원가입 DTO 생성 메소드
    public SignUpRequest createSignUpRequest(String email, String phoneNumber) {
        return SignUpRequest.builder()
                .userName("username")
                .nickname("nickname")
                .phoneNumber(phoneNumber)
                .email(email)
                .address(createAddress())
                .password("password")
                .build();
    }

    // 로그인 DTO 생성 메소드
    public LoginRequest createLoginRequest(String email) {
        return LoginRequest.builder()
                .email(email)
                .password("password").build();
    }

    // 포스트 생성 DTO 생성 메소드
    public PostSaveRequest createPostSaveRequest(Member author) {

        return new PostSaveRequest(1L, "title", "content", false, author, null, null);
    }

    // 트레이드 생성 DTO 생성 메소드
    public CreateTradeDto createTradeDto(Long sellerId, Long buyerId, Long postId) {
        return CreateTradeDto.builder()
                .sellerId(sellerId)
                .buyerId(buyerId)
                .postId(postId).build();
    }
    @Test
    @DisplayName("트레이드 생성 성공 테스트")
    public void createScrapTestSuccess() throws Exception {
        //given
        SignUpRequest signUpRequest1 = createSignUpRequest("testuser1@email.com", "01012345678");
        SignUpRequest signUpRequest2 = createSignUpRequest("testuser2@email.com", "01056781234");
        authService.signUp(signUpRequest1);
        authService.signUp(signUpRequest2);
        Long userId1 = memberRepository.findByEmail("testuser1@email.com").get().getId();
        Long userId2 = memberRepository.findByEmail("testuser2@email.com").get().getId();
        PostSaveRequest postSaveRequest = createPostSaveRequest(memberRepository.findById(userId1).get());
        System.out.println(memberRepository.findById(userId1).get().getNickname());;
        Long postId = postService.savePost(postSaveRequest);


        // when
        CreateTradeDto tradeDto = createTradeDto(userId1, userId2, postId);
        Long tradeId = tradeService.createTrade(tradeDto);
        Optional<Trade> byId = tradeRepository.findById(tradeId);

        // then
        assertArrayEquals(new Long[]{userId1, userId2, postId}, new Long[]{byId.get().getSeller().getId(), byId.get().getBuyer().getId(), byId.get().getPost().getId()});

    }

}
