package f3f.dev1.domain.member.api;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import f3f.dev1.domain.member.application.AuthService;
import f3f.dev1.domain.member.application.EmailCertificationService;
import f3f.dev1.domain.member.application.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static f3f.dev1.domain.member.dto.MemberDTO.*;
import static f3f.dev1.domain.token.dto.TokenDTO.AccessTokenDTO;
import static f3f.dev1.domain.token.dto.TokenDTO.TokenIssueDTO;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberAuthController {
    private final MemberService memberService;

    private final EmailCertificationService emailCertificationService;

    private final AuthService authService;

    private final AmazonS3Client amazonS3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 이미지 유저 프로필 사진 업로드
    // TODO authController에 있을 이유가 없다. memberController로 이동 예정
    @PostMapping(value = "/auth/image/profileImage")
    public ResponseEntity<ImageUrlDto> profileUpload(MultipartFile[] imageFiles) throws IOException {
        List<String> imagePathList = new ArrayList<>();
        for (MultipartFile multipartFile : imageFiles) {
            String originalName = multipartFile.getOriginalFilename(); // 파일 이름
            long size = multipartFile.getSize(); // 파일 크기

            ObjectMetadata objectMetaData = new ObjectMetadata();
            objectMetaData.setContentType(multipartFile.getContentType());
            objectMetaData.setContentLength(size);

            // S3에 업로드
            amazonS3Client.putObject(
                    new PutObjectRequest(
                            bucket,
                            originalName, multipartFile.getInputStream(),
                            objectMetaData
                    ).withCannedAcl(CannedAccessControlList.PublicRead)
            );

            String imagePath = amazonS3Client.getUrl(bucket, originalName).toString(); // 접근가능한 URL 가져오기
            imagePathList.add(imagePath);
        }

        return ResponseEntity.ok(ImageUrlDto.builder().imageUrls(imagePathList).build());
    }

    // 게시글 이미지 업로드
    // TODO authController에 있을 이유가 없다. postController로 이동 예정
    @PostMapping(value = "/auth/image/postImage")
    public ResponseEntity<ImageUrlDto> postUpload(MultipartFile[] imageFiles) throws IOException {
        List<String> imagePathList = new ArrayList<>();
//        String S3Bucket = "cokiri-image/image/postImage";
        for (MultipartFile multipartFile : imageFiles) {
            String originalName = multipartFile.getOriginalFilename(); // 파일 이름
            long size = multipartFile.getSize(); // 파일 크기

            ObjectMetadata objectMetaData = new ObjectMetadata();
            objectMetaData.setContentType(multipartFile.getContentType());
            objectMetaData.setContentLength(size);

            // S3에 업로드
            amazonS3Client.putObject(
                    new PutObjectRequest(
                            bucket,
                            originalName, multipartFile.getInputStream(),
                            objectMetaData
                    ).withCannedAcl(CannedAccessControlList.PublicRead)
            );

            String imagePath = amazonS3Client.getUrl(bucket, originalName).toString(); // 접근가능한 URL 가져오기
            imagePathList.add(imagePath);
        }

        return ResponseEntity.ok(ImageUrlDto.builder().imageUrls(imagePathList).build());
    }

    // 이메일 중복 확인
    @PostMapping(value = "/auth/check-email")
    public ResponseEntity<RedunCheckDto> emailDuplicateCheck(@RequestBody CheckEmailDto checkEmailDto) {
        return ResponseEntity.ok(memberService.existsByEmail(checkEmailDto.getEmail()));
    }

    // 닉네임 중복 확인
    @PostMapping(value = "/auth/check-nickname")
    public ResponseEntity<RedunCheckDto> nicknameDuplicateCheck(@RequestBody CheckNicknameDto checkNicknameDto) {
        return ResponseEntity.ok(memberService.existsByNickname(checkNicknameDto.getNickname()));
    }

    // 전화번호 중복 확인
    @PostMapping(value = "/auth/check-phone")
    public ResponseEntity<RedunCheckDto> phoneNumberDuplicateCheck(@RequestBody CheckPhoneNumberDto checkPhoneNumberDto) {
        return ResponseEntity.ok(memberService.existsByPhoneNumber(checkPhoneNumberDto.getPhoneNumber()));

    }

    // 이메일 인증 요청
    @PostMapping(value = "/auth/mailConfirm")
    public ResponseEntity<EmailSentDto> mailConfirm(@RequestBody ConfirmEmailDto confirmEmailDto) throws Exception {

        emailCertificationService.sendSimpleMessage(confirmEmailDto.getEmail());
        return ResponseEntity.ok(EmailSentDto.builder().email(confirmEmailDto.getEmail()).success(true).build());
    }

    // 코드 인증 요청
    @PostMapping(value = "/auth/codeConfirm")
    public ResponseEntity<CodeConfirmDto> codeConfirm(@RequestBody EmailConfirmCodeDto emailConfirmCodeDto) {
        return ResponseEntity.ok(emailCertificationService.confirmCode(emailConfirmCodeDto));

    }

    // 이메일 찾기
    // TODO: 던지는 예외 메시지도 한번 다듬어야될 것 같음, 같은 예외 재사용이 많아서 비밀번호 찾기에서 유저 못 찾았는데 ID 비밀번호 확인하라는 메시지가 출력됨
    @PostMapping(value = "/auth/find/email")
    public ResponseEntity<EncryptEmailDto> findEmail(@RequestBody FindEmailDto findEmailDto) {
        EncryptEmailDto userEmail = memberService.findUserEmail(findEmailDto);
        return new ResponseEntity<>(userEmail, HttpStatus.OK);
    }

    // 비밀번호 찾기
    @PostMapping(value = "/auth/find/password")
    public ResponseEntity<ReturnPasswordDto> findPassword(@RequestBody FindPasswordDto findPasswordDto) {
        ReturnPasswordDto userPassword = memberService.findUserPassword(findPasswordDto);
        return new ResponseEntity<>(userPassword, HttpStatus.OK);

    }

    // 회원가입
    @PostMapping(value = "/auth/signup")
    public ResponseEntity<String> signUp(@RequestBody SignUpRequest signUpRequest) {
        return new ResponseEntity<>(authService.signUp(signUpRequest), HttpStatus.CREATED);
    }

    // 로그인 - TODO 크롬 자동 로그인 이용하려면 form data 형식도 열어놔야할듯
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserLoginDto> loginJson(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<UserLoginDto> loginForm(LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }


    // 로그아웃 리다이렉트 페이지
    @GetMapping(value = "/logout-redirect")
    public ResponseEntity<String> loginRedirect() {
        return ResponseEntity.ok("LOGOUT");
    }

    // 재발급
    @PostMapping(value = "/auth/reissue")
    public ResponseEntity<TokenIssueDTO> reissue(@RequestBody AccessTokenDTO accessTokenDTO) {
        return ResponseEntity.ok(authService.reissue(accessTokenDTO));
    }


}
