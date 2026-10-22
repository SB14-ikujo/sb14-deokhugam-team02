package com.sprint.deokhugam.comment.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.sprint.deokhugam.comment.domain.Comment;

public interface CommentRepositoryCustom {

    // after/afterId가 null이면 첫 페이지, 있으면 해당 커서 이후(더 오래된) 댓글을 조회
    List<Comment> findByCursor(UUID reviewId, Instant after, UUID afterId, int size);
}
