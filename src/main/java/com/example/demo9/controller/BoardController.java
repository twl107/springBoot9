package com.example.demo9.controller;

import com.example.demo9.common.PageVO;
import com.example.demo9.common.Pagination;
import com.example.demo9.dto.BoardDto;
import com.example.demo9.entity.Board;
import com.example.demo9.entity.BoardReply;
import com.example.demo9.entity.Member;
import com.example.demo9.repository.BoardReplyRepository;
import com.example.demo9.repository.BoardRepository;
import com.example.demo9.repository.MemberRepository;
import com.example.demo9.service.BoardService;
import com.example.demo9.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final BoardRepository boardRepository;
    private final BoardReplyRepository boardReplyRepository;
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final Pagination pagination;

    @GetMapping("/boardList")
    public String guestListGet(Model model, PageVO pageVO) {
        pageVO.setSection("board");
        pageVO = pagination.pagination(pageVO);
        //model.addAttribute("boardList", pageVO.getBoardList());
        model.addAttribute("pageVO", pageVO);

        return "board/boardList";
    }

    @GetMapping("/boardInput")
    public String boardInputGet() {
        return "board/boardInput";
    }

    @PostMapping("/boardInput")
    public String boardInputPost(BoardDto dto, HttpServletRequest request,
                                 Authentication authentication,
                                 Member member) {
        dto.setHostIp(request.getRemoteAddr());
        String email = authentication.getName();
        member = memberService.getMemberEmailCheck(email).get();

        Board board = Board.dtoToEntity(dto, member);
        Board board_ = boardService.setBoardInput(board);

        if(board_ != null) return "redirect:/message/boardInputOk";
        else return "redirect:/message/boardInputNo";
    }

    @GetMapping("/boardContent")
    public String boardContentGet(Model model, Long id, PageVO pageVO, HttpSession session) {
        // 글 조회수 증가처리(중복방지)
        List<String> contentNum = (List<String>) session.getAttribute("sDuplicate");
        if(contentNum == null) contentNum = new ArrayList<>();
        String imsiNum = "board" + id;
        if(!contentNum.contains(imsiNum)) {
            boardService.setBoardReadNumPlus(id);
            contentNum.add(imsiNum);
        }
        session.setAttribute("sDuplicate", contentNum);

        // 이전글/다음글 가져오기
        Board preVO = boardService.getPreNextSearch(id, "pre");
        Board nextVO = boardService.getPreNextSearch(id, "next");
        model.addAttribute("preVO", preVO);
        model.addAttribute("nextVO", nextVO);

        // 원본글 가져오기
        Board board = boardService.getBoardContent(id);
        model.addAttribute("board", board);
        model.addAttribute("pageVO", pageVO);

        // 현재 게시글의 관련 댓글 가져오기
        List<BoardReply> replyVos = boardService.getBoardReply(id);
        System.out.println("replyVos : " + replyVos);
        model.addAttribute("replyVos", replyVos);

        model.addAttribute("newLine", System.lineSeparator());

        return "board/boardContent";
    }

    // 댓글 입력
    @ResponseBody
    @PostMapping("/boardReplyInput")
    public int boardReplyInputPost(HttpServletRequest request,
                                   Authentication authentication,
                                   @RequestParam Long boardId,
                                   @RequestParam String name,
                                   @RequestParam String content) {

        Board board = boardRepository.findById(boardId).orElseThrow(()-> new IllegalStateException("원본글 없음"));
        Member member = memberRepository.findByEmail(authentication.getName()).orElseThrow(()-> new IllegalStateException("회원 없음"));
        BoardReply boardReply = BoardReply.builder()
                .board(board)           // board테이블에서 boardId검색
                .member(member)         // member테이블에서 email검색
                .name(name)
                .content(content)
                .hostIp(request.getRemoteAddr())
                .build();

        boardReplyRepository.save(boardReply);
        return 1;
    }




    @GetMapping("/boardUpdate")
    public String boardUpdateGet(Long id, PageVO pageVO, Model model) {
        Board board = boardService.getBoardContent(id);
        model.addAttribute("board", board);
        model.addAttribute("pageVO", pageVO);

        return "board/boardUpdate";
    }

    @PostMapping("/boardUpdate")
    public String boardUpdatePost(Long id, PageVO pageVO, BoardDto dto) {
        try {
            boardService.setBoardUpdate(id, dto);
        }
        catch (IllegalArgumentException e){
            return "redirect:/message/boardUpdateNo?id="+id;
        }
        return "redirect:/message/boardUpdateOk?id="+id;
    }

    @GetMapping("/boardDelete")
    public String boardDeleteGet(Long id) {
        try {
            boardService.getDeleteBoard(id);
        }
        catch (IllegalArgumentException e) {
            return "redirect:/message/boardDeleteNo?id=" + id;
        }
        return "redirect:/message/boardDeleteOk";
    }






}
