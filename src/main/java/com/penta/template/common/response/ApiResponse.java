package com.penta.template.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * * API 컨트롤러가 사용하는 응답 Response
 */
@Schema(description = "API 성공 공통 응답")
@Getter
public class ApiResponse<T> {

    // 성공 여부
//    @Schema(description = "성공/실패 여부", examples = {"true", "false"},requiredMode = Schema.RequiredMode.REQUIRED,)
//    @Schema(description = "성공/실패 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"true", "false"})
    @Schema(description = "성공/실패 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private final boolean status = true;

    // 데이터
    @Schema(description = "제네릭한 배열 또는 단일 데이터", requiredMode = Schema.RequiredMode.AUTO)
    private T data ;

    // 부가 데이터
    @Schema(description = "부가 데이터 key, value", requiredMode = Schema.RequiredMode.AUTO)
    private Map<String, Object> metaData = new HashMap<>();

    public ApiResponse(T data) {
        this.data = data;
    }

    public ApiResponse(T data, Map<String, Object> metaData) {
        this.data = data;
        this.metaData = metaData;
    }



}
