package com.sprint.deokhugam.comment.application.port.in;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentCreateRequest;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentDto;
import com.sprint.deokhugam.comment.application.port.out.CommentRepositoryPort;
import com.sprint.deokhugam.comment.domain.Comment;
import com.sprint.deokhugam.comment.domain.exception.CommentErrorCode;
import com.sprint.deokhugam.comment.domain.exception.CommentException;
import com.sprint.deokhugam.review.application.port.out.ReviewRepositoryPort;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.user.application.port.out.UserRepositoryPort;
import com.sprint.deokhugam.user.domain.User;

@Service
@RequiredArgsConstructor
public class CommentService implements CommentUseCase {

    private final CommentRepositoryPort commentRepository;
    private final ReviewRepositoryPort reviewRepository;
    private final UserRepositoryPort userRepository;

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

        return CommentDto.from(comment);
    }
}
