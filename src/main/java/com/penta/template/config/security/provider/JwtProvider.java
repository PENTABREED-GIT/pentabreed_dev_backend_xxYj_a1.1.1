package com.penta.template.config.security.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.penta.template.common.enums.Role;
import com.penta.template.config.security.context.UserContext;
import com.penta.template.config.property.CustomYmlProperty;
import com.penta.template.web.user.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

@Slf4j
public class JwtProvider {

    private final ObjectMapper mapper = new ObjectMapper();
    private final UserRepository userRepository;
    private final CustomYmlProperty customYmlProperty;

    // 일반 토큰 타임 셋팅 (1 분)
    private int time;
    private ChronoUnit timeUnit = ChronoUnit.MINUTES;

    // 리프레쉬 토큰 타임 셋팅 (4 분)
    private int refreshTime;
    private ChronoUnit refreshTimeUnit = ChronoUnit.MINUTES;

    // 비밀키
    private String secretKey;
    private byte[] secretKeyBytes;
    private SecretKey key;
    private String base64EncodedSecretKey;

    public JwtProvider(UserRepository userRepository, CustomYmlProperty customYmlProperty) {
        this.userRepository = userRepository;
        this.customYmlProperty = customYmlProperty;
        this.time = Integer.valueOf(customYmlProperty.getKey("jwt.token.time"));
        this.refreshTime = Integer.valueOf(customYmlProperty.getKey("jwt.refresh.token.time"));
        this.secretKey = customYmlProperty.getKey("jwt.secret.key");

        // 비밀키를 바이트로 전환
        secretKeyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

        // 512 비트 이상이면 SHA-512 동작
        key = Keys.hmacShaKeyFor(secretKeyBytes);

        // jwt.io 사이트에서 검증할 때 사용할 Base64 인코딩된 비밀키
        base64EncodedSecretKey = Base64.getEncoder().encodeToString(secretKeyBytes);
    }

    /**
     * 토큰 생성 후 UserContext 에 추가
     */
    public String createToken(UserContext userContext) {
        String token = generateToken(userContext, time, timeUnit);
        log.info("생성된 토큰 = {}", token);
        userContext.setToken(token);
        return token;
    }


    /**
     * 리프래쉬 토큰 생성
     */
    public String createRefreshToken(UserContext userContext) {
        String refreshToken = generateToken(userContext, refreshTime, refreshTimeUnit);
        log.info("생성된 리프래쉬 토큰 = {}", refreshToken);
        return refreshToken;
    }


    /**
     * JWT 토큰 생성기
     */
    private String generateToken(UserContext userContext, int time, ChronoUnit timeUnit) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");

        // 현재 순간
        Instant instant = Instant.now();


        // 생성일
        Date createDate = Date.from(instant);

        // 만료일
        Date endDate = Date.from(instant.plus(time, timeUnit));


        // 출력용
        String startDateText = sdf.format(createDate);
        String endDateText = sdf.format(endDate);
        log.info("토큰 시작 시각 = {}", startDateText);
        log.info("토큰 종료 시각 = {}", endDateText);

        // 알고리즘
        MacAlgorithm hs512 = Jwts.SIG.HS512;

        return Jwts.builder()
                .subject("penta")
                .issuedAt(createDate)
                .expiration(endDate)
                .claim("userId", userContext.getUserId())
                .claim("role", userContext.getRole())
                .signWith(key, hs512)  // 서명
                .issuer("penta")
                .compact();
    }


    /**
     * 토큰 검증
     */
    public void verifyToken(String token, HttpServletResponse response) throws JsonProcessingException {

        JwtParser jwtParser = Jwts.parser()
                .verifyWith(key)
                .build();


        // Base64로 토큰 페이로드 읽기
        String json = readPayloadToBase64(token);

        // 토큰을 map 으로 전환
        Map<String,String> dataMap = mapper.readValue(json, Map.class);

        String userId = dataMap.get("userId");
        Role role = Role.valueOf(dataMap.get("role"));
        UserContext userContext = new UserContext(userId, null, role);

        try {
            Jwt<?, ?> jwt = jwtParser.parse(token);
            log.info("jwt = {}", jwt);

            Collection<? extends GrantedAuthority> roles = userContext.getAuthorities();

            // 시큐리티 컨텍스트에 저장한다
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userContext, null, roles);
            SecurityContextHolder.getContextHolderStrategy().getContext().setAuthentication(usernamePasswordAuthenticationToken);


        } catch(ExpiredJwtException e) {
            log.info("[1] 토큰 만료되었습니다");

            // 리프래쉬 토큰 검증
            verifyRefreshToken(response, userId, jwtParser, userContext);

        } catch(Exception e) {
            log.info("토큰 체크 에러 발생");
            throw new RuntimeException("토큰이 변조 되었습니다");
        }



    }

    /**
     * 토큰 유효 시간 상관없이 JWT 토큰 Base64로 읽기
     */
    private static String readPayloadToBase64(String token) {
        String[] chunks = token.split("\\.");
        Base64.Decoder decode = Base64.getUrlDecoder();
        byte[] decodeByteArr = decode.decode(chunks[1]);    // 0 : 헤더, 1 : 페이로드, 2 : 시그니처
        String json = new String(decodeByteArr);
        log.info("base64 - jwt = {} ", json);
        return json;
    }



    /**
     * 리프래쉬 토큰 검증
     */
    private void verifyRefreshToken(HttpServletResponse response, String id, JwtParser jwtParser, UserContext userContext) {

        try {
            String refreshToken = userRepository.findByRefreshTokenByUserId(id);

            // 리프래쉬 토큰을 검증 해본다
            Jwt<?, ?> jwt = jwtParser.parse(refreshToken);
            log.info("[2] 리프래쉬 토큰이 존재합니다");
            log.info("[3] 새로운 토큰을 발급합니다");

            // 검증에 성공하면
            // 1. 정상 토큰 발급 (리프래쉬 토큰은 발급 하지 않는다)
            String reToken = createToken(userContext);

            Collection<? extends GrantedAuthority> roles = userContext.getAuthorities();

            // 최초 로그인 이후에 userContext.getUser 는 null 이다
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userContext, null, roles);
            SecurityContextHolder.getContextHolderStrategy().getContext().setAuthentication(usernamePasswordAuthenticationToken);


        } catch(Exception innerException) {
            log.info("[2] 리프래쉬 토큰 만료 되었습니다");
            throw new RuntimeException(customYmlProperty.getKey("jwt.expire"));
        }

    }


}
