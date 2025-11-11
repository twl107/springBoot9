package com.example.demo9.service;

import com.example.demo9.entity.Board;
import com.example.demo9.entity.QBoard;
import com.example.demo9.repository.BoardRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    private final JPAQueryFactory queryFactory;

    private final QBoard board = QBoard.board;


    public Page<Board> getBoardList(Pageable pageable) {

        List<Board> content = queryFactory
                .selectFrom(board)
                .orderBy(board.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(board.count())
                .from(board)
                .fetchOne();

        if (total == null) {
            total = 0L;
        }

        return new PageImpl<>(content, pageable, total);
    }

    public Board setBoardInput(Board board) {
        return boardRepository.save(board);
    }
}
