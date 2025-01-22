package site.beilsang.beilsang_server_v2.domain.member.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findBySocialId(String socialId);

}
