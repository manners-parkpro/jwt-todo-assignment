package com.assignment.user.adapter.out.persistence;

import com.assignment.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class UserJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// SQLite에서는 컬럼의 unique만 CREATE TABLE에 들어간다(@Table의 유니크 제약은 생략된다)
	@Column(nullable = false, unique = true, length = 50)
	private String username;

	@Column(nullable = false)
	private String password;

	// 기존 todo.db에 이 컬럼을 더할 때(ddl-auto: update) SQLite는 기본값 없는 NOT NULL 컬럼 추가를 거부한다
	@ColumnDefault("false")
	@Column(nullable = false)
	private boolean withdrawn;

	private UserJpaEntity(Long id, String username, String password, boolean withdrawn) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.withdrawn = withdrawn;
	}

	static UserJpaEntity from(User user) {
		return new UserJpaEntity(
				user.getId(),
				user.getUsername(),
				user.getPassword(),
				user.isWithdrawn());
	}

	User toDomain() {
		return User.of(id, username, password, withdrawn);
	}
}
