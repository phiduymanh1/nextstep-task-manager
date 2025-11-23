package org.example.nextstepbackend.Controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.Dto.Request.LoginRequest;
import org.example.nextstepbackend.Utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public record JwtResponse(String accessToken){}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req,
        HttpServletResponse response
    ) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.userEmail(), req.password())
            );
            // nếu authenticate thành công, tạo JWT
            String accessToken = jwtUtil.generationAccessToken(auth.getName());
            String refreshToken = jwtUtil.generationRefreshToken(auth.getName());

            Cookie cookie = new Cookie("refreshToken", refreshToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // khi deploy HTTPS -> set true
            cookie.setPath("/");     // cookie gửi ở mọi request
            cookie.setMaxAge(7 * 24 * 60 * 60);
            cookie.setAttribute("SameSite", "Strict");

            response.addCookie(cookie);

            return ResponseEntity.ok(new JwtResponse(accessToken));
        } catch (AuthenticationException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @PostMapping("/refresh")
    public JwtResponse refresh(@CookieValue("refreshToken") String refreshToken) {
        if (refreshToken == null)
            throw new RuntimeException("Refresh token missing");

        if (!jwtUtil.isRefreshTokenValid(refreshToken))
            throw new RuntimeException("Refresh token invalid");

        String username = jwtUtil.extractUserName(refreshToken);
        String newAcessToken = jwtUtil.generationAccessToken(username);

        return new JwtResponse(newAcessToken);
    }
}
