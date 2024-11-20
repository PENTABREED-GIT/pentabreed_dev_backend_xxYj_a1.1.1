package com.penta.template.config.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penta.template.common.response.ApiErrorResponse;
import com.penta.template.common.response.ApiResponse;
import com.penta.template.config.property.CustomYmlProperty;
import com.penta.template.config.security.provider.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JwtFilter extends OncePerRequestFilter {


    private ObjectMapper mapper = new ObjectMapper();

    private JwtProvider jwtProvider;
    private CustomYmlProperty customYmlProperty;

    public JwtFilter(JwtProvider jwtProvider, CustomYmlProperty customYmlProperty) {
        this.jwtProvider = jwtProvider;
        this.customYmlProperty = customYmlProperty;
    }


    /**
     * /api 로 시작하는 요청만 토큰 검사
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        AntPathRequestMatcher matcher = new AntPathRequestMatcher("/api/**");

        String requestURI = request.getRequestURI();
        log.info("[0] 요청 URL = {}", requestURI);

        if (matcher.matches(request)) {
            try {
                String header = request.getHeader("Authorization");
                if (header == null || !header.contains("Bearer"))
                    throw new RuntimeException("올바르지 않은 Header 입니다");
                String token = header.substring("Bearer".length() + 1);
                log.info("사용자가 보낸 토큰 = {}", token);
                // 토큰 검증
                jwtProvider.verifyToken(token, response);

            } catch (Exception e) {

                ApiErrorResponse<?> apiErrorResponse = new ApiErrorResponse<>(e.getMessage());

                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.setStatus(HttpStatus.OK.value());
//                response.getWriter().write(mapper.writeValueAsString(ApiResponse.error(e.getMessage())));
                response.getWriter().write(mapper.writeValueAsString(apiErrorResponse));
                return;
            }
        }
        filterChain.doFilter(request, response);


    }
}
