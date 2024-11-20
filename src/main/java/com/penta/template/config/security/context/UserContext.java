package com.penta.template.config.security.context;

import com.penta.template.common.enums.Role;
import com.penta.template.web.user.entity.User;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@ToString
public class UserContext implements UserDetails {


    private User user;

    // User 안에서 자주 쓰이는건 UserContext 에 노출 ...
    private String userId;
    private String userPassword;
    private Role role;
    private String token;

    /**
     * User 엔티티는 리프레쉬 토큰만을 가지고 있어,
     * token이 필요할땐 UserContext에 바인딩
     */
    public void setToken(String token) {
        this.token = token;
    }


    /**
     * 로그인, 회원가입 시
     */
    public UserContext(User user) {
        this.user = user;
        this.userId = user.getUserId();
        this.userPassword = user.getUserPassword();
        this.role = user.getRole();
    }

    /**
     * 리프래쉬 토큰으로 인증 상태 유지시
     */
    public UserContext(String id, String password, Role role) {
        this.userId = id;
        this.userPassword = password;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(this.role.name());
        return List.of(authority);
    }


    @Override
    public String getPassword() {
        return this.userPassword;
    }

    @Override
    public String getUsername() {
        return this.userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
