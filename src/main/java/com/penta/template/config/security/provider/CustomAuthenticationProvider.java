package com.penta.template.config.security.provider;

import com.penta.template.config.security.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;

@Slf4j
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private UserDetailsService userDetailsService;  // CustomUserDetailsService

    private PasswordEncoder passwordEncoder;        // BCryptPasswordEncoder

    public CustomAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userId = authentication.getName();
        String userPassword = (String) authentication.getCredentials();

        UserContext userContext = (UserContext) userDetailsService.loadUserByUsername(userId);
        log.info("로그인 회원 조회 = {}", userContext);

        if (passwordEncoder.matches(userPassword, userContext.getPassword())) {
            Collection<? extends GrantedAuthority> roles = userContext.getAuthorities();
            return new UsernamePasswordAuthenticationToken(userContext, null, roles);

        } else {
            // TODO
            log.info("패스워드 일치안함");
            throw new BadCredentialsException("");
        }

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
    }
}
