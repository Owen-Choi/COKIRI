package f3f.dev1.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import f3f.dev1.global.error.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        sendResponse(response, authException);

    }

    private void sendResponse(HttpServletResponse response, AuthenticationException authException) throws IOException {
        String result;
        if (authException instanceof BadCredentialsException) {
            result = objectMapper.writeValueAsString(new ErrorResponse(CONFLICT, "INVALID_EMAIL_PASSWORD"));
            response.setStatus(response.SC_CONFLICT);
        } else if (authException instanceof InternalAuthenticationServiceException) {
            result = objectMapper.writeValueAsString(new ErrorResponse(NOT_FOUND,
                    "USERDETAIL_NOT_FOUND"));
            response.setStatus(response.SC_NOT_FOUND);

        } else {
            result = objectMapper.writeValueAsString(new ErrorResponse(INTERNAL_SERVER_ERROR, authException.getMessage()));
            response.setStatus(response.SC_INTERNAL_SERVER_ERROR);
        }



        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Access-Control-Allow-Origin","http://localhost:3000");
        response.getWriter().write(result);

    }


}
