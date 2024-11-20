package com.penta.template.web.admin.controller;

import com.penta.template.common.response.ApiResponse;
import com.penta.template.config.security.context.UserContext;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ROLE_ADMIN 만 호출 가능
 */
@RestController
public class AdminController {

    @GetMapping("/api/admin")
    public ApiResponse<?> apiAdmin(@AuthenticationPrincipal UserContext userContext) {
        String adminId = userContext.getUserId();
//        return ApiResponse.ok(adminId);
        return new ApiResponse<>(adminId);
    }
}
