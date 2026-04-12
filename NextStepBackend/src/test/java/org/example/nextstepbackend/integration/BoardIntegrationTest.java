package org.example.nextstepbackend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.nextstepbackend.dto.request.BoardRequest;
import org.example.nextstepbackend.entity.User;
import org.example.nextstepbackend.entity.Workspace;
import org.example.nextstepbackend.enums.Visibility;
import org.example.nextstepbackend.repository.WorkSpaceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

//@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BoardIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private WorkSpaceRepository workspaceRepository;

  private static final String EMAIL_SUCCESS = "phiduymanh@gmail.com";

  private static final String DOMAIN_API_BOARD = "/board";

  @Test
  @Transactional
  @WithUserDetails(value = EMAIL_SUCCESS)
  @AuthIntegrationTest.WithAuthUser
  void create_board_success() throws Exception {

    Workspace ws = new Workspace();
    ws.setSlug("workspace-1");
    ws.setName("Test WS");
    ws.setCreatedBy(User.builder().build().builder().id(1).build());
    workspaceRepository.save(ws);

    BoardRequest request = new BoardRequest("Board 1", "desc", "#ffffff", null, Visibility.PUBLIC);

    mockMvc
        .perform(
            post(DOMAIN_API_BOARD + "/{slug}", "workspace-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.metaData.success").value(true));
  }

  @Test
  @WithUserDetails(value = EMAIL_SUCCESS)
  @AuthIntegrationTest.WithAuthUser
  void create_board_workspace_not_found() throws Exception {

    BoardRequest request = new BoardRequest("Board 1", "desc", "#ffffff", null, Visibility.PUBLIC);

    mockMvc
        .perform(
            post(DOMAIN_API_BOARD + "/{slug}", "not-exist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  @WithUserDetails(value = EMAIL_SUCCESS)
  @AuthIntegrationTest.WithAuthUser
  void create_board_same_name_should_auto_increment_slug() throws Exception {

    Workspace ws = new Workspace();
    ws.setSlug("workspace-1");
    ws.setName("Test WS");
    ws.setCreatedBy(User.builder().id(1).build());
    workspaceRepository.save(ws);

    BoardRequest request =
        new BoardRequest("Same Name", "desc", "#ffffff", null, Visibility.PUBLIC);

    // First -> success
    mockMvc
        .perform(
            post(DOMAIN_API_BOARD + "/{slug}", "workspace-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    // Second -> success
    mockMvc
        .perform(
            post(DOMAIN_API_BOARD + "/{slug}", "workspace-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }

  @Test
  @WithUserDetails(value = EMAIL_SUCCESS)
  @AuthIntegrationTest.WithAuthUser
  void create_board_name_blank() throws Exception {

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
            post(DOMAIN_API_BOARD + "/{slug}", "workspace-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
        .andExpect(status().isBadRequest());
  }

  @Test
  void create_board_unauthorized() throws Exception {

    BoardRequest request = new BoardRequest("Board 1", "desc", "#ffffff", null, Visibility.PUBLIC);

    mockMvc
        .perform(
            post(DOMAIN_API_BOARD + "/{slug}", "workspace-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }
}
