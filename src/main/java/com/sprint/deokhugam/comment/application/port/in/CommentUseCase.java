package com.sprint.deokhugam.comment.application.port.in;

import java.util.UUID;

import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentCreateRequest;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentDto;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentListRequest;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentUpdateRequest;
import com.sprint.deokhugam.common.pagination.CursorPageResponse;

public interface CommentUseCase {

    CommentDto create(CommentCreateRequest request); //댓글을 등록

    CursorPageResponse<CommentDto> getList(CommentListRequest request); //리뷰에 달린 댓글 목록을 커서 기반으로 조회

    CommentDto update(UUID commentId, UUID requestUserId, CommentUpdateRequest request); //댓글 내용을 수정

    void softDelete(UUID commentId, UUID requestUserId); //댓글을 논리 삭제

    void hardDelete(UUID commentId, UUID requestUserId); //댓글을 물리 삭제
}

