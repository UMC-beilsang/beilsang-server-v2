package site.beilsang.beilsang_server_v2.domain.member.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.global.enums.Provider;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findBySocialIdAndEmail(String socialId, String email);

    Optional<Member> findBySocialIdAndProvider(String socialId, Provider provider);



}
