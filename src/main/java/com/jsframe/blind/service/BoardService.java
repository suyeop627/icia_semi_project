package com.jsframe.blind.service;

import com.jsframe.blind.entity.Board;
import com.jsframe.blind.entity.BoardFiles;
import com.jsframe.blind.entity.Comment;
import com.jsframe.blind.entity.Member;
import com.jsframe.blind.repository.BoardFileRepository;
import com.jsframe.blind.repository.BoardRepository;
import com.jsframe.blind.repository.CommentRepository;
import com.jsframe.blind.repository.MemberRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
@Log
public class BoardService {
  @Autowired
  private BoardRepository bRepo;
  @Autowired
  private BoardFileRepository bfRepo;

  @Autowired
  private CommentRepository cRepo;

  private ModelAndView mv;

  //게시글 상세페이지
  public ModelAndView getBoard(long bno, HttpSession session) {
    log.info("getBoard()");
    mv = new ModelAndView();
    //상세페이지
    mv.setViewName("detail");

    Board board = bRepo.findById(bno).get();

    mv.addObject("board", board);
    //로그인한 계정과 글 작성자를 비교하기 위해 ModelAndView에 member 저장 후 반환
    mv.addObject("member", session.getAttribute("member"));
    //해당 게시글에 첨부된 파일데이터
    List<BoardFiles> bfList = bfRepo.findByBfno(board);
    mv.addObject("bfList", bfList);


    return mv;
  }

  //카테고리별 게시글 조회
  public List<Board> getTopicList(String category, Integer pageNum, HttpSession session) {
    log.info("getTopicList()");

    //한 페이지 당 출력할 게시글
    int listCnt = 20;

    //각 페이지 당 20개씩, 작성일 기준 내림차순
    Pageable topicPageable = PageRequest.of(pageNum - 1, listCnt, Sort.Direction.DESC, "bdate");
    //각 페이지당 20개 -> 조건은 쿼리로 지정
    Pageable bestPageable = PageRequest.of(pageNum - 1, listCnt);

    //반환할 게시글 목록
    List<Board> topicList;


    //오늘 날짜 구하기
    Date today = new Date();
    //현재 날짜(밀리초)를 Timestamp 형식으로 변환
    Timestamp now = new Timestamp(today.getTime());
    //현재시간 기준 6시간 전 Timestamp
    Timestamp sixHoursAgo = new Timestamp(today.getTime() - (6 * 60 * 60 * 1000));

    //현재시간 기준 7일 전 Timestamp
    Timestamp weekAgo = new Timestamp(today.getTime() - (7 * 24 * 60 * 60 * 1000));



    switch (category) {
      case "전체":
        //모든 게시글을 대상으로, 작성일 기준 내림차순
        topicList = bRepo.findAll(topicPageable);
        break;
      case "베스트":
        //일주일 안에 작성된 게시글 중 조회수 기준 내림차순
        topicList = bRepo.findAllWeekBest(now, weekAgo, bestPageable);
        break;
      default:
        //각 카테고리별 게시글을 대상으로, 작성일 기준 내림차순
        topicList = bRepo.findByBcategory(category, topicPageable);

        //첫페이지일 경우,
        if (pageNum == 1) {
          System.out.println("pageNum = " + pageNum);
         Board nowBest = bRepo.findNowBest(category, now, sixHoursAgo);

          //6시간 이내 작성된 게시글이 없을경우, 해당 카테고리 내의 가장 최신 게시글 조회
          if (nowBest == null) {
            nowBest = bRepo.findFirstByBcategoryOrderByBdateDesc(category);
          }

          //일주일 이내 작성된 게시글 중 가장 조회수가 높은 게시글 조회
          Board  weekBest = bRepo.findWeekBest(category, now, weekAgo);

          //일주일 이내 작성된 게시글이 없을 경우, 해당 카테고리 내의 좋아요 수가 가장 많은 게시글 조회
          if (weekBest == null) {
            weekBest = bRepo.findFirstByBcategoryOrderByBlikeDesc(category);
          }
          //현재 인기글과 일주일 이내 인기글을 반환할 게시글 리스트에 추가
          topicList.add(0, nowBest);
          topicList.add(1, weekBest);
        }
    }

    return topicList;
  }

//게시글 작성
  @Transactional
  public String insertBoard(List<MultipartFile> files, Board board, HttpSession session, RedirectAttributes rttr) {
    log.info("insertBoard()");
    String msg = null;
    String view = null;


    try {
      Member loginUser = (Member) session.getAttribute("member");
      board.setMbid(loginUser);
      bRepo.save(board);
      //파일 업로드
      fileUpload(files, session, board);

      view = "redirect:/";
      msg = "저장 완료";

    } catch (Exception e) {
      e.printStackTrace();
      view = "redirect:write";
      msg = "저장 실패";
    }
    rttr.addFlashAttribute("msg", msg);
    return view;
  }

//파일 업로드 처리
  private void fileUpload(List<MultipartFile> files,
                          HttpSession session, Board board) throws Exception {
    log.info("fileUpload()");

    //절대경로 가져오기 (/.../webapp/)
    String realPath = session.getServletContext().getRealPath("/");
    log.info("realpath : " + realPath);

//webapp 하위의 upload 폴더로 경로 지정
    realPath += "upload/";
    File folder = new File(realPath);
    //webapp 하위에 images폴더가 없다면 생성
    if (folder.isDirectory() == false) {
      folder.mkdir();
    }


    for (MultipartFile mf : files) {
      //각 파일에 대해서 처리
      String orname = mf.getOriginalFilename();
      //더이상 파일이 없으면 반복문 종료
      if (orname.equals("")) {
        return;
      }

      BoardFiles bf = new BoardFiles();
      bf.setBfno(board);

      bf.setForiname(orname);
      //동일한 이름으로 저장되는 것을 방지하기위해, 저장될 파일의 이름을 밀리초로 하고, 확장자 추가해서 저장
      String sysname = System.currentTimeMillis() + orname.substring(orname.lastIndexOf("."));
      bf.setFsysname(sysname);

      File file = new File(realPath + sysname);
      //저장 경로에 파일 생성
      mf.transferTo(file);
      System.out.println("realPath = " + realPath);
      bfRepo.save(bf);
    }
  }


