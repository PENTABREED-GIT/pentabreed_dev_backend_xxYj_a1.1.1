package com.penta.template.config.audit;

import com.penta.template.config.security.context.UserContext;
import com.penta.template.web.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Slf4j
public class AuditorAwareImpl implements AuditorAware<String> {


    @Override
    public Optional<String> getCurrentAuditor() {

        ////////////////////////
        // Spring Security
        ////////////////////////
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Auditor = {}", authentication);

        UserContext userContext = (UserContext) authentication.getPrincipal();
        User user = userContext.getUser();

        // TODO User PK vs User ID
        // BaseEntity 의 regId, modId (등록자, 수정자에 사용자 아이디로 값 설정)
        return Optional.of(user.getUserId());
    }


}
