CREATE EXTENSION IF NOT EXISTS postgis;
SELECT PostGIS_full_version();

INSERT INTO instruments (instruments_id, name)
VALUES (1, '보컬'),
       (2, '기타'),
       (3, '베이스'),
       (4, '키보드'),
       (5, '드럼'),
       (6, '관악'),
       (7, '목관'),
       (8, '현악'),
       (9, '성악'),
       (10, '국악'),
       (11, 'MIDI'),
       (12, '그 외')
;

INSERT INTO terms (term_id, type, target_role, version, is_mandatory, effective_at, created_at)
VALUES (1, 'TERMS_OF_USE', 'MUSICIAN', 'v1.0', TRUE, NOW(), NOW()),
       (2, 'PRIVACY_COLLECTION', 'MUSICIAN', 'v1.0', TRUE, NOW(), NOW()),
       (3, 'PRIVACY_PROCESSING', 'MUSICIAN', 'v1.0', TRUE, NOW(), NOW()),
       (4, 'MARKETING_RECEIVE', 'MUSICIAN', 'v1.0', FALSE, NOW(), NOW()),
       (5, 'TERMS_OF_USE', 'OWNER', 'v1.0', TRUE, NOW(), NOW()),
       (6, 'PRIVACY_COLLECTION', 'OWNER', 'v1.0', TRUE, NOW(), NOW()),
       (7, 'PRIVACY_PROCESSING', 'OWNER', 'v1.0', TRUE, NOW(), NOW()),
       (8, 'MARKETING_RECEIVE', 'OWNER', 'v1.0', FALSE, NOW(), NOW())
;

INSERT INTO term_contents (term_id, content)
VALUES (1, '뮤지션 이용약관 v1.0 내용입니다.'),
       (2, '뮤지션 개인정보 수집 동의서 v1.0 내용입니다.'),
       (3, '뮤지션 개인정보 처리 동의서 v1.0 내용입니다.'),
       (4, '뮤지션 마케팅 수신 동의서 v1.0 내용입니다.'),
       (5, '오너 이용약관 v1.0 내용입니다.'),
       (6, '오너 개인정보 수집 동의서 v1.0 내용입니다.'),
       (7, '오너 개인정보 처리 동의서 v1.0 내용입니다.'),
       (8, '오너 마케팅 수신 동의서 v1.0 내용입니다.');


-- Studio Data

