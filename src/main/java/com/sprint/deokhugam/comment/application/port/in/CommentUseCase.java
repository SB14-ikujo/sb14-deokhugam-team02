package com.sprint.deokhugam.comment.application.port.in;

import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentCreateRequest;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentDto;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentListRequest;
import com.sprint.deokhugam.common.pagination.CursorPageResponse;

public interface CommentUseCase {

    CommentDto create(CommentCreateRequest request);

    CursorPageResponse<CommentDto> getList(CommentListRequest request);
}