  //조회수 ++
  @Transactional
  public void addViewCount(Long bno, HttpSession session) {
    log.info("addViewCount()");
    Board board = bRepo.findById(bno).get();
    session.setAttribute("board", board);
    //조회수 1 추가
    board.setBview(board.getBview() + 1);

    bRepo.save(board);
  }


  //게시글 수정
  @Transactional
  public String boardUpdate(List<MultipartFile> files, Board board, HttpSession session, RedirectAttributes rttr) {
    log.info("boardUpdate()");
    String msg = null;
    String view = null;

    List<BoardFiles> bfList = bfRepo.findByBfno(board);
    try {
      //기존에 저장돼있던 파일 삭제
      deleteFiles(bfList,session);
      //db에 저장된 데이터 삭제
      bfRepo.deleteByBfno(board);
      //수정된 게시글 저장
      bRepo.save(board);
      //수정된 파일 새로 저장
      fileUpload(files, session, board);

      msg = "수정 성공";
      view = "redirect:detail?bno=" + board.getBno();
    } catch (Exception e) {
      e.printStackTrace();
      msg = "수정 실패";
      view = "redirect:updateFrm?bno" + board.getBno();
    }
    rttr.addFlashAttribute("msg", msg);
    return view;
  }


  //파일 다운로드 처리
  public ResponseEntity<Resource> fileDownload(BoardFiles bfile, HttpSession session) throws IOException {
    log.info("fileDownload()");
    //파일 저장 경로 구하기
    String realpath = session.getServletContext().getRealPath("/");
    //DispatcherServlet에는 session이 동작하는데 필요한 정보(context)를 함께 가지고 있으므로,
    // ServletContext에서 루트경로(~/webapp까지의 경로)를 구할 수 있다.
    realpath += "upload/" + bfile.getFsysname();

    //resource를 관리하는 inputstream으로 통로 연결.
    InputStreamResource fResource = new InputStreamResource(new FileInputStream(realpath));

    //original file name이 한글일 경우 제대로 동작하지않을 수도 있으므로 utf-8로 인코딩 처리(비영어권에선 필수적)
    String fileName = URLEncoder.encode(bfile.getForiname(), "UTF-8");

    return ResponseEntity.ok() //- 상태코드 200을 처리하는 메서드
        .contentType(MediaType.APPLICATION_OCTET_STREAM) //통신으로 전송되는 데이터(application에서 만들어진 데이터)인데, 8비트로 된 데이터(바이너리 데이터)의 파일임을 contentType으로 지정
        .cacheControl(CacheControl.noCache())//캐시로 처리하지 않도록 지정(임시저장하지 않도록)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
        //서버에서 넘어오는 데이터는 파일덩어리가 아니라 비트열로 넘어오는데,
        // 어디부터 어디까지가 파일인지를 패킷에 담아 보내준다.
        // 이걸 모아서 하나의 파일로 완성하는데, 비트열로 넘어온 데이터라서 따로 이름 담겨있진 않다.
        // 그래서 파일이름을 다시 지정해줘야한다.
        .body(fResource); //파일의 실제 데이터
  }


