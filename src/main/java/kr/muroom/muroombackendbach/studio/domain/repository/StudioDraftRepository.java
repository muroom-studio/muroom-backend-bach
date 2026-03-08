package kr.muroom.muroombackendbach.studio.domain.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioDraft;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioDraftRepository extends JpaRepository<StudioDraft, Long> {

  List<StudioDraft> findAllByOwnerIdOrderByUpdatedAtDesc(Long ownerId);

  Optional<StudioDraft> findByIdAndOwnerId(Long id, Long ownerId);

  void deleteAllByExpiresAtBefore(OffsetDateTime now);
}
