-- Repeatable migration: 상황 데이터 초기화

INSERT INTO situation (id, name, created_at, updated_at)
VALUES
    (1, '데일리', NOW(6), NOW(6)),
    (2, '여행', NOW(6), NOW(6)),
    (3, '데이트', NOW(6), NOW(6)),
    (4, '파티', NOW(6), NOW(6)),
    (5, '출근룩', NOW(6), NOW(6)),
    (6, '운동', NOW(6), NOW(6)),
    (7, '축제', NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    updated_at = NOW(6);

