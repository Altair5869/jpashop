package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
/*
*   JPA에서 같은 트랜잭션 안에서 같은 엔티티(pk 값)가 똑같다면
*   영속성 컨텍스트에서 같은 값을 지닌 객체가 관리가 됨
* */
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;


    @Test
    //@Rollback(false)
    public void join() throws Exception {
        //given
        Member member = new Member();
        member.setName("kim");

        //when
        Long savedId = memberService.join(member);
        /*
        * 쿼리 실행문에서 insert문이 없는 이유:
        * jpa에서는 db 트랜잭션이 커밋될 때, 플러시가 되면서 db insert 쿼리가 나가게 됨
        * -> 트랜잭션 커밋이 매우 중요
        * 그러나 스프링에서 트랜잭션 어노테이션은 기본적으로 커밋 x, 롤백 O
        *
        * 기본적으로 롤백을 하는 이유:
        * 테스트는 반복적 -> db에 데이터가 남으면 안되기 때문
        * */

        //then
        assertEquals(member, memberRepository.findOne(savedId));
    }

    @Test()
    public void validateDuplicateMember() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");

        //when
        memberService.join(member1);
        assertThrows(IllegalStateException.class, () -> {memberService.join(member2);});


    }
  
}