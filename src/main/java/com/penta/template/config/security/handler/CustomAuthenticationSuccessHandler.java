package com.penta.template.config.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penta.template.common.response.ApiResponse;
import com.penta.template.config.security.context.UserContext;
import com.penta.template.web.user.dto.UserLoginDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("인증 성공 - {}", authentication);

        UserContext userContext = (UserContext) authentication.getPrincipal();

        UserLoginDto userLoginDto = UserLoginDto.builder()
                .userId(userContext.getUserId())
                .role(userContext.getRole())
                .token(userContext.getToken())
                .build();

//        ApiResponse apiResponse = ApiResponse.ok(userLoginDto);
        ApiResponse<UserLoginDto> apiResponse = new ApiResponse<>(userLoginDto);
        String responseBody = mapper.writeValueAsString(apiResponse);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.OK.value());
        response.getWriter().write(responseBody);

    }
}
