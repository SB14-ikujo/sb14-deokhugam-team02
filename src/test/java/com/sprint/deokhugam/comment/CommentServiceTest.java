package com.sprint.deokhugam.comment;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentCreateRequest;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentDto;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentListRequest;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentUpdateRequest;
import com.sprint.deokhugam.comment.adapter.in.web.mapper.CommentMapper;
import com.sprint.deokhugam.comment.application.port.in.CommentApplicationService;
import com.sprint.deokhugam.comment.application.port.out.CommentRepositoryPort;
import com.sprint.deokhugam.comment.domain.Comment;
import com.sprint.deokhugam.comment.domain.exception.CommentErrorCode;
import com.sprint.deokhugam.comment.domain.exception.CommentException;
import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.review.application.port.out.ReviewRepositoryPort;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.user.application.port.out.UserRepositoryPort;
import com.sprint.deokhugam.user.domain.User;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepositoryPort commentRepository;
    @Mock
    private ReviewRepositoryPort reviewRepository;
    @Mock
    private UserRepositoryPort userRepository;
    @Spy
    private CommentMapper commentMapper = Mappers.getMapper(CommentMapper.class);

    @InjectMocks
    private CommentApplicationService commentService;

    private final UUID reviewId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final CommentCreateRequest request =
        new CommentCreateRequest(reviewId, userId, "좋은 리뷰네요!");

    @Test
    @DisplayName("댓글 등록 성공")
    void create_success() {
        Review review = mock(Review.class);
        User user = mock(User.class);
        given(review.isDeleted()).willReturn(false);
        given(review.getId()).willReturn(reviewId);
        given(user.isDeleted()).willReturn(false);
        given(user.getId()).willReturn(userId);
        given(user.getNickname()).willReturn("닉네임");
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(commentRepository.save(any(Comment.class))).willAnswer(inv -> inv.getArgument(0));

        CommentDto result = commentService.create(request);

        assertThat(result.reviewId()).isEqualTo(reviewId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.userNickname()).isEqualTo("닉네임");
        assertThat(result.content()).isEqualTo("좋은 리뷰네요!");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("존재하지 않는 리뷰면 COMMENT_REVIEW_NOT_FOUND")
    void create_reviewNotFound() {
        given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create(request))
            .isInstanceOf(CommentException.class)
            .extracting("errorCode")
            .isEqualTo(CommentErrorCode.COMMENT_REVIEW_NOT_FOUND);

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("논리 삭제된 리뷰면 COMMENT_REVIEW_NOT_FOUND")
    void create_deletedReview() {
        Review review = mock(Review.class);
        given(review.isDeleted()).willReturn(true);
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

        assertThatThrownBy(() -> commentService.create(request))
            .isInstanceOf(CommentException.class)
            .extracting("errorCode")
            .isEqualTo(CommentErrorCode.COMMENT_REVIEW_NOT_FOUND);

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 사용자면 COMMENT_USER_NOT_FOUND")
    void create_userNotFound() {
        Review review = mock(Review.class);
        given(review.isDeleted()).willReturn(false);
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create(request))
            .isInstanceOf(CommentException.class)
            .extracting("errorCode")
            .isEqualTo(CommentErrorCode.COMMENT_USER_NOT_FOUND);

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("논리 삭제된 사용자면 COMMENT_USER_NOT_FOUND")
    void create_deletedUser() {
        Review review = mock(Review.class);
        User user = mock(User.class);
        given(review.isDeleted()).willReturn(false);
        given(user.isDeleted()).willReturn(true);
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> commentService.create(request))
            .isInstanceOf(CommentException.class)
            .extracting("errorCode")
            .isEqualTo(CommentErrorCode.COMMENT_USER_NOT_FOUND);

        verify(commentRepository, never()).save(any());
    }

    // ===== 댓글 목록 조회 =====

    private void givenActiveReview() {
        Review review = mock(Review.class);
        given(review.isDeleted()).willReturn(false);
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    }

    private Comment mockComment(UUID id, Instant createdAt, String content) {
        Review review = mock(Review.class);
        User user = mock(User.class);
        given(review.getId()).willReturn(reviewId);
        given(user.getId()).willReturn(userId);
        given(user.getNickname()).willReturn("닉네임");

        Comment comment = mock(Comment.class);
        given(comment.getId()).willReturn(id);
        given(comment.getCreatedAt()).willReturn(createdAt);
        given(comment.getContent()).willReturn(content);
        given(comment.getReview()).willReturn(review);
        given(comment.getUser()).willReturn(user);
        return comment;
    }

    @Test
    @DisplayName("목록 조회 - 다음 페이지가 있으면 limit개만 반환하고 마지막 댓글로 커서를 만든다")
    void getList_hasNext() {
        givenActiveReview();
        Instant t1 = Instant.parse("2024-01-03T00:00:00Z");
        Instant t2 = Instant.parse("2024-01-02T00:00:00Z");
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        // limit(2) + 1 = 3개를 조회해 온 상황
        Comment c1 = mockComment(id1, t1, "3번째");
        Comment c2 = mockComment(id2, t2, "2번째");
        Comment extra = mock(Comment.class);
        given(commentRepository.findByCursor(reviewId, null, null, Sort.Direction.DESC, 3))
            .willReturn(List.of(c1, c2, extra));
        given(commentRepository.countByReviewIdAndIsDeletedFalse(reviewId)).willReturn(5L);

        CursorPageResponse<CommentDto> result = commentService.getList(
            new CommentListRequest(reviewId, Sort.Direction.DESC, null, null, 2));

        assertThat(result.content()).extracting(CommentDto::content).containsExactly("3번째", "2번째");
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo(id2.toString());
        assertThat(result.nextAfter()).isEqualTo(t2);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(5L);
    }

    @Test
    @DisplayName("목록 조회 - 마지막 페이지면 hasNext=false, 커서는 null")
    void getList_lastPage() {
        givenActiveReview();
        Comment c1 = mockComment(UUID.randomUUID(), Instant.parse("2024-01-01T00:00:00Z"), "하나");
        given(commentRepository.findByCursor(reviewId, null, null, Sort.Direction.DESC, 51))
            .willReturn(List.of(c1));
        given(commentRepository.countByReviewIdAndIsDeletedFalse(reviewId)).willReturn(1L);

        // limit 미지정 → 기본값 50
        CursorPageResponse<CommentDto> result = commentService.getList(
            new CommentListRequest(reviewId, null, null, null, null));

        assertThat(result.content()).hasSize(1);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
        assertThat(result.nextAfter()).isNull();
        assertThat(result.totalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("목록 조회 - 댓글이 없으면 빈 목록을 반환한다")
    void getList_empty() {
        givenActiveReview();
        given(commentRepository.findByCursor(any(), any(), any(), any(), anyInt()))
            .willReturn(List.of());
        given(commentRepository.countByReviewIdAndIsDeletedFalse(reviewId)).willReturn(0L);

        CursorPageResponse<CommentDto> result = commentService.getList(
            new CommentListRequest(reviewId, Sort.Direction.ASC, null, null, 10));

        assertThat(result.content()).isEmpty();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.totalElements()).isZero();
    }

    @Test
    @DisplayName("목록 조회 - cursor(댓글 ID)와 after(생성 시간)를 레포지토리에 그대로 전달한다")
    void getList_passesCursor() {
        givenActiveReview();
        UUID cursorId = UUID.randomUUID();
        Instant after = Instant.parse("2024-01-01T00:00:00Z");
        given(commentRepository.findByCursor(reviewId, after, cursorId, Sort.Direction.ASC, 11))
            .willReturn(List.of());
        given(commentRepository.countByReviewIdAndIsDeletedFalse(reviewId)).willReturn(0L);

        commentService.getList(
            new CommentListRequest(reviewId, Sort.Direction.ASC, cursorId.toString(), after, 10));

        verify(commentRepository).findByCursor(reviewId, after, cursorId, Sort.Direction.ASC, 11);
    }

    @Test
    @DisplayName("목록 조회 - cursor가 UUID 형식이 아니면 COMMENT_INVALID_CURSOR")
    void getList_invalidCursor() {
        assertThatThrownBy(() -> commentService.getList(
            new CommentListRequest(reviewId, Sort.Direction.DESC, "not-a-uuid", Instant.now(), 10)))
            .isInstanceOf(CommentException.class)
            .extracting("errorCode")
            .isEqualTo(CommentErrorCode.COMMENT_INVALID_CURSOR);

        verify(commentRepository, never()).findByCursor(any(), any(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("목록 조회 - 존재하지 않는 리뷰면 COMMENT_REVIEW_NOT_FOUND")
    void getList_reviewNotFound() {
        given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getList(
            new CommentListRequest(reviewId, Sort.Direction.DESC, null, null, 10)))
            .isInstanceOf(CommentException.class)
            .extracting("errorCode")
            .isEqualTo(CommentErrorCode.COMMENT_REVIEW_NOT_FOUND);

        verify(commentRepository, never()).findByCursor(any(), any(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("목록 조회 - 논리 삭제된 리뷰면 COMMENT_REVIEW_NOT_FOUND")
    void getList_deletedReview() {
        Review review = mock(Review.class);
        given(review.isDeleted()).willReturn(true);
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

        assertThatThrownBy(() -> commentService.getList(
            new CommentListRequest(reviewId, Sort.Direction.DESC, null, null, 10)))
            .isInstanceOf(CommentException.class)
            .extracting("errorCode")
            .isEqualTo(CommentErrorCode.COMMENT_REVIEW_NOT_FOUND);

        verify(commentRepository, never()).findByCursor(any(), any(), any(), any(), anyInt());
    }

    // ===== 댓글 수정 =====

    private final UUID commentId = UUID.randomUUID();
    private final CommentUpdateRequest updateRequest = new CommentUpdateRequest("수정된 댓글");

    private Comment givenCommentWrittenBy(UUID authorId) {
        Review review = mock(Review.class);
        User author = mock(User.class);
        lenient().when(review.getId()).thenReturn(reviewId);
        given(author.getId()).willReturn(authorId);
        lenient().when(author.getNickname()).thenReturn("닉네임");

        Comment comment = Comment.create(review, author, "수정 전");
        given(commentRepository.findByIdAndIsDeletedFalse(commentId)).willReturn(Optional.of(comment));
        return comment;
    }

    @Test
    @DisplayName("댓글 수정 성공 - 작성자 본인이면 내용이 바뀌고 수정된 DTO를 반환한다")
    void update_success() {
        Comment comment = givenCommentWrittenBy(userId);

        CommentDto result = commentService.update(commentId, userId, updateRequest);

        assertThat(comment.getContent()).isEqualTo("수정된 댓글");
        assertThat(result.content()).isEqualTo("수정된 댓글");
        assertThat(result.reviewId()).isEqualTo(reviewId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.userNickname()).isEqualTo("닉네임");
        verify(commentRepository).flush();
    }

    @Test
    @DisplayName("댓글 수정 - 존재하지 않거나 논리 삭제된 댓글이면 COMMENT_NOT_FOUND")
    void update_commentNotFound() {
        given(commentRepository.findByIdAndIsDeletedFalse(commentId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.update(commentId, userId, updateRequest))
            .isInstanceOf(CommentException.class)
            .extracting("errorCode")
            .isEqualTo(CommentErrorCode.COMMENT_NOT_FOUND);

        verify(commentRepository, never()).flush();
    }

    @Test
    @DisplayName("댓글 수정 - 작성자가 아니면 COMMENT_FORBIDDEN이고 내용은 바뀌지 않는다")
    void update_forbidden() {
        Comment comment = givenCommentWrittenBy(UUID.randomUUID());

        assertThatThrownBy(() -> commentService.update(commentId, userId, updateRequest))
            .isInstanceOf(CommentException.class)
            .extracting("errorCode")
            .isEqualTo(CommentErrorCode.COMMENT_FORBIDDEN);

        assertThat(comment.getContent()).isEqualTo("수정 전");
        verify(commentRepository, never()).flush();
    }
}
