package com.sprint.deokhugam.comment.application.port.in;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentCreateRequest;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentDto;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentListRequest;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentUpdateRequest;
import com.sprint.deokhugam.comment.adapter.in.web.mapper.CommentMapper;
import com.sprint.deokhugam.comment.application.port.out.CommentRepositoryPort;
import com.sprint.deokhugam.comment.domain.Comment;
import com.sprint.deokhugam.comment.domain.exception.CommentErrorCode;
import com.sprint.deokhugam.comment.domain.exception.CommentException;
import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.common.util.SliceUtils;
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
        log.debug("댓글목록 조회 요청: reviewId={}, direction={}, cursor={}, after={}, limit={}",
            request.reviewId(), request.direction(), request.cursor(), request.after(), request.limit());

        int limit = request.limit();
        UUID cursorId = parseCursor(request.cursor());

        reviewRepository.findById(request.reviewId())
            .filter(r -> !r.isDeleted())
            .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_REVIEW_NOT_FOUND));

        List<CommentDto> comments = commentRepository.findByCursor(
                request.reviewId(), request.after(), cursorId, request.direction(), limit + 1)
            .stream()
            .map(commentMapper::toDto)
            .toList();

        Slice<CommentDto> slice = SliceUtils.toSlice(comments, limit);

        long total = commentRepository.countByReviewIdAndIsDeletedFalse(request.reviewId());

        return CursorPageResponse.of(
            slice, total,
            dto -> dto.id().toString(),
            CommentDto::createdAt
        );
    }

    @Override
    @Transactional
    public CommentDto update(UUID commentId, UUID requestUserId, CommentUpdateRequest request) {
        // 논리 삭제된 댓글은 "없는 댓글"로 취급
        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
            .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));

        // 작성자 본인만 수정 가능
        if (!comment.getUser().getId().equals(requestUserId)) {
            throw new CommentException(CommentErrorCode.COMMENT_FORBIDDEN);
        }

        comment.updateContent(request.content());
        // 응답에 갱신된 updatedAt(JPA Auditing)이 담기도록 먼저 DB에 반영
        commentRepository.flush();

        return commentMapper.toDto(comment);
    }

    @Override
    @Transactional
    public void softDelete(UUID commentId, UUID requestUserId) {
        // 이미 논리 삭제된 댓글은 "없는 댓글"로 취급 (재삭제 시 404)
        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
            .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));

        // 작성자 본인만 삭제 가능
        if (!comment.getUser().getId().equals(requestUserId)) {
            throw new CommentException(CommentErrorCode.COMMENT_FORBIDDEN);
        }

        // 상태 변경(isDeleted = true)은 더티 체킹으로 트랜잭션 커밋 시 DB에 반영
        comment.softDelete();
        log.debug("댓글 논리 삭제: commentId={}, requestUserId={}", commentId, requestUserId);
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
