ALTER TABLE member CHANGE COLUMN clokey_id nickname VARCHAR(30) NOT NULL;
ALTER TABLE member ADD CONSTRAINT uk_member_nickname UNIQUE (nickname);
