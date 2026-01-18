package org.example.nextstepbackend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.nextstepbackend.comm.constants.ValidateMessageConst;
import org.example.nextstepbackend.dto.request.LoginRequest;
import org.example.nextstepbackend.enums.MessageConst;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

  @SuppressWarnings("java:S2699")
  @Test
  void context_loads() {}

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
        .andExpect(cookie().exists("refreshToken"))
        .andExpect(
            cookie()
                .value(
                    "refreshToken", Matchers.startsWith("ey"))); // Check format JWT refresh token
  }

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
}
