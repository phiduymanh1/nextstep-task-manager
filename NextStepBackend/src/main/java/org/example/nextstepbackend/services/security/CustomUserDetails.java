package org.example.nextstepbackend.services.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
  @Getter private final Integer id;
  private final String email;
  private final String password;
  private final boolean isActive;
  private final List<GrantedAuthority> authorities;

  public CustomUserDetails(
      Integer id,
      String email,
      String password,
      boolean isActive,
      List<GrantedAuthority> authorities) {
    this.id = id;
    this.email = email;
    this.password = password;
    this.isActive = isActive;
    this.authorities = authorities;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
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
    return isActive;
  }
}