  @Transactional
  public String boardDelete(long bno, HttpSession session, RedirectAttributes rttr) {

    log.info("boardDelete()");
    String msg = null;
    String view = null;
    //매개변수로 받은 board의 기본키를 board 인스턴스에 저장
    Board board = new Board();
    board.setBno(bno);

    //board로 해당 게시글의 파일과 댓글 조회
    List<BoardFiles> bfList = bfRepo.findByBfno(board);
    List<Comment> cList = cRepo.findByBcno(board);

    try {
      //저장된 파일 삭제
      deleteFiles( bfList,session);
      //연관 comment 삭제
      cRepo.deleteAll(cList);

      //연관 boardfile 삭제
      bfRepo.deleteAll(bfList);
      //게시글 삭제
      bRepo.deleteById(bno);
      msg = "게시글을 삭제했습니다.";
      view = "redirect:/";
    } catch (Exception e) {
      e.printStackTrace();
      msg = "게시글 삭제 과정에서 문제가 발생했습니다.";
      view = "redirect:detail?bno=" + bno;
    }
    rttr.addFlashAttribute("msg", msg);

    return view;
  }

  //파일 삭제
  private void deleteFiles(List<BoardFiles> bfList, HttpSession session) {
    String realPath = session.getServletContext().getRealPath("/");
    realPath += "upload/";
    //각 파일 삭제
    for (BoardFiles bf : bfList) {
      //삭제할 파일 위치
      String delPath = realPath + bf.getFsysname();
      File file = new File(delPath);

      //해당 파일이 존재한다면 삭제
      if (file.exists()) {
        file.delete();
      }
    }
  }

//좋아요 ++
  public String likeBoard(long bno, RedirectAttributes rttr) {

    log.info("likeBoard()");
    String msg = null;
    String view = null;

    try {
      Board board = bRepo.findById(bno).get();
      //좋아요 +1
      board.setBlike(board.getBlike() + 1);
      bRepo.save(board);

      view = "redirect:/detail?bno=" + bno;
      msg = "게시글을 좋아요 했습니다.";
    } catch (Exception e) {
      e.printStackTrace();
      view = "redirect:/detail?bno=" + bno;
    }
    rttr.addFlashAttribute("msg", msg);
    return view;
  }
} // class end
