package org.example.nextstepbackend.repository;

import java.util.List;
import java.util.Optional;
import org.example.nextstepbackend.dto.request.UserSearchResponse;
import org.example.nextstepbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Integer> {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  @Query(
      """
    SELECT new org.example.nextstepbackend.dto.request.UserSearchResponse(
        u.id, u.fullName, u.email, u.avatarUrl
    )
    FROM User u
    WHERE (LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND u.id NOT IN (
          SELECT wm.user.id
          FROM WorkspaceMember wm
          WHERE wm.workspace.slug = :workspaceId
      )
""")
  List<UserSearchResponse> searchUser(String keyword, String workspaceId);
}
