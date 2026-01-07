package org.example.nextstepbackend.services.security;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerUserDetailService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) {
    var user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

    return new CustomUserDetails(
        user.getId(), user.getEmail(), user.getPasswordHash(), List.of(authority));
  }
}
