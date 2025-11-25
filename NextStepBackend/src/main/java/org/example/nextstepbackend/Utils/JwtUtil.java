package org.example.nextstepbackend.Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-expiration}")
    private Long expirationAccessToken;

    @Value("${app.jwt.refresh-token-expiration}")
    private Long expirationRefreshToken;

    private Key getKey(){
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateAccessToken(String username){
        return buildToken(username,expirationAccessToken);
    }

    public String generateRefreshToken(String username){
        return buildToken(username,expirationRefreshToken);
    }

    public String buildToken(String username,Long exp){
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+ exp))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUserName(String token){
        Claims claims = parseClaims(token);
        if (claims == null) {
            return null;
        }
        return claims.getSubject();
    }

    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        Claims claims = parseClaims(token);

        if (claims == null) return false;

        return claims.getSubject().equals(userDetails.getUsername())
                && !claims.getExpiration().before(new Date());
    }

    public boolean isRefreshTokenValid(String token) {
        Claims c = parseClaims(token);
        if (c == null) return false;

        return !c.getExpiration().before(new Date());
    }


    private Claims parseClaims(String token){
        try {
            return Jwts.parserBuilder() // bat dau qua trinh giai ma jwt
                    .setSigningKey(getKey())// xac thuc chu ky
                    .build()   //xay dung doi tuong JwtParser
                    .parseClaimsJws(token)   //giai ma token
                    .getBody(); // trich xuat doi tuong claims
        }catch (JwtException e){
            return null;
        }
    }

}
