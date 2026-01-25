package kr.muroom.muroombackendbach.room.domain.repository;

import java.util.List;
import kr.muroom.muroombackendbach.room.domain.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {

  List<Room> findAllByStudioIdIn(List<Long> studioIds);

  List<Room> findAllByStudioId(Long studioId);

  void deleteAllByStudioId(Long studioId);
}
