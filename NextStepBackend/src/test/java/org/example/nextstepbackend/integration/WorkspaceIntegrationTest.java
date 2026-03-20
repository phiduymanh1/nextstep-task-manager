package org.example.nextstepbackend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.nextstepbackend.comm.constants.ValidateMessageConst;
import org.example.nextstepbackend.dto.request.WorkSpaceRequest;
import org.example.nextstepbackend.dto.request.WorkSpaceUpdateRequest;
import org.example.nextstepbackend.entity.User;
import org.example.nextstepbackend.entity.Workspace;
import org.example.nextstepbackend.enums.MessageConst;
import org.example.nextstepbackend.enums.Visibility;
import org.example.nextstepbackend.repository.WorkSpaceRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkspaceIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private WorkSpaceRepository workspaceRepository;

  private static final String DOMAIN_API_WORKSPACE = "/work-space/me";

  private static final String EMAIL_SUCCESS = "phiduymanh@gmail.com";

  @Test
  @WithUserDetails(value = EMAIL_SUCCESS)
  @AuthIntegrationTest.WithAuthUser
  void create_workspace_success() throws Exception {

    WorkSpaceRequest request = new WorkSpaceRequest("workspace1", "desc", Visibility.PUBLIC);

    mockMvc
        .perform(
            post(DOMAIN_API_WORKSPACE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.metaData.code").value(MessageConst.WORK_SPACE_CREATE_SUCCESS.getCode()));
  }

  @Test
  @WithUserDetails(value = EMAIL_SUCCESS)
  @AuthIntegrationTest.WithAuthUser
  void create_workspace_duplicate_slug() throws Exception {

    WorkSpaceRequest request = new WorkSpaceRequest("workspace1", "desc", Visibility.PUBLIC);

    // First call -> success
    mockMvc
        .perform(
            post(DOMAIN_API_WORKSPACE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    // Second call -> duplicate slug
    mockMvc
        .perform(
            post(DOMAIN_API_WORKSPACE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.metaData.message").value(ValidateMessageConst.SLUG_DUPLICATE));
  }

  @Test
  @WithUserDetails(value = EMAIL_SUCCESS)
  @AuthIntegrationTest.WithAuthUser
  void create_workspace_name_invalid() throws Exception {

    WorkSpaceRequest request =
        new WorkSpaceRequest(
            "abc", // smaller 4 character
            "desc",
            Visibility.PUBLIC);

    mockMvc
        .perform(
            post(DOMAIN_API_WORKSPACE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath(
                "$.metaData.errors",
                Matchers.hasItem("The name must be between 4 and 20 characters long.")));
  }

  @Test
  @WithUserDetails(value = EMAIL_SUCCESS)
  @AuthIntegrationTest.WithAuthUser
  void create_workspace_name_blank() throws Exception {

    String request =
        """
        {
          "name": "",
          "description": "desc",
          "visibility": "PUBLIC"
        }
    """;

    mockMvc
        .perform(
            post(DOMAIN_API_WORKSPACE).contentType(MediaType.APPLICATION_JSON).content(request))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.metaData.errors", Matchers.hasItem(ValidateMessageConst.NAME_REQUIRED)));
  }

  @Test
  @WithUserDetails(value = EMAIL_SUCCESS)
  @AuthIntegrationTest.WithAuthUser
  void create_workspace_missing_visibility() throws Exception {

    String request =
        """
        {
          "name": "workspace1",
          "description": "desc"
        }
    """;

    mockMvc
        .perform(
            post(DOMAIN_API_WORKSPACE).contentType(MediaType.APPLICATION_JSON).content(request))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath(
                "$.metaData.errors", Matchers.hasItem(ValidateMessageConst.VISIBILITY_REQUIRED)));
  }

  @Test
  void create_workspace_unauthorized() throws Exception {

    WorkSpaceRequest request = new WorkSpaceRequest("workspace1", "desc", Visibility.PUBLIC);

    mockMvc
        .perform(
            post(DOMAIN_API_WORKSPACE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithUserDetails(value = EMAIL_SUCCESS)
  @AuthIntegrationTest.WithAuthUser
  void get_workspace_me_success() throws Exception {

    mockMvc
        .perform(get(DOMAIN_API_WORKSPACE))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.metaData.success").value(true));
  }

  @Test
  void get_workspace_me_unauthorized() throws Exception {

    mockMvc
        .perform(get(DOMAIN_API_WORKSPACE))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithUserDetails(value = EMAIL_SUCCESS)
  @AuthIntegrationTest.WithAuthUser
  void update_workspace_not_found() throws Exception {

    WorkSpaceUpdateRequest request =
        new WorkSpaceUpdateRequest("newName", "newDesc", Visibility.PRIVATE);

    mockMvc
        .perform(
            patch(DOMAIN_API_WORKSPACE + "/{slug}", "not-exist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  @WithUserDetails(value = EMAIL_SUCCESS)
  @AuthIntegrationTest.WithAuthUser
  void update_workspace_success() throws Exception {

    Workspace ws = new Workspace();
    ws.setSlug("workspace-1");
    ws.setName("Test WS");
    ws.setCreatedBy(User.builder().id(1).build());
    workspaceRepository.save(ws);

    WorkSpaceUpdateRequest request =
        new WorkSpaceUpdateRequest("newName", "newDesc", Visibility.PRIVATE);

    mockMvc
        .perform(
            patch(DOMAIN_API_WORKSPACE + "/{slug}", "workspace-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.metaData.code").value(MessageConst.WORK_SPACE_UPDATE_SUCCESS.getCode()));
  }

  @Test
  @WithUserDetails(value = EMAIL_SUCCESS)
  @AuthIntegrationTest.WithAuthUser
  void update_workspace_forbidden() throws Exception {

    Workspace ws = new Workspace();
    ws.setSlug("workspace-of-other-user");
    ws.setName("Test WS");
    ws.setCreatedBy(User.builder().id(2).build());
    workspaceRepository.save(ws);

    WorkSpaceUpdateRequest request =
        new WorkSpaceUpdateRequest("newName", "newDesc", Visibility.PRIVATE);

    mockMvc
        .perform(
            patch(DOMAIN_API_WORKSPACE + "/{slug}", "workspace-of-other-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound()); // hoặc 403 tùy bạn design
  }
}
