package org.example.nextstepbackend.Controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.Dto.Request.LoginRequest;
import org.example.nextstepbackend.Dto.Request.RegisterRequest;
import org.example.nextstepbackend.Dto.Response.AuthResponse;
import org.example.nextstepbackend.Services.Auth.AuthService;
import org.example.nextstepbackend.Utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req
                                   , HttpServletResponse response
    ) {
        try {
            Map<String,String> tokens = authService.login(req.email(), req.password());

            // Set refresh token cookie
            Cookie cookie = new Cookie("refreshToken", tokens.get("refreshToken"));
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // deploy HTTPS -> true
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60);
            cookie.setAttribute("SameSite", "Strict");
            response.addCookie(cookie);

            return ResponseEntity.ok(new AuthResponse(tokens.get("accessToken"), tokens.get("refreshToken")));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || !jwtUtil.isRefreshTokenValid(refreshToken)) {
            return ResponseEntity.status(401).body("Refresh token invalid or missing");
        }

        String newAccessToken = authService.refreshToken(refreshToken);

        return ResponseEntity.ok(new AuthResponse(newAccessToken, refreshToken));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req){
        authService.register(req);

        return ResponseEntity.ok("Successfully registered");
    }
}
