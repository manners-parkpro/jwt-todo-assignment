package com.assignment.todo.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface TodoRepository extends JpaRepository<TodoJpaEntity, Long> {

	Optional<TodoJpaEntity> findByIdAndUserId(Long id, Long userId);

	List<TodoJpaEntity> findAllByUserIdOrderByIdAsc(Long userId);
}
