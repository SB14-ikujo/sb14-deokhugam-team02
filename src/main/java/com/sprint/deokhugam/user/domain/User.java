package com.sprint.deokhugam.user.domain;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.sprint.deokhugam.common.domain.BaseEntity;
import com.sprint.deokhugam.user.domain.exception.UserErrorCode;
import com.sprint.deokhugam.user.domain.exception.UserException;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    private static final int NICKNAME_MIN_LENGTH = 2;
    private static final int NICKNAME_MAX_LENGTH = 20;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = NICKNAME_MAX_LENGTH)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @ColumnDefault("false")
    @Column(nullable = false)
    private boolean isDeleted;

    private User(String email, String nickname, String password) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
    }

    public static User create(String email, String nickname, String password) {
        validateNickname(nickname);
        return new User(email, nickname, password);
    }

    private static void validateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new UserException(UserErrorCode.USER_NICKNAME_INVALID);
        }
        if (nickname.length() < NICKNAME_MIN_LENGTH || nickname.length() > NICKNAME_MAX_LENGTH) {
            throw new UserException(UserErrorCode.USER_NICKNAME_INVALID);
        }
    }

    public void changeNickname(String nickname) {
    }

}
