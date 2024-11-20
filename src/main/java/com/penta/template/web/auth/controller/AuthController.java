package com.penta.template.web.auth.controller;

import com.penta.template.common.response.ApiResponse;
import com.penta.template.config.security.context.UserContext;
import com.penta.template.web.user.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ROLE_USER OR ROLE_ADMIN 공통 호출 가능
 */
@Slf4j
@RestController
public class AuthController {

    @GetMapping("/api/auth/me")
    public ApiResponse<?> apiAuthMe(@AuthenticationPrincipal UserContext userContext) {

        UserDto userDto = UserDto.builder()
                .userId(userContext.getUserId())
                .role(userContext.getRole())
                .build();

        log.info("userDto = {}", userDto);
//        return ApiResponse.ok(data);
        return new ApiResponse<UserDto>(userDto);
    }
}
