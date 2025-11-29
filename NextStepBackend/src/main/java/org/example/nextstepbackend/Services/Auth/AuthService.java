package org.example.nextstepbackend.Services.Auth;

import lombok.RequiredArgsConstructor;
import org.example.nextstepbackend.Dto.Request.RegisterRequest;
import org.example.nextstepbackend.Entity.User;
import org.example.nextstepbackend.Utils.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

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

    }


}
