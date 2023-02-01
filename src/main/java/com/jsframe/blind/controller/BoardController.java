package com.jsframe.blind.controller;

import com.jsframe.blind.entity.Board;
import com.jsframe.blind.entity.BoardFiles;
import lombok.extern.java.Log;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.jsframe.blind.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;


@Controller
@Log
public class BoardController {

  ModelAndView mv;
  @Autowired
  private BoardService bServ;


  //첫페이지
  @GetMapping("/")
  public ModelAndView home() {
    log.info("home()");
    mv = new ModelAndView();

    mv.setViewName("topic");

    return mv;

  }

  //해당 카테고리의 글목록 가져오기(20개)
  @GetMapping("getTopicList")
  public @ResponseBody List<Board> topic(String category, Integer pageNum, HttpSession session) {
    log.info("getTopicList()");

    return bServ.getTopicList(category, pageNum, session);
  }

  //게시글 상세페이지
  @GetMapping("detail")
  public ModelAndView detail(Long bno, HttpSession session) {
    log.info("detail()");
    bServ.addViewCount(bno, session);

    mv = bServ.getBoard(bno, session);

    return mv;
  }

  //게시글 작성 페이지
  @GetMapping("writePost")
  public String writeFrm() {
    log.info("write()");
    return "writePost";
  }

  //게시글 작성
  @PostMapping("writeProc")
  public String writeProc(@RequestPart List<MultipartFile> files,
                          Board board,
                          HttpSession session,
                          RedirectAttributes rttr) {

    log.info("writeProc()");
    return bServ.insertBoard(files, board, session, rttr);

  }

  //게시글 수정페이지
  @GetMapping("updateFrm")
  public ModelAndView updateFrm(long bno, HttpSession session) {


    log.info("updateFrm()");
    mv = bServ.getBoard(bno, session);
    mv.setViewName("updateFrm");
    return mv;
  }

  //게시글 수정
  @PostMapping("updateProc")
  public String updateProc(List<MultipartFile> files, Board board, HttpSession session, RedirectAttributes rttr) {
    log.info("updateProc()");

    return bServ.boardUpdate(files, board, session, rttr);
  }


  //파일 다운로드
  @GetMapping("download")
  public ResponseEntity<Resource> fileDownload(BoardFiles bfile, HttpSession session) throws IOException {
    return bServ.fileDownload(bfile, session);
  }

  //게시글 삭제
  @GetMapping("delete")
  public String deleteBoard(long bno, HttpSession session, RedirectAttributes rttr) {
    log.info("deleteBoard()");

    return bServ.boardDelete(bno, session, rttr);

  }

  //게시글 좋아요
  @GetMapping("likeBoard")
  public String likeBoard(long bno, RedirectAttributes rttr) {
    log.info("likeBoard()");
    return bServ.likeBoard(bno, rttr);
  }

}


