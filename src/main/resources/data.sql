INSERT INTO terms (term_id, type, target_role, version, is_mandatory, effective_at, created_at)
VALUES
    (1, 'TERMS_OF_USE', 'MUSICIAN', 'v1.0', TRUE, NOW(), NOW()),
    (2,'PRIVACY_COLLECTION', 'MUSICIAN', 'v1.0', TRUE, NOW(), NOW()),
    (3,'PRIVACY_PROCESSING', 'MUSICIAN', 'v1.0', TRUE, NOW(), NOW()),
    (4,'MARKETING_RECEIVE', 'MUSICIAN', 'v1.0', FALSE, NOW(), NOW()),
    (5,'TERMS_OF_USE', 'OWNER', 'v1.0', TRUE, NOW(), NOW()),
    (6,'PRIVACY_COLLECTION', 'OWNER', 'v1.0', TRUE, NOW(), NOW()),
    (7,'PRIVACY_PROCESSING', 'OWNER', 'v1.0', TRUE, NOW(), NOW()),
    (8,'MARKETING_RECEIVE', 'OWNER', 'v1.0', FALSE, NOW(), NOW())
;

INSERT INTO term_contents (term_id, content)
VALUES
    (1, '뮤지션 이용약관 v1.0 내용입니다.'),
    (2, '뮤지션 개인정보 수집 동의서 v1.0 내용입니다.'),
    (3, '뮤지션 개인정보 처리 동의서 v1.0 내용입니다.'),
    (4, '뮤지션 마케팅 수신 동의서 v1.0 내용입니다.'),
    (5, '오너 이용약관 v1.0 내용입니다.'),
    (6, '오너 개인정보 수집 동의서 v1.0 내용입니다.'),
    (7, '오너 개인정보 처리 동의서 v1.0 내용입니다.'),
    (8, '오너 마케팅 수신 동의서 v1.0 내용입니다.');