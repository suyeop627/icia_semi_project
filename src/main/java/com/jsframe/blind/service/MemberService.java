package com.jsframe.blind.service;


import com.jsframe.blind.entity.Member;
import com.jsframe.blind.repository.MemberRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Service
@Log
public class MemberService {
  @Autowired
  private MemberRepository mRepo;

  private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();


  @Transactional
  public String regMember(Member member, RedirectAttributes rttr, HttpSession session) {
    log.info("regMember()");
    String msg = null;
    String view = null;

    String cPwd = encoder.encode(member.getMpwd());
    member.setMpwd(cPwd);


    try {
      mRepo.save(member);
      msg = "회원가입이 완료됐습니다.";
      session.setAttribute("member", member);

      view = "redirect:/";

    } catch (Exception e) {
      e.printStackTrace();
      msg = "가입실패";
      view = "redirect:register";
    }
    rttr.addFlashAttribute("msg", msg);
    return view;
  }


  public String loginProc(Member member, HttpSession session, RedirectAttributes rttr) {
    log.info("loginMember()");
    String msg = null;
    String view = null;

    String dbPwd = null;
    Member dbmember = null;
    try {
      dbmember = mRepo.findById(member.getMid()).get();
      dbPwd = dbmember.getMpwd();
    } catch (Exception e) {
      //e.printStackTrace();
    }


    if (dbPwd != null) {
      String userPwd = member.getMpwd();

      if (encoder.matches(userPwd, dbPwd)) {
        session.setAttribute("member", dbmember);
        log.info(session.getAttribute("member").toString());
        msg = "로그인 됐습니다.";
        view = "redirect:/";

      } else {
        msg = "비밀번호가 일치하지 않습니다..";
        view = "redirect:login";
      }
    } else {
      msg = "등록되지 않은 아이디를 입력하셨습니다. \n아이디 확인 후 다시 시도해주세요.";
      view = "redirect:login";
    }
    rttr.addFlashAttribute("msg", msg);
    return view;
  }


  public String checkDuplicatedId(String id) {
    log.info("checkDuplicatedId()");
    String msg = null;

    try {
      mRepo.findById(id).get();
      msg = "사용할 수 없는 아이디";
    } catch (Exception e) {
      msg = "사용 가능한 아이디";
    }
    return msg;
  }
}



