package com.example.demo9.controller;

import com.example.demo9.dto.BoardDto;
import com.example.demo9.entity.Board;
import com.example.demo9.entity.Member;
import com.example.demo9.repository.BoardRepository;
import com.example.demo9.service.BoardService;
import com.example.demo9.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final MemberService memberService;

    @GetMapping("/boardList")
    public String boardListGet(Model model,
                               @RequestParam(name = "pag", defaultValue = "0", required = false) int pag,
                               @RequestParam(name = "pageSize", defaultValue = "5", required = false) int pageSize) {

        Pageable pageable = PageRequest.of(pag, pageSize, Sort.by("id").descending());

        Page<Board> pageData = boardService.getBoardList(pageable);

        List<Board> boardList = pageData.getContent();
        int totPage = pageData.getTotalPages();

        int blockSize = 5;
        int curBlock = pag / blockSize;

        int lastBlock = (totPage == 0) ? 0 : (int)(Math.ceil((double) totPage / blockSize)) -1;

        model.addAttribute("boardList", boardList);
        model.addAttribute("pag", pag);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totPage", totPage);
        model.addAttribute("blockSize", blockSize);
        model.addAttribute("curBlock", curBlock);
        model.addAttribute("lastBlock", lastBlock);

        return "board/boardList";
    }

    @GetMapping("/boardInput")
    public String boardInputGet() {
        return "board/boardInput";
    }

    @PostMapping("/boardInput")
    public String boardInputPost(BoardDto dto, HttpServletRequest request, Authentication authentication,
                                 Member member) {
        dto.setHostIp(request.getRemoteAddr());
        String email = authentication.getName();
        member = memberService.getMemberEmailCheck(email).get();

        Board board = Board.dtoToEntity(dto, member);
        Board board_ = boardService.setBoardInput(board);

        if(board_ != null) return "redirect:/message/boardInputOk";
        else return "redirect:/message/boardInputNo";
    }




}
