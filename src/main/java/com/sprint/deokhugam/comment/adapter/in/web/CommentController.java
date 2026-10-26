package com.sprint.deokhugam.comment.adapter.in.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentCreateRequest;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentDto;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentListRequest;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentUpdateRequest;
import com.sprint.deokhugam.comment.application.port.in.CommentUseCase;
import com.sprint.deokhugam.common.context.user.RequestUserId;
import com.sprint.deokhugam.common.pagination.CursorPageResponse;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController implements CommentApi {

    private final CommentUseCase commentUseCase;

    //댓글 등록 API
    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@Valid @RequestBody CommentCreateRequest request) {
        return commentUseCase.create(request);
    }

    //댓글 목록 조회
    @Override
    @GetMapping
    public CursorPageResponse<CommentDto> getList(@Valid CommentListRequest request) {
        return commentUseCase.getList(request);
    }

    //댓글 수정 API
    @Override
    @PatchMapping("/{commentId}")
    public CommentDto update(
        @PathVariable UUID commentId,
        @Parameter(hidden = true) @RequestUserId UUID requestUserId,
        @Valid @RequestBody CommentUpdateRequest request
    ) {
        return commentUseCase.update(commentId, requestUserId, request);
    }

}
