package com.penta.template.config.security.filter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.penta.template.config.security.context.UserContext;
import com.penta.template.config.security.handler.CustomAuthenticationFailureHandler;
import com.penta.template.config.security.handler.CustomAuthenticationSuccessHandler;
import com.penta.template.config.security.provider.JwtProvider;
import com.penta.template.web.user.entity.User;
import com.penta.template.web.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;


@Slf4j
public class CustomFormLoginFilter extends AbstractAuthenticationProcessingFilter {


    private static final AntPathRequestMatcher MACTHER = new AntPathRequestMatcher("/loginProc", "POST");

    private static String ID = "userId";
    private static String PASSWORD = "userPassword";

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    private final ObjectMapper mapper = new ObjectMapper();


    public CustomFormLoginFilter(AuthenticationManager authenticationManager, JwtProvider jwtProvider, UserRepository userRepository) {
        super(MACTHER);
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;

        // 인증 성공 핸들러
        setAuthenticationSuccessHandler(new CustomAuthenticationSuccessHandler());
        // 인증 실패 핸들러
        setAuthenticationFailureHandler(new CustomAuthenticationFailureHandler());
    }

    public static void setID(String ID) {
        CustomFormLoginFilter.ID = ID;
    }

    public static void setPASSWORD(String PASSWORD) {
        CustomFormLoginFilter.PASSWORD = PASSWORD;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

        String userId = String.valueOf(request.getAttribute(ID));
        String userPassword = String.valueOf(request.getAttribute(PASSWORD));

        ServletInputStream inputStream = request.getInputStream();
        String body = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

        if (StringUtils.hasText(body)) {
            log.info("body = {}", body);
            Map<String, String> map  = mapper.readValue(body, Map.class);
            userId = map.get(ID);
            userPassword = map.get(PASSWORD);
        }

        if (Strings.isEmpty(userId) || Strings.isEmpty(userPassword)) {
            throw new AuthenticationCredentialsNotFoundException("아이디 또는 패스워드를 입력해주세요");
        }

        UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(userId,userPassword);

        // 인증
        Authentication authentication = authenticationManager.authenticate(authRequest);
        SecurityContextHolder.getContextHolderStrategy().getContext().setAuthentication(authentication);

        UserContext userContext = (UserContext) authentication.getPrincipal();

        String token = jwtProvider.createToken(userContext);
        String refreshToken = jwtProvider.createRefreshToken(userContext);

        userRepository.findByUserId(userId)
                .ifPresent((user) -> {
                    user.createRefreshToken(refreshToken);
                    userRepository.saveAndFlush(user);
                });

        return authentication;


    }
}
