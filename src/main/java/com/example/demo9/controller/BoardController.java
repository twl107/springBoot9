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
                               @RequestParam(name = "pag", defaultValue = "1", required = false) int pag,
                               @RequestParam(name = "pageSize", defaultValue = "5", required = false) int pageSize) {

        Pageable pageable = PageRequest.of(Math.max(pag - 1, 0), pageSize, Sort.by("id").descending());

        Page<Board> pageData = boardService.getBoardList(pageable);

        model.addAttribute("paging", pageData);

        int blockSize = 5;
        int totalPages = pageData.getTotalPages();
        int currentPage = pageData.getNumber();
        int startBlockPage;
        int endBlockPage;

        if (totalPages == 0) {
            startBlockPage = 1;
            endBlockPage = 1;
        } else {
            startBlockPage = (currentPage / blockSize) * blockSize + 1;

            endBlockPage = Math.min(startBlockPage + blockSize - 1, totalPages);

            if (endBlockPage > totalPages) {
                endBlockPage = totalPages;
            }
        }

        model.addAttribute("startBlockPage", startBlockPage);
        model.addAttribute("endBlockPage", endBlockPage);

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
