package kr.muroom.muroombackendbach.user.domain.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MusicianQueryRepositoryImpl implements MusicianQueryRepository {

    private final JPAQueryFactory queryFactory;



}
