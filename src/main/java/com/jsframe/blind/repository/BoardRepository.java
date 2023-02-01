package com.jsframe.blind.repository;

import com.jsframe.blind.entity.Board;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.List;

public interface BoardRepository extends CrudRepository<Board, Long> {

  //카테고리별 조회
  List<Board> findByBcategory(String category, Pageable topicPageable);

  //전체조회
  List<Board> findAll(Pageable topicPageable);

  //7일 이내 조회수가 많은 게시글 내림차순 조회
  @Query(value = "SELECT * FROM boardtbl WHERE b_date BETWEEN :weekAgo AND :now ORDER BY b_view DESC",
      nativeQuery = true)
  List<Board> findAllWeekBest(Timestamp now, Timestamp weekAgo, Pageable bestPageable);

  //6시간 안에 작성된 게시글 중 조회수가 가장 많은 게시글 1개 조회
  @Query(value = "SELECT * FROM boardtbl WHERE b_date BETWEEN :sixHoursAgo AND :now AND b_category=:category " +
      "AND b_view=(SELECT MAX(b_view) FROM boardtbl " +
      "WHERE  b_category=:category AND b_date BETWEEN :sixHoursAgo AND :now ) LIMIT 1",
      nativeQuery = true)
  Board findNowBest(String category, Timestamp now, Timestamp sixHoursAgo);

  //해당 카테고리의 가장 최신 게시글 1개 조회
  Board findFirstByBcategoryOrderByBdateDesc(String category);

  //일주일 안에 작성된 게시글 중, 조회수가 가장 많은 게시글 1개 조회
  @Query(value = "SELECT * FROM boardtbl WHERE  b_date BETWEEN :weekAgo AND :now AND b_category=:category " +
      "AND b_view=(SELECT MAX(b_view) FROM boardtbl " +
      "WHERE  b_category=:category AND b_date BETWEEN :weekAgo AND :now ) LIMIT 1"
      , nativeQuery = true)
  Board findWeekBest(String category, Timestamp now, Timestamp weekAgo);

  //해당 카테고리 내의 좋아요수가 가장 많은 게시글 1개 조회
  Board findFirstByBcategoryOrderByBlikeDesc(String category);
}

