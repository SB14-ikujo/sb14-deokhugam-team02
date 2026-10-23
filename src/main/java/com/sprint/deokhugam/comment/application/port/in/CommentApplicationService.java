package com.sprint.deokhugam.comment.application.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentCreateRequest;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentDto;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentListRequest;
import com.sprint.deokhugam.comment.adapter.in.web.mapper.CommentMapper;
import com.sprint.deokhugam.comment.application.port.out.CommentRepositoryPort;
import com.sprint.deokhugam.comment.domain.Comment;
import com.sprint.deokhugam.comment.domain.exception.CommentErrorCode;
import com.sprint.deokhugam.comment.domain.exception.CommentException;
import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.review.application.port.out.ReviewRepositoryPort;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.user.application.port.out.UserRepositoryPort;
import com.sprint.deokhugam.user.domain.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentApplicationService implements CommentUseCase {

    private final CommentRepositoryPort commentRepository;
    private final ReviewRepositoryPort reviewRepository;
    private final UserRepositoryPort userRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto create(CommentCreateRequest request) {
        // 논리 삭제된 리뷰에는 댓글을 달 수 없음
        Review review = reviewRepository.findById(request.reviewId())
            .filter(r -> !r.isDeleted())
            .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_REVIEW_NOT_FOUND));

        // 논리 삭제된 사용자는 댓글을 달 수 없음
        User user = userRepository.findById(request.userId())
            .filter(u -> !u.isDeleted())
            .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_USER_NOT_FOUND));

        Comment comment = commentRepository.save(Comment.create(review, user, request.content()));

        return commentMapper.toDto(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<CommentDto> getList(CommentListRequest request) {
        log.debug("댓글 목록 조회 요청: reviewId={}, direction={}, cursor={}, after={}, limit={}",
            request.reviewId(), request.direction(), request.cursor(), request.after(), request.limit());

        int limit = request.limit();
        UUID cursorId = parseCursor(request.cursor());

        // 존재하지 않거나 논리 삭제된 리뷰는 조회 불가
        reviewRepository.findById(request.reviewId())
            .filter(r -> !r.isDeleted())
            .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_REVIEW_NOT_FOUND));

        // hasNext 판단을 위해 limit + 1개를 조회 (논리 삭제된 댓글은 쿼리에서 제외됨)
        List<Comment> rows = commentRepository.findByCursor(
            request.reviewId(), request.after(), cursorId, request.direction(), limit + 1);

        boolean hasNext = rows.size() > limit;
        List<Comment> page = hasNext ? rows.subList(0, limit) : rows;
        List<CommentDto> content = page.stream()
            .map(commentMapper::toDto)
            .toList();

        long total = commentRepository.countByReviewIdAndIsDeletedFalse(request.reviewId());

        if (!hasNext) {
            return new CursorPageResponse<>(content, null, null, content.size(), total, false);
        }

        // 다음 페이지 커서 = 이번 페이지 마지막 댓글의 (id, createdAt)
        Comment last = page.get(page.size() - 1);
        String nextCursor = last.getId().toString();
        Instant nextAfter = last.getCreatedAt();

        return new CursorPageResponse<>(content, nextCursor, nextAfter, content.size(), total, true);
    }

    private UUID parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(cursor);
        } catch (IllegalArgumentException e) {
            throw new CommentException(CommentErrorCode.COMMENT_INVALID_CURSOR);
        }
    }
}
