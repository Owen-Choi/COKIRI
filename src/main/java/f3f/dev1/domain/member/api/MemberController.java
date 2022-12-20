package f3f.dev1.domain.member.api;

import f3f.dev1.domain.member.application.AuthService;
import f3f.dev1.domain.member.application.MemberService;
import f3f.dev1.domain.member.dao.MemberRepository;
import f3f.dev1.domain.member.model.Member;
import f3f.dev1.domain.model.Address;
import f3f.dev1.global.error.exception.NotFoundByIdException;
import f3f.dev1.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static f3f.dev1.domain.member.dto.MemberDTO.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    private final MemberRepository memberRepository;

    private final AuthService authService;

    // 유저 정보 조회
    @GetMapping(value = "/user")
    public ResponseEntity<UserInfo> getUserInfo() {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Member member = memberRepository.findById(currentMemberId).orElseThrow(NotFoundByIdException::new);
        return ResponseEntity.ok(member.toUserInfo());

    }

    // 유저 정보 수정
    @PatchMapping(value = "/user")
    public ResponseEntity<UserInfo> updateUserInfo(@RequestBody UpdateUserInfo updateUserInfo) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(memberService.updateUserInfo(updateUserInfo, currentMemberId));
    }

    // 유저 삭제
    @DeleteMapping(value = "/user")
    public ResponseEntity<String> deleteUser() {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(memberService.deleteUser(currentMemberId));

    }

    // 유저 비밀번호 변경
    @PatchMapping(value = "/user/password")
    public ResponseEntity<String> updateUserPassword(@RequestBody UpdateUserPassword updateUserPassword) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(memberService.updateUserPassword(updateUserPassword, currentMemberId));

    }

    // 유저 주소 업데이트
    @PatchMapping(value = "/user/address")
    public ResponseEntity<Address> updateUserAddress(@RequestBody Address address) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        memberService.updateUserAddress(address, currentMemberId);
        return ResponseEntity.ok(address);
    }

    @DeleteMapping("/user/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) throws IOException {

        authService.logout(request, response);
        return ResponseEntity.ok().build();
    }

    // TODO 주소 업데이트 요청 처리 경로 만들어야함, 서비스도

    @GetMapping("/user/posts")
    public ResponseEntity<GetUserPostDto> getUserPosts() {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(memberService.getUserPostDto(currentMemberId));
    }

    @GetMapping("/user/messagerooms")
    public ResponseEntity<GetUserMessageRoomDto> getUserMessageRooms() {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(memberService.getUserMessageRoomDto(currentMemberId));
    }
}