package kr.muroom.muroombackendbach.user.domain.repository;

import kr.muroom.muroombackendbach.user.domain.entity.MyStudio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyStudioRepository extends JpaRepository<MyStudio, Long> {

}
