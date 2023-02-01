package com.jsframe.blind.repository;

import com.jsframe.blind.entity.Board;
import com.jsframe.blind.entity.BoardFiles;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BoardFileRepository
    extends CrudRepository<BoardFiles, Long> {

  List<BoardFiles> findByBfno(Board board);

  void deleteByBfno(Board board);

}
