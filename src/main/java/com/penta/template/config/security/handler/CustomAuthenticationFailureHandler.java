package com.penta.template.config.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penta.template.common.response.ApiErrorResponse;
import com.penta.template.common.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

@Slf4j
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        String errorMessage = "존재하지 않은 회원입니다";
        if (exception instanceof BadCredentialsException) {
            errorMessage = "아이디 및 패스워드를 잘못 입력 하였습니다";
        }

        if (exception instanceof UsernameNotFoundException) {
            errorMessage = "존재하지 않은 회원 입니다";
        }

        if (exception instanceof AuthenticationCredentialsNotFoundException) {
            errorMessage = exception.getMessage();
        }

        log.info("인증 실패 - {}", errorMessage);
//        response.sendRedirect("/login");

        //! API 응답
        response.setCharacterEncoding("utf-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.OK.value());

        ApiErrorResponse<?> apiErrorResponse = new ApiErrorResponse<>(errorMessage);

//        ApiResponse apiResponse = ApiResponse.error(errorMessage);
//        String text = this.mapper.writeValueAsString(apiResponse);

        String text = this.mapper.writeValueAsString(apiErrorResponse);
        response.getWriter().write(text);

    }
}
