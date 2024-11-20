package com.penta.template.web.sample.controller;

import com.penta.template.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SampleController {

    @GetMapping("/")
    public ApiResponse<String> index() {
//        return ApiResponse.ok( "Hello World");
        return new ApiResponse<>("Hello World");
    }

    /**
     * 토큰없이 테스트 -> JwtFilter 가 캐치하지 않는다 (/api/** 만 캐치)
     */
    @GetMapping("/anonymous")
    public ApiResponse<String> anonymous() {
//        return ApiResponse.ok("ANONYMOUS 로서 호출 성공");
        return new ApiResponse<>("ANONYMOUS 로서 호출 성공");
    }

    /**
     * USER Token 을 끼고 테스트 -> JwtFilter 가 캐치한다 -> AccessDeniedHandler 인가 핸들러 동작
     */
    @GetMapping("/api/anonymous")
    public ApiResponse<String> apiAnonymous() {
//        return ApiResponse.ok("[API] ANONYMOUS 로서 호출 성공");
        return new ApiResponse<>("[API] ANONYMOUS 로서 호출 성공");
    }

}
