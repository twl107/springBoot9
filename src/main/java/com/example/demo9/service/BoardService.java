package com.example.demo9.service;

import com.example.demo9.dto.BoardDto;
import com.example.demo9.entity.Board;
import com.example.demo9.entity.BoardReply;
import com.example.demo9.repository.BoardReplyRepository;
import com.example.demo9.repository.BoardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardReplyRepository boardReplyRepository;

//  public List<Board> getBoardList() {
//    return boardRepository.findAll();
//  }

    public Board setBoardInput(Board board) {
        return boardRepository.save(board);
    }

    public Board getBoardContent(Long id) {
        //return boardRepository.findById(id).get();
        return boardRepository.findById(id).orElse(null);
    }

    public void setBoardReadNumPlus(Long id) {
        boardRepository.setBoardReadNumPlus(id);
    }

    public Board getPreNextSearch(Long id, String preNext) {
        if(preNext.equals("pre")) {
            return boardRepository.findPrevious(id);
        }
        else {
            return boardRepository.findNext(id);
        }
    }

    public List<BoardReply> getBoardReply(Long boardId) {
        return boardReplyRepository.findByBoardIdOrderById(boardId);
    }


    @Transactional
    public void setBoardUpdate(Long id, BoardDto dto) {
        Board board = boardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 게시물을 찾을 수 없습니다."));

        board.setTitle(dto.getTitle());
        board.setContent(dto.getContent());


    }

    public void getDeleteBoard(Long id) {
        Board board = boardRepository.findById(id).orElseThrow(()->new IllegalArgumentException("삭제할 게시물을 찾을 수 없습니다."));
        boardRepository.delete(board);
    }
}
