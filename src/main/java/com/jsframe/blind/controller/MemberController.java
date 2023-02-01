package com.jsframe.blind.controller;


import com.jsframe.blind.entity.Member;
import com.jsframe.blind.service.MemberService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
@Log
public class MemberController {
  @Autowired
  private MemberService mServ;

  @GetMapping("login")
  public String login() {
    log.info("login()");
    return "login";
  }

  //로그인
  @PostMapping("loginProc")
  public String loginProc(Member member, HttpSession session, RedirectAttributes rttr) {
    log.info("loginProc()");
    String view = mServ.loginProc(member, session, rttr);
    return view;
  }


  //회원가입 페이지 이동
  @GetMapping("register")
  public String register() {
    log.info("register()");
    return "register";
  }


  //회원가입
  @PostMapping("regProc")
  public String regProc(Member member, RedirectAttributes rttr, HttpSession session) {
    log.info("regProc()");
    String view = mServ.regMember(member, rttr, session);
    return view;
  }


  //중복확인
  @PostMapping("checkDuplicatedId")
  public @ResponseBody String checkDuplicatedId(String id) {
    log.info("checkDuplicatedId()");
    String msg = mServ.checkDuplicatedId(id);
    return msg;
  }


  //로그아웃
  @GetMapping("logoutProc")
  public String logoutProc(HttpSession session, RedirectAttributes rttr) {
    log.info("logoutProc()");

    session.invalidate();
    rttr.addFlashAttribute("msg", "로그아웃 됐습니다.");
    return "redirect:/";

  }
}
