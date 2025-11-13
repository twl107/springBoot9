package com.example.demo9.controller;

import com.example.demo9.common.PageVO;
import com.example.demo9.common.Pagination;
import com.example.demo9.dto.MemberDto;
import com.example.demo9.entity.Member;
import com.example.demo9.repository.MemberRepository;
import com.example.demo9.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final Pagination pagination;

    @Autowired
    PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String homeGet() {
        return "home";
    }

    @GetMapping("/memberLogin")
    public String memberLoginGet() {
        return "member/memberLogin";
    }

    @GetMapping("/memberLoginOk")
    public String memberLoginOkGet(HttpServletRequest request,
                                   HttpServletResponse response,
                                   Authentication authentication,
                                   HttpSession session) {
        String email = authentication.getName();
        System.out.println("로그인한 email : " + email);

//        String name = memberService.getMemberEmailCheck(email).get().getName();
//        String strLevel = memberService.getMemberEmailCheck(email).get().getRole().toString();

        Optional<Member> opMember = memberService.getMemberEmailCheck(email);

        // 등급 정보 처리
        String strLevel = opMember.get().getRole().toString();
        if(strLevel.equals("ADMIN")) strLevel = "관리자";
        else if (strLevel.equals("OPERATOR")) strLevel = "운영자";
        else if (strLevel.equals("USER")) strLevel = "정회원";

        // Http세션에 필요한 정보 저장
        session.setAttribute("sName", opMember.get().getName());
        session.setAttribute("strLevel", strLevel);

        return "redirect:/message/memberLoginOk";
    }

    @GetMapping("/login/error")
    public String loginErrorGet () {
        return "redirect:/message/memberLoginNo";
    }

    @GetMapping("/memberLogout")
    public String memberLogoutGet(Authentication authentication,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  HttpSession session) {

        if(authentication != null) {
            String name = session.getAttribute("sName").toString();
            session.invalidate();
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        return "redirect:/message/memberLogout";
    }

    @GetMapping("/memberJoin")
    public String memberJoinGet(Model model) {
        model.addAttribute("memberDto", new MemberDto());
        return "member/memberJoin";
    }

    @PostMapping("/memberJoin")
    public String memberJoinPost(@Valid MemberDto dto,
                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "member/memberJoin";
        }
        else {
            try {
                Member member = Member.dtoToEntity(dto, passwordEncoder);
                Member memberRes = memberService.saveMember(member);
                return "redirect:/message/memberJoinOk";
            } catch (IllegalStateException e) {
                return "redirect:/message/memberJoinNo";
            }
        }
    }

    @GetMapping("/memberMain")
    public String memberMainGet() {
        return "member/memberMain";
    }

    @GetMapping("/memberList")
    public String memberListGet(Model model, PageVO pageVO) {
        pageVO.setSection("member");
        pageVO = pagination.pagination(pageVO);
        model.addAttribute("pageVO", pageVO);

        return "member/memberList";
    }

    @GetMapping("/memberPwdCheck/{flag}")
    public String memberPwdCheckGet(Model model, @PathVariable String flag) {
        model.addAttribute("flag", flag);
        return "member/memberPwdCheck";
    }

    @PostMapping("/memberPwdCheck")
    @ResponseBody
    public String memberPwdCheckPost(String email, String password) {
        //Member member = memberService.getMemberIdCheck(id);
        Optional<Member> member = memberService.getMemberEmailCheck(email);
        if(member.isPresent() && passwordEncoder.matches(password, member.get().getPassword())) {
            return "1";
        }
        return "0";
    }

    @PostMapping("/memberPwdChange")
    public String memberPwdChangePost(String email, String password) {

        Optional<Member> opMember = memberService.getMemberEmailCheck(email);

        if(opMember.isPresent()) {
            Member member = opMember.get();
            String newPwd = passwordEncoder.encode(password);
            member.setPassword(newPwd);

            memberRepository.save(member);



        }

    }



}
