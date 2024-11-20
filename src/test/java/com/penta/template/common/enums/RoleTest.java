package com.penta.template.common.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest
@ActiveProfiles("local")  // --spring.profiles.active=local
class RoleTest {

    @Test
    @DisplayName("roleTest")
    public void roleTest() throws Exception {


        Role[] values = Role.values();
        for (Role role : values) {
            System.out.println("role = " + role);
            System.out.println("role.name() = " + role.name());
            System.out.println("role.toString() = " + role.toString());
        }


    }

    @Test
    @DisplayName("test2")
    public void test2() throws Exception {


        Role roleUser = Role.valueOf("ROLE_USER");
        System.out.println("roleUser = " + roleUser);

    }

}
