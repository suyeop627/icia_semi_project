package com.jsframe.blind.controller;

import com.jsframe.blind.entity.Comment;
import com.jsframe.blind.service.CommentService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@Log
public class CommentController {


  @Autowired
  private CommentService cServ;

//댓글 작성
  @PostMapping("cWriteProc")
  public String cWriteProc(Comment comment, HttpSession session, RedirectAttributes rttr) {
    log.info("cWriteProc()");
    String view = cServ.commentSave(comment, session, rttr);

    return view;
  }

//댓글 삭제
  @GetMapping("cDeleteProc")
  public String cDeleteProc(Long cno,  RedirectAttributes rttr) {
    log.info("cDeleteproc()");
    String view = cServ.commentDelete(cno, rttr);

    return view;
  }

//댓글 조회(5개씩)
  @GetMapping("getCommentList")
  public @ResponseBody List<Comment> commentList(Integer cPageNum, Long bno) {
    log.info("getCommentList()");

    List<Comment> commentList = cServ.getCommentList(cPageNum, bno);

    return commentList;
  }

  //댓글 좋아요
  @GetMapping("likeComment")
  public String likeCommnet(long cno, RedirectAttributes rttr) {
    log.info("likeComment()");
    return cServ.likeComment(cno, rttr);
  }

}