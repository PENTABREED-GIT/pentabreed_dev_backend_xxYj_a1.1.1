package com.penta.template.web.user.entity;

import com.penta.template.common.enums.Role;
import com.penta.template.config.security.context.UserContext;
import com.penta.template.web.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

//@DataJpaTest
@ActiveProfiles("local")  // --spring.profiles.active=local
@SpringBootTest
public class UserTest {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    String id = "user1";
    String password = "1234";


    @BeforeEach
    public void beforeEach() {
        String userId = "user1";
        String password = "1234";

        UserContext userContext = new UserContext(userId, password, Role.ROLE_USER);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userContext, null, List.of(new SimpleGrantedAuthority(Role.ROLE_USER.name())));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @Transactional
    @DisplayName("asd")
    @Commit
    public void temp() throws Exception {
        UserContext userContext = (UserContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = User.builder()
                .userId(userContext.getUserId())
                .userPassword(passwordEncoder.encode(userContext.getPassword()))
                .role(Role.ROLE_USER)
                .build();

        System.out.println("user = " + user);


    }

}
