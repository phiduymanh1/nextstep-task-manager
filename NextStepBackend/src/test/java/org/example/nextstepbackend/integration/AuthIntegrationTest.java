package org.example.nextstepbackend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.example.nextstepbackend.comm.constants.Const;
import org.example.nextstepbackend.comm.constants.ValidateMessageConst;
import org.example.nextstepbackend.dto.request.ForgotPasswordRequest;
import org.example.nextstepbackend.dto.request.LoginRequest;
import org.example.nextstepbackend.dto.request.RegisterRequest;
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
  private static final String DOMAIN_API_FORGOT_PASSWORD = "/auth/forgot-password";
  private static final String DOMAIN_API_REGISTER = "/auth/register";
  private static final String DOMAIN_API_LOGOUT = "/auth/logout";
  private static final String ACCOUNT_SUCCESS = "phiduymanh@gmail.com";

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
    LoginRequest loginRequest = new LoginRequest(ACCOUNT_SUCCESS, "1");

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
        .andExpect(jsonPath("$.metaData.code").value(ValidateMessageConst.VALIDATE_CODE))
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
    LoginRequest loginRequest = new LoginRequest(ACCOUNT_SUCCESS, "wrong_password");

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
    LoginRequest loginRequest = new LoginRequest(ACCOUNT_SUCCESS, "1");

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
    String email = ACCOUNT_SUCCESS;
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
    LoginRequest loginRequest = new LoginRequest(ACCOUNT_SUCCESS, "1");

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
    LoginRequest loginRequest = new LoginRequest(ACCOUNT_SUCCESS, "1");

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

  // Forgot Password Api ST
  /** Test forgot password success case */
  @Test
  @WithAuthUser
  void forgot_password_success() throws Exception {
    ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest(ACCOUNT_SUCCESS);

    mockMvc
        .perform(
            post(DOMAIN_API_FORGOT_PASSWORD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forgotPasswordRequest)))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.metaData.success").value(true))
        .andExpect(
            jsonPath("$.metaData.code").value(MessageConst.AUTH_FORGOT_PASSWORD_SENT.getCode()));
  }

  /** Test forgot password fail case - empty email */
  @Test
  void forgot_password_fail_empty_email() throws Exception {
    ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest("");

    mockMvc
        .perform(
            post(DOMAIN_API_FORGOT_PASSWORD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forgotPasswordRequest)))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.metaData.success").value(false))
        .andExpect(jsonPath("$.metaData.code").value(ValidateMessageConst.VALIDATE_CODE))
        .andExpect(
            jsonPath("$.metaData.errors", Matchers.hasItem(ValidateMessageConst.EMAIL_REQUIRED)));
  }

  /** Test forgot password fail case - invalid email format */
  @Test
  void forgot_password_fail_invalid_email_format() throws Exception {
    ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest("abc@invalid");

    mockMvc
        .perform(
            post(DOMAIN_API_FORGOT_PASSWORD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forgotPasswordRequest)))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.metaData.success").value(false))
        .andExpect(jsonPath("$.metaData.code").value(ValidateMessageConst.VALIDATE_CODE))
        .andExpect(
            jsonPath("$.metaData.errors", Matchers.hasItem(ValidateMessageConst.EMAIL_VALID)));
  }

  /** Test forgot password fail case - email not found */
  @Test
  void forgot_password_fail_email_not_found() throws Exception {
    ForgotPasswordRequest forgotPasswordRequest =
        new ForgotPasswordRequest("phiduymanh2@gmail.com");

    mockMvc
        .perform(
            post(DOMAIN_API_FORGOT_PASSWORD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forgotPasswordRequest)))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.metaData.success").value(true))
        .andExpect(
            jsonPath("$.metaData.code").value(MessageConst.AUTH_FORGOT_PASSWORD_SENT.getCode()));
  }

  /** Test forgot password fail case - account inactive */
  @Test
  @Sql(
      scripts = "/db/sql/modules/auth/insert-user-inactive.sql",
      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/db/sql/clean-up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  void forgot_password_fail_account_inactive() throws Exception {
    ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest(ACCOUNT_SUCCESS);
    mockMvc
        .perform(
            post(DOMAIN_API_FORGOT_PASSWORD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forgotPasswordRequest)))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.metaData.success").value(true))
        .andExpect(
            jsonPath("$.metaData.code").value(MessageConst.AUTH_FORGOT_PASSWORD_SENT.getCode()));
  }

  // Forgot Password Api EN

  // Register Api ST
  /** Test successful registration with valid data. */
  @Test
  void register_success() throws Exception {
    RegisterRequest request =
        new RegisterRequest(
            "phiduymanh", "manhphi123@gmail.com", "Manh992005@", "Phí Duy Mạnh", "0123456789");

    mockMvc
        .perform(
            post(DOMAIN_API_REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.metaData.success").value(true))
        .andExpect(jsonPath("$.metaData.code").value(MessageConst.AUTH_REGISTER_SUCCESS.getCode()));
  }

  /** Test registration failure due to invalid input validation. */
  @Test
  void register_validation_fail() throws Exception {
    RegisterRequest request = new RegisterRequest("test@gmail.com", "", "", "", "");
    mockMvc
        .perform(
            post(DOMAIN_API_REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.metaData.success").value(false))
        .andExpect(jsonPath("$.metaData.errors").isArray());
  }

  /** Test registration failure when the email is already registered. */
  @Test
  @WithAuthUser
  void register_email_already_exists() throws Exception {
    RegisterRequest request =
        new RegisterRequest(
            "phiduymanh", ACCOUNT_SUCCESS, "Manh992005@", "Phí Duy Mạnh", "0123456789");

    mockMvc
        .perform(
            post(DOMAIN_API_REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.metaData.success").value(false))
        .andExpect(jsonPath("$.metaData.code").value("CONFLICT"));
  }

  // Register API EN

  // Logout Api ST

  /** Test successful logout. */
  @Test
  void logout_success() throws Exception {
    String dummyRefreshToken = "valid.refresh.token.here";
    Cookie refreshTokenCookie = new Cookie(Const.TEXT_REFRESH_TOKEN, dummyRefreshToken);

    mockMvc
        .perform(post(DOMAIN_API_LOGOUT).cookie(refreshTokenCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.metaData.success").value(true))
        .andExpect(jsonPath("$.metaData.code").value(MessageConst.AUTH_LOGOUT_SUCCESS.getCode()));
  }

  /** Test logout failure when the refresh token cookie is not present. */
  @Test
  void logout_notExistsCookie() throws Exception {
    mockMvc
        .perform(post(DOMAIN_API_LOGOUT))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.metaData.success").value(true))
        .andExpect(jsonPath("$.metaData.code").value(MessageConst.AUTH_LOGOUT_SUCCESS.getCode()));
  }

  // TODO: Token đã blacklist
  // Logout Api EN
}
