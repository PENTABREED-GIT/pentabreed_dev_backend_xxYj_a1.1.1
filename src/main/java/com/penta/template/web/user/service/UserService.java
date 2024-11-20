package com.penta.template.web.user.service;

import com.penta.template.common.exception.DuplicateUserIdException;
import com.penta.template.config.security.context.UserContext;
import com.penta.template.web.user.vo.UserIdCheckVo;
import com.penta.template.web.user.vo.UserJoinVo;
import com.penta.template.web.user.entity.User;
import com.penta.template.web.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long join(UserJoinVo userJoinVo) {
        try {

            userRepository
                    .findByUserId(userJoinVo.getUserId())
                    .ifPresent(user -> {
                        throw new DuplicateUserIdException();
                    });

            User user = User.builder()
                    .userId(userJoinVo.getUserId())
                    .userPassword(passwordEncoder.encode(userJoinVo.getUserPassword()))
                    .role(userJoinVo.getRole())
                    .build();


            // userRepository.save 때, auditor 가 등록자, 수정자를 자동으로 넣기위해 S.C(Security Context)에 저장
            // 이후 로직에는 jwt 토큰을 가지고 filter 에서 미리 등록하기 때문에 필요엄슴...
            SecurityContextHolder.getContext()
                    .setAuthentication(new UsernamePasswordAuthenticationToken(new UserContext(user), null, null));

            user = userRepository.save(user);
            log.info("user = {}", user);

            // userId 가 아닌 pk 입니다
            return user.getId();

        } catch (DuplicateUserIdException e) {
            e.printStackTrace();
            throw new DuplicateUserIdException();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("회원 가입 에러");
        }
    }

    public void idCheck(UserIdCheckVo userIdCheckVo) {
        userRepository
                .findByUserId(userIdCheckVo.getUserId())
                .ifPresent(user -> {
                    throw new DuplicateUserIdException();
                });
    }
}
