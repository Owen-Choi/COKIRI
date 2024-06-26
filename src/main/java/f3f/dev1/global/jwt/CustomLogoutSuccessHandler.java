package f3f.dev1.global.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler implements LogoutSuccessHandler {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    @Override
    @Transactional
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
//        setDefaultTargetUrl("https://f3f-cokiri.site/logout-redirect");
        setDefaultTargetUrl("http://localhost:8080/logout-redirect");
        // 여기 response에 cors 관련 헤더 허용해준다는 거 넣으면 될듯? 그리고 여기서도 그 값 제거해줄 수 있을 것 같은데

        String token = request.getHeader("Authorization").split(" ")[1];
        log.info("token = " + token);
        Authentication auth = jwtTokenProvider.getAuthentication(token);
        User principal = (User) auth.getPrincipal();


        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        if (valueOperations.get(principal.getUsername()) != null) {
            // 로그아웃인데 refreshToken을 봐야할 필요가 있나?
//            throw new UserNotFoundException();
            valueOperations.getAndDelete(principal.getUsername());
        }

        super.onLogoutSuccess(request, response, authentication);
    }

}
