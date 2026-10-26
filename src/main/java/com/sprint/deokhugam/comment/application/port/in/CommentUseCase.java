package com.sprint.deokhugam.comment.application.port.in;

import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentCreateRequest;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentDto;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentListRequest;
import com.sprint.deokhugam.common.pagination.CursorPageResponse;

public interface CommentUseCase {

    CommentDto create(CommentCreateRequest request); //댓글을 등록

    CursorPageResponse<CommentDto> getList(CommentListRequest request); //리뷰에 달린 댓글 목록을 Cursor 기반으로 조회
}
