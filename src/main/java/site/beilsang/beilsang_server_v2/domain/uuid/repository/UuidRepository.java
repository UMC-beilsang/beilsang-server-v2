package site.beilsang.beilsang_server_v2.domain.uuid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.beilsang.beilsang_server_v2.domain.uuid.entity.Uuid;

@Repository
public interface UuidRepository extends JpaRepository<Uuid, Long> {
}
