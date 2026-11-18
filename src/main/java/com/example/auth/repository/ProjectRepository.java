package com.example.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.auth.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
