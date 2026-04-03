package org.example.nextstepbackend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.nextstepbackend.comm.constants.ValidateMessageConst;
import org.example.nextstepbackend.dto.request.UserUpdateRequest;
import org.example.nextstepbackend.enums.MessageConst;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  private static final String DOMAIN_API_USERS = "/users/me";
  private static final String DOMAIN_API_USER_BY_ID = "/users/{id}";
  private static final String EMAIL_SUCCESS = "phiduymanh@gmail.com";
  private static final String EMAIL_NOT_FOUND = "mp2005@gmail.com";
  private static final String EMAIL_USER_NOT_ADMIN = "user@gmail.com";

  // Get User Me ST
  /** Test getting current user info successfully */
  @Test
  @WithMockUser(username = EMAIL_SUCCESS)
  @AuthIntegrationTest.WithAuthUser
  void login_success() throws Exception {

    mockMvc
        .perform(get(DOMAIN_API_USERS).contentType(MediaType.APPLICATION_JSON))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.email").value(EMAIL_SUCCESS));
  }

  /** Test getting current user info when user not found */
  @Test
  @WithMockUser(username = EMAIL_NOT_FOUND)
  @AuthIntegrationTest.WithAuthUser
  void login_user_not_found() throws Exception {
    mockMvc
        .perform(get(DOMAIN_API_USERS).contentType(MediaType.APPLICATION_JSON))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isUnauthorized())
        .andExpect(
            jsonPath("$.metaData.code").value(MessageConst.AUTH_INVALID_CREDENTIALS.getCode()));
  }

  /** Test getting current user info when user not logged in */
  @Test
  void user_not_login() throws Exception {
    mockMvc
        .perform(get(DOMAIN_API_USERS).contentType(MediaType.APPLICATION_JSON))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.metaData.code").value(MessageConst.AUTH_ERROR.getCode()));
  }

  // Get User Me EN

  // Get User By Id ST
  /** Test getting user info by id when user not logged in */
  @Test
  void user_id_case_not_login() throws Exception {
    mockMvc
        .perform(get(DOMAIN_API_USER_BY_ID, 1).contentType(MediaType.APPLICATION_JSON))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.metaData.code").value(MessageConst.AUTH_ERROR.getCode()));
  }

  /** Test getting user info by id when user role is not admin */
  @Test
  @WithMockUser(username = EMAIL_USER_NOT_ADMIN)
  @AuthIntegrationTest.WithAuthUser
  void login_with_role_not_admin() throws Exception {
    mockMvc
        .perform(get(DOMAIN_API_USER_BY_ID, 1).contentType(MediaType.APPLICATION_JSON))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.metaData.code").value(MessageConst.USER_ACCESS_DENIED.getCode()));
  }

  /** Test getting user info by id successfully with admin role */
  @Test
  @WithMockUser(
      username = EMAIL_SUCCESS,
      roles = {"ADMIN"})
  @AuthIntegrationTest.WithAuthUser
  void login_with_role_admin_success() throws Exception {
    mockMvc
        .perform(get(DOMAIN_API_USER_BY_ID, 1).contentType(MediaType.APPLICATION_JSON))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.metaData.success").value(true));
  }

  /** Test getting user info by id when user not found with admin role */
  @Test
  @WithMockUser(
      username = EMAIL_NOT_FOUND,
      roles = {"ADMIN"})
  void login_with_user_not_found() throws Exception {
    mockMvc
        .perform(get(DOMAIN_API_USER_BY_ID, 1).contentType(MediaType.APPLICATION_JSON))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.metaData.success").value(false))
        .andExpect(jsonPath("$.metaData.code").value("RESOURCE_NOT_FOUND"));
  }

  // Get User By Id EN

  @Test
  void patchUserMe_whenNotAuthenticated_shouldReturn401() throws Exception {
    mockMvc
        .perform(patch(DOMAIN_API_USERS).contentType(MediaType.APPLICATION_JSON))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.metaData.code").value(MessageConst.AUTH_ERROR.getCode()));
  }

  @Test
  @WithUserDetails(value = EMAIL_SUCCESS)
  @AuthIntegrationTest.WithAuthUser
  void patchUserMe_whenAuthenticated_shouldSuccess() throws Exception {

    UserUpdateRequest updateRequest = new UserUpdateRequest("Manh Update", "0923456789");

    mockMvc
        .perform(
            patch(DOMAIN_API_USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.metaData.success").value(true))
        .andExpect(jsonPath("$.metaData.code").value(MessageConst.USER_UPDATE_SUCCESS.getCode()));
  }

  @Test
  @WithUserDetails(value = EMAIL_SUCCESS)
  @AuthIntegrationTest.WithAuthUser
  void patchUserMe_whenInvalidBody_shouldReturn400() throws Exception {
    UserUpdateRequest updateRequest = new UserUpdateRequest("Manh Update", "01234567899");

    mockMvc
        .perform(
            patch(DOMAIN_API_USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.metaData.success").value(false))
        .andExpect(jsonPath("$.metaData.code").value(ValidateMessageConst.VALIDATE_CODE))
        .andExpect(
            jsonPath("$.metaData.errors", Matchers.hasItem(ValidateMessageConst.PHONE_VALID)));
  }
}
