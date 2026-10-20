package com.sprint.deokhugam.user.domain;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.sprint.deokhugam.common.domain.BaseEntity;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 20)
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
        return new User(email, nickname, password);
    }

}
