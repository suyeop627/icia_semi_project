package com.jsframe.blind.repository;

import com.jsframe.blind.entity.Board;
import com.jsframe.blind.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommentRepository extends CrudRepository<Comment, Long>{


  List<Comment> findByBcno(Board board, Pageable commentPageable);

  List<Comment> findByBcno(Board board);
}
