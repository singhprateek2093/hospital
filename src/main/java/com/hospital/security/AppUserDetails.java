package com.hospital.security;

import com.hospital.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapts our domain {@link User} into Spring Security's {@link UserDetails}.
 *
 * Once a request is authenticated, this object becomes the "principal". Controllers
 * can grab it with {@code @AuthenticationPrincipal AppUserDetails me} and reach the
 * underlying User (id, role, etc.) for authorization decisions.
 */
public class AppUserDetails implements UserDetails {

    private final User user;

    public AppUserDetails(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // e.g. ROLE_ADMIN — drives hasRole(...) checks
        return List.of(new SimpleGrantedAuthority(user.getRole().asAuthority()));
    }

    @Override public String getPassword() { return user.getPasswordHash(); }
    @Override public String getUsername() { return user.getEmail(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
