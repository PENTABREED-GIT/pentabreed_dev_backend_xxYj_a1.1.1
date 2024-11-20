package com.penta.template.web.user.dto;

import com.penta.template.common.enums.Role;
import lombok.*;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDto {

    private String userId;
    private Role role;
    private String token;
}
