package com.jsframe.blind.service;

import com.jsframe.blind.entity.Board;
import com.jsframe.blind.entity.Comment;
import com.jsframe.blind.repository.BoardRepository;
import com.jsframe.blind.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Service
@Log
@RequiredArgsConstructor

public class CommentService {
  @Autowired
  private CommentRepository cRepo;
  @Autowired
  private BoardRepository bRepo;

  //댓글 작성
  @Transactional
  public String commentSave(Comment comment, HttpSession session, RedirectAttributes rttr) {
    log.info("commentSave()");
    String msg = null;
    String view = null;

    try {
      cRepo.save(comment);
      log.info("c_no : " + comment.getCno());
      //해당 게시글에 댓글 수 1 추가
      comment.getBcno().setBcomment(comment.getBcno().getBcomment() + 1);
      view = "redirect:/detail?bno=" + comment.getBcno().getBno();
      msg = "댓글이 작성되었습니다.";
    } catch (Exception e) {
      e.printStackTrace();
      view = "redirect:/detail?bno=" + comment.getBcno().getBno();
    }
    rttr.addFlashAttribute("msg", msg);
    return view;
  }

//댓글 삭제
  @Transactional
  public String commentDelete(Long cno, RedirectAttributes rttr) {
    log.info("commentDelete()");
    String msg = null;
    String view = null;

    Comment cmt = cRepo.findById(cno).get();

    try {
      Optional<Board> board = bRepo.findById(cmt.getBcno().getBno());
      if (board.isPresent()) {
        //해당 게시글의 댓글 수 -1
        board.get().setBcomment(board.get().getBcomment() - 1);
        bRepo.save(board.get());
      }
      cRepo.deleteById(cno);
      msg = "삭제되었습니다.";
      view = "redirect:/detail?bno=" + cmt.getBcno().getBno();
    } catch (Exception e) {
      e.printStackTrace();
      msg = "다시 시도해주세요.";
      view = "redirect:/detail?bno=" + cmt.getBcno().getBno();
    }
    rttr.addFlashAttribute("msg", msg);
    return view;
  }
//댓글 목록 조회
  public List<Comment> getCommentList(Integer cPageNum, Long bno) {
    log.info("getCommentList()");
    //불러올 댓글 수 = 5
    int listCnt = 5;
    //각 페이지는 작성일 기준, 5개씩 내림차순으로 구성
    Pageable commentPageable = PageRequest.of(cPageNum - 1, listCnt, Sort.Direction.DESC, "cdate");

    Board board = bRepo.findById(bno).get();

    //해당 게시글의 댓글 조회
    List<Comment> commentList = cRepo.findByBcno(board, commentPageable);

    return commentList;
  }


  public String likeComment(long cno, RedirectAttributes rttr) {


    log.info("likeCommnet()");
    String msg = null;
    String view = null;

    Comment comment = cRepo.findById(cno).get();

    try {
      //댓글 좋아오 +1
      comment.setClike(comment.getClike() + 1);
      cRepo.save(comment);

      view = "redirect:/detail?bno=" + comment.getBcno().getBno();
      msg = "댓글을 좋아요 했습니다.";
    } catch (Exception e) {
      e.printStackTrace();
      view = "redirect:/detail?bno=" + comment.getBcno().getBno();
    }
    rttr.addFlashAttribute("msg", msg);
    return view;
  }


}
