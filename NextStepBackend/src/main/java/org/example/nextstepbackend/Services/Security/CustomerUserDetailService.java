package org.example.nextstepbackend.Services.Security;

import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.Repositorys.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String role = user.getUserRole();
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_"+role);

        return User.builder().username(user.getUserEmail())  // username in Spring Security = email here
                .password(user.getPasswordHash()) // encoded password stored in DB
                .authorities(List.of(grantedAuthority))
                .accountExpired(false)
                .accountLocked(false) // example: treat inactive as locked (adjust to your entity)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