-- TRUNCATE TABLE studios RESTART IDENTITY;
--
-- INSERT INTO studios (studio_id, name, address, location, view_count, introduction,
--                      thumbnail_image_key, blueprint_image_key, created_at, updated_at, deleted_at)
-- VALUES (nextval('studio_id_seq'), '뮤룸 강남점', '서울특별시 강남구 테헤란로 123',
--         ST_SetSRID(ST_MakePoint(127.0276, 37.4980), 4326), 0, '강남역 근처에 위치한 최신 시설의
--       스튜디오입니다.', 'thumbnail_gangnam.jpg', 'blueprint_gangnam.jpg', NOW(), NOW(), NULL);
--
-- INSERT INTO studios (studio_id, name, address, location, view_count, introduction,
--                      thumbnail_image_key, blueprint_image_key, created_at, updated_at, deleted_at)
-- VALUES (nextval('studio_id_seq'), '뮤룸 홍대점', '서울특별시 마포구 홍익로 20',
--         ST_SetSRID(ST_MakePoint(126.9245, 37.5575), 4326), 0, '홍대입구역 도보 5분 거리, 감성적인
--       분위기의 스튜디오.', 'thumbnail_hongdae.jpg', 'blueprint_hongdae.jpg', NOW(), NOW(), NULL);
--
-- INSERT INTO studios (studio_id, name, address, location, view_count, introduction,
--                      thumbnail_image_key, blueprint_image_key, created_at, updated_at, deleted_at)
-- VALUES (nextval('studio_id_seq'), '뮤룸 명동점', '서울특별시 중구 명동길 10',
--         ST_SetSRID(ST_MakePoint(126.9860, 37.5610), 4326), 0, '명동 한복판에 위치한 접근성 좋은
--       스튜디오.', 'thumbnail_myeongdong.jpg', 'blueprint_myeongdong.jpg', NOW(), NOW(), NULL);
--
-- INSERT INTO studios (studio_id, name, address, location, view_count, introduction,
--                      thumbnail_image_key, blueprint_image_key, created_at, updated_at, deleted_at)
-- VALUES (nextval('studio_id_seq'), '뮤룸 잠실점', '서울특별시 송파구 올림픽로 300',
--         ST_SetSRID(ST_MakePoint(127.1000, 37.5130), 4326), 0, '잠실 롯데월드타워 인근, 넓고
--       쾌적한 스튜디오.', 'thumbnail_jamsil.jpg', 'blueprint_jamsil.jpg', NOW(), NOW(), NULL);
--
-- INSERT INTO studios (studio_id, name, address, location, view_count, introduction,
--                      thumbnail_image_key, blueprint_image_key, created_at, updated_at, deleted_at)
-- VALUES (nextval('studio_id_seq'), '뮤룸 이태원점', '서울특별시 용산구 이태원로 200',
--         ST_SetSRID(ST_MakePoint(126.9940, 37.5340), 4326), 0, '이국적인 분위기의 이태원
--       스튜디오.', 'thumbnail_itaewon.jpg', 'blueprint_itaewon.jpg', NOW(), NOW(), NULL);
--
-- INSERT INTO studios (studio_id, name, address, location, view_count, introduction,
--                      thumbnail_image_key, blueprint_image_key, created_at, updated_at, deleted_at)
-- VALUES (nextval('studio_id_seq'), '뮤룸 신촌점', '서울특별시 서대문구 연세로 50',
--         ST_SetSRID(ST_MakePoint(126.9360, 37.5590), 4326), 0, '신촌역 근처, 대학가에 위치한 활기찬
--       스튜디오.', 'thumbnail_sinchon.jpg', 'blueprint_sinchon.jpg', NOW(), NOW(), NULL);
--
-- INSERT INTO studio_prices (studio_id, min_price, max_price)
-- VALUES ((SELECT studio_id FROM studios WHERE name = '뮤룸 잠실점'), 500000, 900000);
-- INSERT INTO studio_prices (studio_id, min_price, max_price)
-- VALUES ((SELECT studio_id FROM studios WHERE name = '뮤룸 이태원점'), 320000, 850000);
-- INSERT INTO studio_prices (studio_id, min_price, max_price)
-- VALUES ((SELECT studio_id FROM studios WHERE name = '뮤룸 신촌점'), 280000, 750000);
--
-- INSERT INTO subway_stations_nearby_studios (subway_station_nearby_studio_id, studio_id,
--                                             subway_station_id, sequence, created_at)
-- VALUES (nextval('subway_nearby_id_seq'), (SELECT studio_id FROM studios WHERE name = '뮤룸 강남점'),
--         (SELECT subway_station_id FROM subway_stations WHERE name = '강남'), 1, NOW());
--
-- INSERT INTO subway_stations_nearby_studios (subway_station_nearby_studio_id, studio_id,
--                                             subway_station_id, sequence, created_at)
-- VALUES (nextval('subway_nearby_id_seq'), (SELECT studio_id FROM studios WHERE name = '뮤룸 강남점'),
--         (SELECT subway_station_id FROM subway_stations WHERE name = '신논현'), 2, NOW());
--
-- INSERT INTO subway_stations_nearby_studios (subway_station_nearby_studio_id, studio_id,
--                                             subway_station_id, sequence, created_at)
-- VALUES (nextval('subway_nearby_id_seq'), (SELECT studio_id FROM studios WHERE name = '뮤룸 홍대점'),
--         (SELECT subway_station_id FROM subway_stations WHERE name = '홍대입구'), 1, NOW());
--
-- INSERT INTO subway_stations_nearby_studios (subway_station_nearby_studio_id, studio_id,
--                                             subway_station_id, sequence, created_at)
-- VALUES (nextval('subway_nearby_id_seq'), (SELECT studio_id FROM studios WHERE name = '뮤룸 명동점'),
--         (SELECT subway_station_id FROM subway_stations WHERE name = '명동'), 1, NOW());
--
-- INSERT INTO subway_stations_nearby_studios (subway_station_nearby_studio_id, studio_id,
--                                             subway_station_id, sequence, created_at)
-- VALUES (nextval('subway_nearby_id_seq'), (SELECT studio_id FROM studios WHERE name = '뮤룸 잠실점'),
--         (SELECT subway_station_id FROM subway_stations WHERE name = '잠실(송파구청)'), 1, NOW());
--
-- INSERT INTO subway_stations_nearby_studios (subway_station_nearby_studio_id, studio_id,
--                                             subway_station_id, sequence, created_at)
-- VALUES (nextval('subway_nearby_id_seq'), (SELECT studio_id FROM studios WHERE name = '뮤룸 이태원점'),
--         (SELECT subway_station_id FROM subway_stations WHERE name = '이태원'), 1, NOW());
--
-- INSERT INTO subway_stations_nearby_studios (subway_station_nearby_studio_id, studio_id,
--                                             subway_station_id, sequence, created_at)
-- VALUES (nextval('subway_nearby_id_seq'), (SELECT studio_id FROM studios WHERE name = '뮤룸 신촌점'),
--         (SELECT subway_station_id FROM subway_stations WHERE name = '신촌'), 1, NOW());
