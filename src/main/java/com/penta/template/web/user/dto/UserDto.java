package com.penta.template.web.user.dto;

import com.penta.template.common.enums.Role;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserDto {
    private String userId;
    private Role role;
}
