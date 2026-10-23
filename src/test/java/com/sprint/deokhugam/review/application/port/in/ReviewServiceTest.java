// import java.time.LocalDate;
// import java.util.UUID;
//
// import org.junit.jupiter.api.Disabled;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
//
// import com.sprint.deokhugam.book.application.port.in.BookUseCase;
// import com.sprint.deokhugam.book.domain.Book;
// import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewCreateRequest;
// import com.sprint.deokhugam.review.application.port.in.ReviewService;
// import com.sprint.deokhugam.review.application.port.out.ReviewLikeRepositoryPort;
// import com.sprint.deokhugam.review.application.port.out.ReviewRepositoryPort;
// import com.sprint.deokhugam.user.application.port.in.UserUseCase;
// import com.sprint.deokhugam.user.domain.User;
//
// @ExtendWith(MockitoExtension.class)
// class ReviewServiceTest {
//
//     @InjectMocks
//     private ReviewService reviewService;
//
//     @Mock
//     private ReviewRepositoryPort reviewRepositoryPort;
//
//     @Mock
//     private ReviewLikeRepositoryPort reviewLikeRepositoryPort;
//
//     @Mock
//     private UserUseCase userUseCase;
//
//     @Mock
//     private BookUseCase bookUseCase;
//
//     //@Mock
//     // private ReviewMapper reviewMapper;
//
//     @Disabled
//     @Test
//     @DisplayName("[성공] - 올바른 도서 ID와 유저 ID가 들어오면 리뷰가 정상적으로 등록된다.")
//     void createReview_Success() {
//         // given
//         UUID requestUserId = UUID.randomUUID();
//         UUID targetBookId = UUID.randomUUID();
//         ReviewCreateRequest createRequest = new ReviewCreateRequest(requestUserId, targetBookId, "정말 재밌어요!!", 5);
//         User requestUser = User.create("www.spring@naver.com", "스프링좋아", "12345678");
//         Book targetBook = Book.create("JPA에 모든 것", requestUser, "김영한", "JPA에 모든걸 설명해준다", "다산북스"
//             , LocalDate.of(2026, 10, 20), "9788960777330", "http://example.com");
//         // given(userUseCase.findById(requestUser.getId())).willReturn(requestUser);
//         // given(bookUseCase.findById(targetBookId)).willReturn(targetBook);
//         // given(reviewRepositoryPort.findByUserAndBook(requestUser, targetBook)).willReturn(Optional.empty());
//
//         // when
//         // ReviewDto review = reviewService.createReview(createRequest);
//
//         // then
//         // 5
//     }
//
//     @Disabled
//     @Test
//     @DisplayName("[실패] - 존재하지 않는 도서 ID로 리뷰를 생성하면 예외가 발생한다.")
//     void createReview_Fail_BookNotFound() {
//         // given
//         // when
//         // then
//     }
//
//     @Disabled
//     @Test
//     @DisplayName("[실패] -존재하지 않는 유저 ID로 리뷰를 생성하면 예외가 발생한다.")
//     void createReview_Fail_UserNotFound() {
//         // given
//         // when
//         // then
//     }
//
// }
