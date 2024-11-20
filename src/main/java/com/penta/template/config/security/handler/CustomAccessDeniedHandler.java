package com.penta.template.config.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penta.template.common.response.ApiErrorResponse;
import com.penta.template.common.response.ApiResponse;
import com.penta.template.config.property.CustomYmlProperty;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * 인가 에러 핸들러
 */
@Slf4j
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final CustomYmlProperty customYmlProperty;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.info("[인가예외] 요청 url : {}", request.getRequestURI());

//        ApiResponse apiResponse = ApiResponse.error(customYmlProperty.getKey("security.exception.deny"));
        ApiErrorResponse<?> apiErrorResponse = new ApiErrorResponse<>(customYmlProperty.getKey("security.exception.deny"));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
//        response.setStatus(HttpStatus.OK.value());
        response.setStatus(HttpStatus.FORBIDDEN.value()); // 403 금지
        response.getWriter().write(mapper.writeValueAsString(apiErrorResponse));
    }
}
