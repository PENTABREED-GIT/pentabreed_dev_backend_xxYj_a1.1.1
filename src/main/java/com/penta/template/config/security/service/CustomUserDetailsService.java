package com.penta.template.config.security.service;

import com.penta.template.config.security.context.UserContext;
import com.penta.template.web.user.entity.User;
import com.penta.template.web.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;



    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        User user = userRepository.findByUserId(userId).orElseThrow(() -> {
            log.info("존재하지 않은 회원입니다, userId = {}", userId);
            return new UsernameNotFoundException("존재하지 않은 회원입니다");
        });

        UserContext userContext = new UserContext(user);

        return userContext;
    }
}
