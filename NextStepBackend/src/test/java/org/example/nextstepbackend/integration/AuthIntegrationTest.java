package org.example.nextstepbackend.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.example.nextstepbackend.comm.constants.Const;
import org.example.nextstepbackend.comm.constants.ValidateMessageConst;
import org.example.nextstepbackend.dto.request.LoginRequest;
import org.example.nextstepbackend.enums.MessageConst;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Value("${app.jwt.secret}")
  private String secret;

  private static final String DOMAIN_API_FINAL = "/auth/login";
  private static final String DOMAIN_API_REFRESH = "/auth/refresh";

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @Sql(
      scripts = "/db/sql/modules/auth/insert-user.sql",
      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/db/sql/clean-up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public @interface WithAuthUser {}

  // Login Api ST
  /** Test login success case */
  @Test
  @WithAuthUser
  void login_success_should_return_token() throws Exception {
    LoginRequest loginRequest = new LoginRequest("phiduymanh@gmail.com", "1");

    mockMvc
        .perform(
            post(DOMAIN_API_FINAL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.metaData.success").value(true))
        .andExpect(jsonPath("$.metaData.code").value(MessageConst.AUTH_LOGIN_SUCCESS.getCode()))
        .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
        .andExpect(jsonPath("$.data.accessToken").value(org.hamcrest.Matchers.startsWith("ey")))
        .andExpect(cookie().exists(Const.TEXT_REFRESH_TOKEN))
        .andExpect(cookie().value(Const.TEXT_REFRESH_TOKEN, Matchers.startsWith("ey")));
  }

  /** Test login fail case - validate json body */
  @Test
  @WithAuthUser
  void login_fail_validate_json_body() throws Exception {
    LoginRequest loginRequest = new LoginRequest("", "");

    mockMvc
        .perform(
            post(DOMAIN_API_FINAL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.metaData.success").value(false))
        .andExpect(jsonPath("$.metaData.code").value("VALIDATION_ERROR"))
        .andExpect(
            jsonPath("$.metaData.errors", Matchers.hasItem(ValidateMessageConst.EMAIL_REQUIRED)))
        .andExpect(
            jsonPath(
                "$.metaData.errors", Matchers.hasItem(ValidateMessageConst.PASSWORD_REQUIRED)));
  }

  /** Test login fail case - wrong password */
  @Test
  @WithAuthUser
  void login_fail_wrong_password() throws Exception {
    LoginRequest loginRequest = new LoginRequest("phiduymanh@gmail.com", "wrong_password");

    mockMvc
        .perform(
            post(DOMAIN_API_FINAL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.metaData.success").value(false))
        .andExpect(
            jsonPath("$.metaData.code").value(MessageConst.AUTH_INVALID_CREDENTIALS.getCode()));
  }

  /** Test login fail case - unregistered email */
  @Test
  @WithAuthUser
  void login_fail_unregistered_email() throws Exception {
    LoginRequest loginRequest = new LoginRequest("manhphi@gmail.com", "1");

    mockMvc
        .perform(
            post(DOMAIN_API_FINAL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.metaData.success").value(false))
        .andExpect(
            jsonPath("$.metaData.code").value(MessageConst.AUTH_INVALID_CREDENTIALS.getCode()));
  }

  /** Verify cookie attributes */
  @Test
  @WithAuthUser
  void login_success_verify_cookie_attributes() throws Exception {
    LoginRequest loginRequest = new LoginRequest("phiduymanh@gmail.com", "1");

    mockMvc
        .perform(
            post(DOMAIN_API_FINAL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(cookie().exists(Const.TEXT_REFRESH_TOKEN))
        .andExpect(cookie().httpOnly(Const.TEXT_REFRESH_TOKEN, true))
        .andExpect(cookie().path(Const.TEXT_REFRESH_TOKEN, "/"))
        .andExpect(cookie().maxAge(Const.TEXT_REFRESH_TOKEN, 7 * 24 * 60 * 60));
  }

  /** verify jwt claims in access token */
  @Test
  @WithAuthUser
  void login_success_verify_jwt_claims_in_access_token() throws Exception {
    String email = "phiduymanh@gmail.com";
    LoginRequest loginRequest = new LoginRequest(email, "1");

    mockMvc
        .perform(
            post(DOMAIN_API_FINAL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(
            result -> {
              String responseBody = result.getResponse().getContentAsString();
              String accessToken = JsonPath.read(responseBody, "$.data.accessToken");

              Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

              Claims claims =
                  Jwts.parserBuilder()
                      .setSigningKey(key)
                      .build()
                      .parseClaimsJws(accessToken)
                      .getBody();

              assertEquals(email, claims.getSubject());
              assertNotNull(claims.getExpiration());
              assertTrue(claims.getExpiration().after(new Date()));
            });
  }

  /** inactive user should not log in */
  @Test
  @Sql(
      scripts = "/db/sql/modules/auth/insert-user-inactive.sql",
      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/db/sql/clean-up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  void login_fail_inactive_user() throws Exception {
    LoginRequest loginRequest = new LoginRequest("phiduymanh@gmail.com", "1");

    mockMvc
        .perform(
            post(DOMAIN_API_FINAL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.metaData.success").value(false))
        .andExpect(jsonPath("$.metaData.code").value(MessageConst.AUTH_DISABLED_ACCOUNT.getCode()));
  }

  // Login Api EN

  // Refresh Api ST
  /** Test refresh success case */
  @Test
  @WithAuthUser
  void refresh_success() throws Exception {
    LoginRequest loginRequest = new LoginRequest("phiduymanh@gmail.com", "1");

    MvcResult apiLoginResult =
        mockMvc
            .perform(
                post(DOMAIN_API_FINAL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

    Cookie refreshTokenCookie = apiLoginResult.getResponse().getCookie(Const.TEXT_REFRESH_TOKEN);

    mockMvc
        .perform(post(DOMAIN_API_REFRESH).cookie(refreshTokenCookie))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
        .andExpect(jsonPath("$.data.accessToken").value(org.hamcrest.Matchers.startsWith("ey")))
        .andExpect(jsonPath("$.metaData.success").value(true))
        .andExpect(jsonPath("$.metaData.code").value(MessageConst.AUTH_REFRESH_SUCCESS.getCode()));
  }

  /** Test refresh fail case - missing refresh token */
  @Test
  void refresh_fail_missing_refresh_token() throws Exception {
    mockMvc
        .perform(post(DOMAIN_API_REFRESH))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.metaData.success").value(false))
        .andExpect(jsonPath("$.metaData.code").value("AUTH_ERROR"));
  }

  /** Test refresh fail case - invalid refresh token */
  @Test
  void refresh_fail_invalid_refresh_token() throws Exception {
    Cookie invalidRefreshTokenCookie = new Cookie(Const.TEXT_REFRESH_TOKEN, "invalid_token_value");
    mockMvc
        .perform(post(DOMAIN_API_REFRESH).cookie(invalidRefreshTokenCookie))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.metaData.success").value(false))
        .andExpect(jsonPath("$.metaData.code").value("AUTH_ERROR"));
  }

  /** Test refresh fail case - expired refresh token */
  @Test
  void refresh_fail_expired_refresh_token() throws Exception {
    String expiredToken =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJwaGlkdXltYW5oQGdtYWlsLmNvbSIsImlhdCI6MTYwOTAwMDAwMCwiZXhwIjoxNjA5MDAwMDAxfQ.sflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    Cookie expiredRefreshTokenCookie = new Cookie(Const.TEXT_REFRESH_TOKEN, expiredToken);
    mockMvc
        .perform(post(DOMAIN_API_REFRESH).cookie(expiredRefreshTokenCookie))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.metaData.success").value(false))
        .andExpect(jsonPath("$.metaData.code").value("AUTH_ERROR"));
  }

  // Refresh Api EN

}
