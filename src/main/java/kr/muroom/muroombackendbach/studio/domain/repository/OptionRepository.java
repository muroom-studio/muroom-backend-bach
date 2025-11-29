package kr.muroom.muroombackendbach.studio.domain.repository;

import java.util.List;
import kr.muroom.muroombackendbach.studio.domain.entity.Option;
import kr.muroom.muroombackendbach.studio.domain.enums.OptionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionRepository extends JpaRepository<Option, Long> {

  List<Option> findAllByCategory(OptionCategory optionCategory);
}
