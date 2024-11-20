package com.penta.template.web.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserIdCheckVo {

    @Schema(description = "사용자 아이디", example = "user1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;

}
