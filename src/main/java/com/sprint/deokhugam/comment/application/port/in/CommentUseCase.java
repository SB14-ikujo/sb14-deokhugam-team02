package com.sprint.deokhugam.comment.application.port.in;

import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentCreateRequest;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentDto;

public interface CommentUseCase {

    CommentDto create(CommentCreateRequest request);
}

