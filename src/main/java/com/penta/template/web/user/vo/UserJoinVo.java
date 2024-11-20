package com.penta.template.web.user.vo;

import com.penta.template.common.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserJoinVo {

    @Schema(description = "사용자 아이디", example = "user1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;

    @Schema(description = "사용자 패스워드", example = "1234", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userPassword;

    @Schema(description = "권한", example = "ROLE_USER", requiredMode = Schema.RequiredMode.REQUIRED)
    private Role role;

}
