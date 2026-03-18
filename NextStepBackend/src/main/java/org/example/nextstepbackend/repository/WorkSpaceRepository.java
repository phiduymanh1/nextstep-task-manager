package org.example.nextstepbackend.repository;

import org.example.nextstepbackend.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkSpaceRepository extends JpaRepository<Workspace,Integer> {

    public Workspace findBySlug(String slug);
}
