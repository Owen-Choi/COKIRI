package f3f.dev1.global.common.constants;

public class JwtConstants {
    public static final String AUTHORITIES_KEY = "auth";
    public static final String BEARER_TYPE = "Bearer";
    public static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30;            // 30분
    public static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;  // 7일
    public static final long REFRESH_TOKEN_COOKIE_EXPIRE_TIME =  60 * 60 * 24 * 7;  // 7일

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    public static final String REFRESH_TOKEN = "refreshToken";

    public static final String SET_COOKIE = "Set-Cookie";

}