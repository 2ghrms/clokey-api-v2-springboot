-- Repeatable migration: 카테고리 데이터 초기화
-- 부모 카테고리와 자식 카테고리를 계층 구조로 적재

-- 부모 카테고리 삽입
INSERT INTO category (id, name, parent_id, created_at, updated_at)
VALUES
    (1, '상의', NULL, NOW(6), NOW(6)),
    (2, '바지', NULL, NOW(6), NOW(6)),
    (3, '스커트', NULL, NOW(6), NOW(6)),
    (4, '아우터', NULL, NOW(6), NOW(6)),
    (5, '신발', NULL, NOW(6), NOW(6)),
    (6, '가방', NULL, NOW(6), NOW(6)),
    (7, '패션 소품', NULL, NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    updated_at = NOW(6);

-- 자식 카테고리 삽입
INSERT INTO category (id, name, parent_id, created_at, updated_at)
VALUES
    -- 상의 (parent_id: 1)
    (8, '티셔츠', 1, NOW(6), NOW(6)),
    (9, '니트/스웨터', 1, NOW(6), NOW(6)),
    (10, '맨투맨', 1, NOW(6), NOW(6)),
    (11, '후드티', 1, NOW(6), NOW(6)),
    (12, '셔츠/블라우스', 1, NOW(6), NOW(6)),
    (13, '반팔티', 1, NOW(6), NOW(6)),
    (14, '나시', 1, NOW(6), NOW(6)),
    (15, '기타', 1, NOW(6), NOW(6)),
    
    -- 바지 (parent_id: 2)
    (16, '청바지', 2, NOW(6), NOW(6)),
    (17, '반바지', 2, NOW(6), NOW(6)),
    (18, '트레이닝/조거팬츠', 2, NOW(6), NOW(6)),
    (19, '면바지', 2, NOW(6), NOW(6)),
    (20, '슈트팬츠/슬랙스', 2, NOW(6), NOW(6)),
    (21, '레깅스', 2, NOW(6), NOW(6)),
    (22, '기타', 2, NOW(6), NOW(6)),
    
    -- 스커트 (parent_id: 3)
    (23, '미니스커트', 3, NOW(6), NOW(6)),
    (24, '미디스커트', 3, NOW(6), NOW(6)),
    (25, '롱스커트', 3, NOW(6), NOW(6)),
    (26, '원피스', 3, NOW(6), NOW(6)),
    (27, '투피스', 3, NOW(6), NOW(6)),
    (28, '기타', 3, NOW(6), NOW(6)),
    
    -- 아우터 (parent_id: 4)
    (29, '숏패딩/헤비 아우터', 4, NOW(6), NOW(6)),
    (30, '무스탕/퍼', 4, NOW(6), NOW(6)),
    (31, '후드집업', 4, NOW(6), NOW(6)),
    (32, '점퍼/바람막이', 4, NOW(6), NOW(6)),
    (33, '가죽자켓', 4, NOW(6), NOW(6)),
    (34, '청자켓', 4, NOW(6), NOW(6)),
    (35, '슈트/블레이저', 4, NOW(6), NOW(6)),
    (36, '가디건', 4, NOW(6), NOW(6)),
    (37, '아노락', 4, NOW(6), NOW(6)),
    (38, '후리스/양털', 4, NOW(6), NOW(6)),
    (39, '코트', 4, NOW(6), NOW(6)),
    (40, '롱패딩', 4, NOW(6), NOW(6)),
    (41, '패딩조끼', 4, NOW(6), NOW(6)),
    (42, '기타', 4, NOW(6), NOW(6)),
    
    -- 신발 (parent_id: 5)
    (43, '스니커즈', 5, NOW(6), NOW(6)),
    (44, '부츠/워커', 5, NOW(6), NOW(6)),
    (45, '구두', 5, NOW(6), NOW(6)),
    (46, '샌들/슬리퍼', 5, NOW(6), NOW(6)),
    (47, '기타', 5, NOW(6), NOW(6)),
    
    -- 가방 (parent_id: 6)
    (48, '메신저/크로스백', 6, NOW(6), NOW(6)),
    (49, '숄더백', 6, NOW(6), NOW(6)),
    (50, '백팩', 6, NOW(6), NOW(6)),
    (51, '토트백', 6, NOW(6), NOW(6)),
    (52, '에코백', 6, NOW(6), NOW(6)),
    (53, '기타', 6, NOW(6), NOW(6)),
    
    -- 패션 소품 (parent_id: 7)
    (54, '모자', 7, NOW(6), NOW(6)),
    (55, '머플러', 7, NOW(6), NOW(6)),
    (56, '양말/레그웨어', 7, NOW(6), NOW(6)),
    (57, '시계', 7, NOW(6), NOW(6)),
    (58, '주얼리', 7, NOW(6), NOW(6)),
    (59, '벨트', 7, NOW(6), NOW(6)),
    (60, '선글라스/안경', 7, NOW(6), NOW(6)),
    (61, '기타', 7, NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    parent_id = VALUES(parent_id),
    updated_at = NOW(6);

