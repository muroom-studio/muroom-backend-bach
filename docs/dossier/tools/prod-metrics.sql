-- 운영 DB 실사용 지표 (전부 읽기 전용) — 결과를 05-questions Q3/Q19/Q25 답변에 사용
-- 실행: 접속 중인 prod psql/GUI 세션에 통째로 붙여넣기

-- [1] 가입자 규모
SELECT 'musicians_total' AS metric, count(*) FROM musicians
UNION ALL SELECT 'musicians_active', count(*) FROM musicians WHERE deleted_at IS NULL AND status = 'ACTIVE'
UNION ALL SELECT 'musicians_withdrawn', count(*) FROM musicians WHERE deleted_at IS NOT NULL
UNION ALL SELECT 'owners_total', count(*) FROM owners
UNION ALL SELECT 'owners_active', count(*) FROM owners WHERE deleted_at IS NULL AND status = 'ACTIVE';

-- [2] 소셜 가입 분포
SELECT provider, count(*) FROM social_accounts GROUP BY provider;

-- [3] 콘텐츠 규모 (soft-delete 포함/제외)
SELECT 'studios_live' AS metric, count(*) FROM studios WHERE deleted_at IS NULL
UNION ALL SELECT 'studios_deleted', count(*) FROM studios WHERE deleted_at IS NOT NULL
UNION ALL SELECT 'rooms_live', count(*) FROM rooms WHERE deleted_at IS NULL
UNION ALL SELECT 'boasts_live', count(*) FROM studio_boasts WHERE deleted_at IS NULL
UNION ALL SELECT 'boast_comments', count(*) FROM studio_boast_comments WHERE deleted_at IS NULL
UNION ALL SELECT 'inquiries', count(*) FROM inquiries
-- studio_drafts 제외: prod에 미존재 확인됨(2026-08-02) = feature/studio 미배포의 실측 증거
UNION ALL SELECT 'subway_stations', count(*) FROM subway_stations;

-- [4] 트래픽 프록시: 월별 검색 로그 (회원 vs 익명)
SELECT date_trunc('month', searched_at) AS month,
       count(*) AS searches,
       count(musician_id) AS by_member,
       count(anonymous_user_id) AS by_guest,
       count(DISTINCT musician_id) AS uniq_members,
       count(DISTINCT anonymous_user_id) AS uniq_guests
FROM search_logs GROUP BY 1 ORDER BY 1;

-- [5] 트래픽 프록시: 월별 스튜디오 상세 조회
SELECT date_trunc('month', viewed_at) AS month,
       count(*) AS views,
       count(DISTINCT anonymous_user_id) AS uniq_guests
FROM studio_view_logs GROUP BY 1 ORDER BY 1;

-- [6] 최근 30일 활성 (검색+조회 합산 근사)
SELECT 'active_members_30d' AS metric,
       count(DISTINCT musician_id) FROM search_logs
       WHERE searched_at > now() - interval '30 days' AND musician_id IS NOT NULL
UNION ALL
SELECT 'active_guests_30d',
       count(DISTINCT anonymous_user_id) FROM search_logs
       WHERE searched_at > now() - interval '30 days' AND anonymous_user_id IS NOT NULL;

-- [7] 가입 추이 (월별)
SELECT date_trunc('month', created_at) AS month, count(*) AS signups
FROM musicians GROUP BY 1 ORDER BY 1;

-- [8] beta 시절 유입 (Q25 — 테이블 있으면)
SELECT 'beta_registrations' AS metric, count(*) FROM beta_registrations
UNION ALL SELECT 'beta_inquiries', count(*) FROM beta_inquiries;

-- [9] GiST 인덱스 실존 확인 (Q2 — 수동 생성 여부 판정)
SELECT tablename, indexname, indexdef FROM pg_indexes
WHERE tablename IN ('studios', 'subway_stations') ORDER BY tablename;

-- [10] 마지막 활동 시각 (서비스 생존 판정)
SELECT 'last_search' AS metric, max(searched_at)::text FROM search_logs
UNION ALL SELECT 'last_view', max(viewed_at)::text FROM studio_view_logs
UNION ALL SELECT 'last_signup', max(created_at)::text FROM musicians;
