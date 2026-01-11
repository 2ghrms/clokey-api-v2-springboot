-- Repeatable migration: 스타일 데이터 초기화

INSERT INTO style (id, name, created_at, updated_at)
VALUES
    (1, '캐주얼', NOW(6), NOW(6)),
    (2, '스트릿', NOW(6), NOW(6)),
    (3, '미니멀', NOW(6), NOW(6)),
    (4, '클래식', NOW(6), NOW(6)),
    (5, '시크', NOW(6), NOW(6)),
    (6, '빈티지', NOW(6), NOW(6)),
    (7, '걸리시', NOW(6), NOW(6)),
    (8, '스포티', NOW(6), NOW(6)),
    (9, '러블리', NOW(6), NOW(6)),
    (10, '오피스룩', NOW(6), NOW(6)),
    (11, '하이틴', NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    updated_at = NOW(6);

