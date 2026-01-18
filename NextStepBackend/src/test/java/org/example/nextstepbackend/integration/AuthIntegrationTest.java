package org.example.nextstepbackend.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.example.nextstepbackend.comm.constants.Const;
import org.example.nextstepbackend.comm.constants.ValidateMessageConst;
import org.example.nextstepbackend.dto.request.LoginRequest;
import org.example.nextstepbackend.enums.MessageConst;
import org.example.nextstepbackend.utils.JwtUtil;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JwtUtil jwtUtil;

  @SuppressWarnings("java:S2699")
  @Test
  void context_loads() {}

  /** Test login success case */
  @Test
  @Sql(
      scripts = "/db/sql/modules/auth/insert-user.sql",
      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/db/sql/clean-up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  void login_success_should_return_token() throws Exception {
    LoginRequest loginRequest = new LoginRequest("phiduymanh@gmail.com", "1");

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.metaData.success").value(true))
        .andExpect(jsonPath("$.metaData.code").value(MessageConst.AUTH_LOGIN_SUCCESS.getCode()))
        .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
        .andExpect(
            jsonPath("$.data.accessToken")
                .value(org.hamcrest.Matchers.startsWith("ey"))) // Check format JWT access token
        .andExpect(cookie().exists(Const.TEXT_REFRESH_TOKEN))
        .andExpect(
            cookie()
                .value(
                    Const.TEXT_REFRESH_TOKEN,
                    Matchers.startsWith("ey"))); // Check format JWT refresh token
  }

  /** Test login fail case - validate json body */
  @Test
  @Sql(
      scripts = "/db/sql/modules/auth/insert-user.sql",
      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/db/sql/clean-up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  void login_fail_validate_json_body() throws Exception {
    LoginRequest loginRequest = new LoginRequest("", "");

    mockMvc
        .perform(
            post("/auth/login")
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
  @Sql(
      scripts = "/db/sql/modules/auth/insert-user.sql",
      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/db/sql/clean-up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  void login_fail_wrong_password() throws Exception {
    LoginRequest loginRequest = new LoginRequest("phiduymanh@gmail.com", "wrong_password");

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.metaData.success").value(false))
        .andExpect(
            jsonPath("$.metaData.code").value(MessageConst.AUTH_INVALID_CREDENTIALS.getCode()));
  }

  /** Test login fail case - unregistered email */
  @Test
  @Sql(
      scripts = "/db/sql/modules/auth/insert-user.sql",
      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/db/sql/clean-up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  void login_fail_unregistered_email() throws Exception {
    LoginRequest loginRequest = new LoginRequest("manhphi@gmail.com", "1");

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.metaData.success").value(false))
        .andExpect(
            jsonPath("$.metaData.code").value(MessageConst.AUTH_INVALID_CREDENTIALS.getCode()));
  }

  /** Verify cookie attributes */
  @Test
  @Sql(
      scripts = "/db/sql/modules/auth/insert-user.sql",
      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/db/sql/clean-up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  void login_success_verify_cookie_attributes() throws Exception {
    LoginRequest loginRequest = new LoginRequest("phiduymanh@gmail.com", "1");

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(cookie().exists(Const.TEXT_REFRESH_TOKEN))
        .andExpect(cookie().httpOnly(Const.TEXT_REFRESH_TOKEN, true))
        .andExpect(cookie().secure(Const.TEXT_REFRESH_TOKEN, false)) // deploy HTTPS -> true
        .andExpect(cookie().path(Const.TEXT_REFRESH_TOKEN, "/"))
        .andExpect(cookie().maxAge(Const.TEXT_REFRESH_TOKEN, 7 * 24 * 60 * 60))
        .andExpect(cookie().attribute(Const.TEXT_REFRESH_TOKEN, "SameSite", "Strict"));
  }

  /** verify jwt claims in access token */
  @Test
  @Sql(
      scripts = "/db/sql/modules/auth/insert-user.sql",
      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/db/sql/clean-up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  void login_success_verify_jwt_claims_in_access_token() throws Exception {
    String email = "phiduymanh@gmail.com";
    LoginRequest loginRequest = new LoginRequest(email, "1");

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(
            result -> {
              String responseBody = result.getResponse().getContentAsString();
              String accessToken = JsonPath.read(responseBody, "$.data.accessToken");

              UserDetails userDetails =
                  User.withUsername(email).password("1").authorities("USER").build();

              assertTrue(jwtUtil.isAccessTokenValid(accessToken, userDetails), "Token invalid");
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
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.metaData.success").value(false))
        .andExpect(
            jsonPath("$.metaData.code").value(MessageConst.AUTH_DISABLED_ACCOUNT.getCode()));
  }
}
