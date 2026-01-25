package org.example.nextstepbackend.services.security;

import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

  public void validate(String refreshToken) {
    // hiện tại chỉ verify chữ ký + exp
  }

  public void revoke(String refreshToken) {
    // TODO: implement Redis sau ( hien tai dang validate basic trong /refresh )
  }
}
