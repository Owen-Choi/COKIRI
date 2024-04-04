package f3f.dev1.domain.member.application;

import f3f.dev1.domain.member.dto.MemberDTO.CodeConfirmDto;
import f3f.dev1.domain.member.dto.MemberDTO.EmailConfirmCodeDto;
import f3f.dev1.domain.member.exception.EmailCertificationExpireException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static f3f.dev1.global.common.constants.EmailConstants.EMAIL_CERTIFICATION_TIME;
import static javax.mail.Message.RecipientType.TO;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailCertificationService {

    private final JavaMailSender emailSender;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${email.username}")
    private String username;
    private String ePw;

    public MimeMessage createMessage(String to) throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = emailSender.createMimeMessage();
        message.addRecipients(TO, to);
        message.setSubject("COKIRI 이메일 인증"); // 제목

        // 지역변수이기 때문에 StringBuilder를 활용하겠음
        StringBuilder msg = new StringBuilder();
        msg.append("<div style = 'margin:100px;'>");
        msg.append("<h1> 안녕하세요 </h1>");
        msg.append("<h1> 물물 교환 플랫폼 COKIRI 입니다.</h1>");
        msg.append("<br>");
        msg.append("<p>아래 코드를 앱으로 돌아가서 입력해주세요</p>");
        msg.append("<br>");
        msg.append("<div align='center' style = 'border:1px solid black; font-family:verdana';>");
        msg.append("<h3 style = 'color:blue;'>회원가입 인증 코드입니다.</h3>");
        msg.append("<div style='font-style:130%'>");
        msg.append("CODE: <strong>");
        msg.append(ePw).append("</strong><div><br/>");
        msg.append("</div>");
        message.setText(msg.toString(), "utf-8", "html");
        message.setFrom(new InternetAddress(username + "@naver.com", "COKIRI_admin"));
        return message;
    }

    public String createKey() {
        StringBuffer key = new StringBuffer();
        Random random = new Random();

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(3);

            switch (index) {
                case 0:
                    key.append((char) random.nextInt(26) + 97);
                    break;
                case 1:
                    key.append((char) random.nextInt(26) + 65);
                    break;
                case 2:
                    key.append((random.nextInt(10)));
                    break;
            }
        }
        return key.toString();
    }

    // 메일 발송
    public void sendSimpleMessage(String to) throws Exception {
        ePw = createKey();
        MimeMessage message = createMessage(to);
        try {
            emailSender.send(message);
            log.info("secret code = " + ePw);
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            valueOperations.set(to, ePw);
            redisTemplate.expire(to, EMAIL_CERTIFICATION_TIME, TimeUnit.MILLISECONDS);
        } catch (MailException es) {
            log.info(es.getLocalizedMessage());
            throw new IllegalArgumentException(es.getMessage());
        }

    }

    // 코드 검증
    public CodeConfirmDto confirmCode(EmailConfirmCodeDto emailConfirmCodeDto) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String code = valueOperations.get(emailConfirmCodeDto.getEmail());
        if (code == null) {
            throw new EmailCertificationExpireException();
        }
        if (!code.equals(emailConfirmCodeDto.getCode())) {
            return CodeConfirmDto.builder().matches(false).build();
        }
        return CodeConfirmDto.builder().matches(true).build();

    }

}
