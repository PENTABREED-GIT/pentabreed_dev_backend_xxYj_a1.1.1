package com.penta.template.web.user.controller;

import com.penta.template.common.annotation.CustomApiResponse;
import com.penta.template.common.response.ApiResponse;
import com.penta.template.config.security.context.UserContext;
import com.penta.template.web.user.dto.UserDto;
import com.penta.template.web.user.service.UserService;
import com.penta.template.web.user.vo.UserIdCheckVo;
import com.penta.template.web.user.vo.UserJoinVo;
import com.penta.template.web.user.vo.UserLoginVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@Tag(name = "사용자", description = "회원가입, 로그인 ...")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    /**
     * 로그인 (스웨거 전용, 실제로는 시큐리티 필터가 로직을 수행함)
     */
    @CustomApiResponse(summary = "스웨거 전용 로그인 (실제론 시큐리티 필터가 동작)", successDescription = "로그인 성공")
    @PostMapping("/swagger/user/login")
    public void login(@RequestBody UserLoginVo userLoginVo, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.info("userLoginVo = {}", userLoginVo);
        request.setAttribute("userId", userLoginVo.getUserId());
        request.setAttribute("userPassword", userLoginVo.getUserPassword());
        // 필터로 포워딩
        RequestDispatcher dispatcher = request.getRequestDispatcher("/loginProc");
        dispatcher.forward(request, response);
    }

    /**
     * 회원가입
     */
    @CustomApiResponse(summary = "회원가입", successDescription = "가입 성공")
    @PostMapping("/user/join")
    public ResponseEntity<ApiResponse<Long>> join(@RequestBody UserJoinVo userJoinVo) {
        log.info("userJoinRequest = {}", userJoinVo);
        Long seq = userService.join(userJoinVo);
        ApiResponse<Long> apiResponse = new ApiResponse<>(seq);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
//        return ApiResponse.ok(seq);
    }


    /**
     * 자신의 정보
     */
    @CustomApiResponse(summary = "토큰으로 자신의 정보 조회", successDescription = "자신의 정보 조회 성공")
    @GetMapping("/api/user/me")
    public ResponseEntity<ApiResponse<UserDto>> userMe(@AuthenticationPrincipal UserContext userContext) {
        log.info("userContext = {}", userContext);
        UserDto userDto = UserDto.builder()
                .userId(userContext.getUserId())
                .role(userContext.getRole())
                .build();
        ApiResponse<UserDto> apiResponse = new ApiResponse<>(userDto);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
//        return ApiResponseDto.ok(userDto);
    }


    /**
     * 중복 아이디 검사
     */
    @CustomApiResponse(summary = "아이디 중복 체크", successDescription = "아이디 중복 체크 성공", errorDescription = "잘못된 요청")
    @PostMapping("/user/id/check")
    public ResponseEntity<ApiResponse<?>> idCheck(@RequestBody UserIdCheckVo userIdCheckVo) {
        log.info("userIdCheckRequest = {}", userIdCheckVo);
        userService.idCheck(userIdCheckVo);
        ApiResponse<?> apiResponse = new ApiResponse<>(true);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
//        return ApiResponseDto.ok();
    }


}
