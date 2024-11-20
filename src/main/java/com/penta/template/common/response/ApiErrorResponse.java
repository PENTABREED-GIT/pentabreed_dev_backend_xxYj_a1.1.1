package com.penta.template.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * * API 컨트롤러가 사용하는 응답 Response
 */
@Schema(description = "API 실패 공통 응답")
@Getter
public class ApiErrorResponse<T> {

    // 성공 여부
    @Schema(description = "성공/실패 여부", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    private final boolean status = false;

    // 데이터
    @Schema(description = "에러 메세지", example = "에러 메세지", requiredMode = Schema.RequiredMode.AUTO)
    private String message;

    public ApiErrorResponse(String message) {
//        this.status = false;
        this.message = message;
    }
}
