package com.example.demo9.repository;

import com.example.demo9.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    Page<Member> findByNameContaining(String searchString, PageRequest pageable);

    Page<Member> findByEmailContaining(String searchString, PageRequest pageable);



}
