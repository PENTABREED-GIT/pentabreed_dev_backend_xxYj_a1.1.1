package com.penta.template.config.security;

import com.penta.template.config.security.filter.CustomFormLoginFilter;
import com.penta.template.config.security.filter.JwtFilter;
import com.penta.template.config.security.handler.CustomAccessDeniedHandler;
import com.penta.template.config.security.handler.CustomAuthenticationEntryPoint;
import com.penta.template.config.security.provider.CustomAuthenticationProvider;
import com.penta.template.config.security.provider.JwtProvider;
import com.penta.template.config.security.service.CustomUserDetailsService;
import com.penta.template.config.property.CustomYmlProperty;
import com.penta.template.web.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Slf4j
@Configuration
public class SecurityConfig {

    /**
     * PasswordEncoder 빈 등록
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return bCryptPasswordEncoder;
    }

    /**
     * UserDetailsService 빈 등록
     */
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return new CustomUserDetailsService(userRepository);
    }

    /**
     * 커스텀 인증 프로바이더
     */
    public AuthenticationProvider customAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        return new CustomAuthenticationProvider(userDetailsService, passwordEncoder);
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(CustomYmlProperty customYmlProperty) {
        return new CustomAuthenticationEntryPoint(customYmlProperty);
    }


    @Bean
    public AccessDeniedHandler accessDeniedHandler(CustomYmlProperty customYmlProperty) {
        return new CustomAccessDeniedHandler(customYmlProperty);
    }



    /**
     * 시큐리티 필터 체인
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserRepository userRepository, CustomYmlProperty customYmlProperty) throws Exception {


        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        JwtProvider jwtProvider = new JwtProvider(userRepository, customYmlProperty);


        CustomFormLoginFilter customFormLoginFilter = new CustomFormLoginFilter(authenticationManager, jwtProvider, userRepository);

        JwtFilter jwtFilter = new JwtFilter(jwtProvider, customYmlProperty);


        http
                .authorizeHttpRequests((auth) -> auth
                        // 정적 자원 설정
                        .requestMatchers(
                                "/",
                                "/swagger-ui/**",
                                "/api-docs/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.*",
                                "/*/icon-*",
                                "/lib/**")
                        .permitAll()
                        .requestMatchers( "/loginProc", "/api/anonymous", "/anonymous", "/user/**", "/swagger/user/login").hasRole("ANONYMOUS")
                        .requestMatchers("/api/user/**").hasRole("USER")
                        .requestMatchers("/api/auth/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)

                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling((exception) -> exception
                        .authenticationEntryPoint(authenticationEntryPoint(null))
                        .accessDeniedHandler(accessDeniedHandler(null)))

                .addFilterBefore(customFormLoginFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtFilter, CustomFormLoginFilter.class)
                .authenticationManager(authenticationManager)
                .authenticationProvider(customAuthenticationProvider(userDetailsService(null), passwordEncoder()))
        ;


        return http.build();

    }


}

