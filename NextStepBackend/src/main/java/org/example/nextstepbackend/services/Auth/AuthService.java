package org.example.nextstepbackend.services.Auth;

import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.dto.request.RegisterRequest;
import org.example.nextstepbackend.entity.PasswordResetToken;
import org.example.nextstepbackend.entity.User;
import org.example.nextstepbackend.mappers.UserMapper;
import org.example.nextstepbackend.repository.PasswordResetTokenRepository;
import org.example.nextstepbackend.repository.UserRepository;
import org.example.nextstepbackend.services.Mail.MailService;
import org.example.nextstepbackend.utils.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MailService mailService;

    public Map<String,String> login(String userEmail,String password){
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userEmail, password)
        );
        // If authenticate success, create JWT
        String accessToken = jwtUtil.generateAccessToken(auth.getName());
        String refreshToken = jwtUtil.generateRefreshToken(auth.getName());

        // Return both tokens, controller will set cookies
        return Map.of(
                "accessToken",accessToken,
                "refreshToken",refreshToken
        );
    }


    public String refreshToken(String refreshToken){
        String username = jwtUtil.extractUserName(refreshToken);

        return jwtUtil.generateAccessToken(username);
    }

    public void register(RegisterRequest request){
        User user = userMapper.toUser(request);

        String passwordHash = passwordEncoder.encode(request.password());
        user.setPasswordHash(passwordHash);

        userRepository.save(user);
    }

    public void forgotPassword(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //1. gen token
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = passwordEncoder.encode(rawToken);

        //2. save token
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(tokenHash)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        // 3. Build reset link
        String resetLink =
                "http://localhost:8080/reset-password?token=" + rawToken;

        // 4. Gửi mail (ASYNC + RETRY)
        mailService.sendMail(
                user.getEmail(),
                "Reset your password",
                "Click link to reset password:\n\n" + resetLink +
                        "\n\nLink het han sau 15 phut"
        );
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {

        List<PasswordResetToken> tokens =
                passwordResetTokenRepository.findAllValidTokens(
                        LocalDateTime.now()
                );

        PasswordResetToken resetToken = tokens.stream()
                .filter(t -> passwordEncoder.matches(rawToken, t.getToken()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.getUsed()) {
            throw new RuntimeException("Token already used");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

}
