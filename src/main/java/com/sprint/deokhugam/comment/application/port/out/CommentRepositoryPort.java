package com.sprint.deokhugam.comment.application.port.out;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sprint.deokhugam.comment.domain.Comment;

public interface CommentRepositoryPort extends JpaRepository<Comment, UUID> {
}
