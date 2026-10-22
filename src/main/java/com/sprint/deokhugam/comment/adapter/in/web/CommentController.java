package com.sprint.deokhugam.comment.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentCreateRequest;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentDto;
import com.sprint.deokhugam.comment.application.port.in.CommentUseCase;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController implements CommentApi {

    private final CommentUseCase commentUseCase;

    @Override
    @PostMapping
    public ResponseEntity<CommentDto> create(@Valid @RequestBody CommentCreateRequest request) {
        CommentDto response = commentUseCase.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
