CREATE TABLE member (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        email VARCHAR(255) NOT NULL,
                        clokey_id VARCHAR(255) UNIQUE NOT NULL ,
                        nickname VARCHAR(30) NOT NULL,
                        oauth_id VARCHAR(255) NOT NULL ,

                        oauth_provider VARCHAR(255) NOT NULL CHECK (
                            oauth_provider IN ('KAKAO', 'APPLE')
                            ),

                        member_status VARCHAR(255) NOT NULL DEFAULT 'ACTIVE' CHECK (
                            member_status IN ('ACTIVE', 'INACTIVE','BANNED')
                            ),

                        member_role VARCHAR(255) NOT NULL DEFAULT 'USER' CHECK (
                            member_role IN ('ADMIN', 'USER')
                            ),

                        visibility VARCHAR(255) NOT NULL DEFAULT 'PUBLIC' CHECK (
                            visibility IN ('PUBLIC', 'PRIVATE')
                            ),

                        profile_image_url VARCHAR(255),
                        profile_back_image_url VARCHAR(255),
                        bio VARCHAR(100),
                        device_token VARCHAR(255),
                        inactive_date DATE,

                        created_at DATETIME(6) NOT NULL,
                        updated_at DATETIME(6) NOT NULL
);


CREATE TABLE term (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,

                      title VARCHAR(255) NOT NULL,
                      body TEXT NOT NULL,
                      optional BOOLEAN NOT NULL,

                      created_at DATETIME(6) NOT NULL,
                      updated_at DATETIME(6) NOT NULL
);


CREATE TABLE category (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(50) NOT NULL,
                          parent_id BIGINT,
                          created_at DATETIME(6) NOT NULL,
                          updated_at DATETIME(6) NOT NULL,

                          CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES category(id)
);

CREATE TABLE folder (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(30) NOT NULL,
                        member_id BIGINT NOT NULL,
                        created_at DATETIME(6) NOT NULL,
                        updated_at DATETIME(6) NOT NULL,
                        CONSTRAINT fk_folder_member FOREIGN KEY (member_id) REFERENCES member(id)
);


CREATE TABLE cloth (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       cloth_image_url VARCHAR(255) NOT NULL ,
                       cloth_url VARCHAR(1000),
                       name VARCHAR(255),
                       brand VARCHAR(255),
                       season VARCHAR(255) NOT NULL CHECK (
                           season IN ('SPRING', 'SUMMER', 'FALL', 'WINTER')
                           ),
                       category_id BIGINT NOT NULL,
                       member_id BIGINT NOT NULL,
                       created_at DATETIME(6) NOT NULL,
                       updated_at DATETIME(6) NOT NULL,

                       CONSTRAINT fk_cloth_category FOREIGN KEY (category_id) REFERENCES category(id),
                       CONSTRAINT fk_cloth_member FOREIGN KEY (member_id) REFERENCES member(id)
);

CREATE TABLE history_type (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       created_at DATETIME(6) NOT NULL,
                       updated_at DATETIME(6) NOT NULL
);

CREATE TABLE history (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         history_date DATE NOT NULL,
                         content VARCHAR(200),
                         banned BOOLEAN NOT NULL,
                         member_id BIGINT NOT NULL,
                         history_type_id BIGINT NOT NULL,
                         created_at DATETIME(6) NOT NULL,
                         updated_at DATETIME(6) NOT NULL,

                         CONSTRAINT fk_history_member FOREIGN KEY (member_id) REFERENCES member(id),
                         CONSTRAINT fk_history_history_type FOREIGN KEY (history_type_id) REFERENCES history_type(id)
);

CREATE TABLE hashtag (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(30) NOT NULL UNIQUE,
                         created_at DATETIME(6) NOT NULL,
                         updated_at DATETIME(6) NOT NULL
);


CREATE TABLE comment (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         content VARCHAR(100) NOT NULL,
                         member_id BIGINT NOT NULL,
                         history_id BIGINT NOT NULL,
                         banned BOOLEAN NOT NULL,
                         parent_id BIGINT,
                         created_at DATETIME(6) NOT NULL,
                         updated_at DATETIME(6) NOT NULL,
                         CONSTRAINT fk_comment_member FOREIGN KEY (member_id) REFERENCES member(id),
                         CONSTRAINT fk_comment_history FOREIGN KEY (history_id) REFERENCES history(id),
                        CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES comment(id)
);

CREATE TABLE cloth_folder (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              cloth_id BIGINT NOT NULL,
                              folder_id BIGINT NOT NULL,
                              created_at DATETIME(6) NOT NULL,
                              updated_at DATETIME(6) NOT NULL,
                              CONSTRAINT fk_cloth_folder_cloth FOREIGN KEY (cloth_id) REFERENCES cloth(id),
                              CONSTRAINT fk_cloth_folder_folder FOREIGN KEY (folder_id) REFERENCES folder(id)
);



CREATE TABLE history_cloth (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               history_id BIGINT NOT NULL,
                               cloth_id BIGINT NOT NULL,
                               created_at DATETIME(6) NOT NULL,
                               updated_at DATETIME(6) NOT NULL,

                               CONSTRAINT fk_history_cloth_history FOREIGN KEY (history_id) REFERENCES history(id),
                               CONSTRAINT fk_history_cloth_cloth FOREIGN KEY (cloth_id) REFERENCES cloth(id),
                               CONSTRAINT uk_history_cloth_history_id_cloth_id UNIQUE (history_id, cloth_id)
);


CREATE TABLE history_image (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               image_url VARCHAR(255) NOT NULL,
                               history_id BIGINT NOT NULL,
                               created_at DATETIME(6) NOT NULL,
                               updated_at DATETIME(6) NOT NULL,

                               CONSTRAINT fk_history_image_history FOREIGN KEY (history_id) REFERENCES history(id)
);

CREATE TABLE history_cloth_tag (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               history_image_id BIGINT NOT NULL,
                               history_cloth_id BIGINT NOT NULL,
                               location_x DOUBLE NOT NULL,
                               location_y DOUBLE NOT NULL,
                               created_at DATETIME(6) NOT NULL,
                               updated_at DATETIME(6) NOT NULL,

                               CONSTRAINT fk_history_cloth_tag_history_image FOREIGN KEY (history_image_id) REFERENCES history_image(id),
                               CONSTRAINT fk_history_cloth_tag_history_cloth FOREIGN KEY (history_cloth_id) REFERENCES history_cloth(id)
);



CREATE TABLE member_like (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             member_id BIGINT NOT NULL,
                             history_id BIGINT NOT NULL,
                             created_at DATETIME(6) NOT NULL,
                             updated_at DATETIME(6) NOT NULL,

                             CONSTRAINT fk_member_like_member FOREIGN KEY (member_id) REFERENCES member(id),
                             CONSTRAINT fk_member_like_history FOREIGN KEY (history_id) REFERENCES history(id),
                             CONSTRAINT uk_member_history UNIQUE (member_id, history_id)
);


CREATE TABLE hashtag_history (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 hashtag_id BIGINT NOT NULL,
                                 history_id BIGINT NOT NULL,
                                 created_at DATETIME(6) NOT NULL,
                                 updated_at DATETIME(6) NOT NULL,

                                 CONSTRAINT fk_hashtag_history_hashtag FOREIGN KEY (hashtag_id) REFERENCES hashtag(id),
                                 CONSTRAINT fk_hashtag_history_history FOREIGN KEY (history_id) REFERENCES history(id),
                                 CONSTRAINT uk_hashtag_history_history_id_hashtag_id UNIQUE (history_id, hashtag_id)
);


CREATE TABLE block (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       blocker_id BIGINT NOT NULL,
                       blocked_id BIGINT NOT NULL,
                       created_at DATETIME(6) NOT NULL,
                       updated_at DATETIME(6) NOT NULL,

                       CONSTRAINT fk_block_blocker FOREIGN KEY (blocker_id) REFERENCES member(id),
                       CONSTRAINT fk_block_blocked FOREIGN KEY (blocked_id) REFERENCES member(id),

                       CONSTRAINT uk_block_blocker_id_blocked_id UNIQUE (blocker_id, blocked_id)
);


CREATE TABLE follow (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        follow_to_id BIGINT NOT NULL,
                        follow_from_id BIGINT NOT NULL,
                        created_at DATETIME(6) NOT NULL,
                        updated_at DATETIME(6) NOT NULL,

                        CONSTRAINT fk_follow_follow_to FOREIGN KEY (follow_to_id) REFERENCES member(id),
                        CONSTRAINT fk_follow_follow_from FOREIGN KEY (follow_from_id) REFERENCES member(id),

                        CONSTRAINT uk_follow_follow_to_id_followed_from_id UNIQUE (follow_to_id, follow_from_id)
);


CREATE TABLE pending_follow (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                follow_to_id BIGINT NOT NULL,
                                follow_from_id BIGINT NOT NULL,
                                created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                                CONSTRAINT fk_pending_follow_to FOREIGN KEY (follow_to_id) REFERENCES member(id),
                                CONSTRAINT fk_pending_follow_from FOREIGN KEY (follow_from_id) REFERENCES member(id),

                                CONSTRAINT uk_pending_follow_to_id_from_id UNIQUE (follow_to_id, follow_from_id)
);


CREATE TABLE clokey_notification (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     content VARCHAR(50) NOT NULL,
                                     notification_image_url VARCHAR(255) NOT NULL,
                                     redirect_info VARCHAR(255) NOT NULL,

                                     redirect_type VARCHAR(255) NOT NULL CHECK (
                                         redirect_type IN ('HISTORY_REDIRECT', 'MEMBER_REDIRECT')
                                         ),

                                     read_status VARCHAR(255) NOT NULL DEFAULT 'NOT_READ' CHECK (
                                         read_status IN ('READ', 'NOT_READ')
                                         ),
                                     member_id BIGINT NOT NULL,
                                     created_at DATETIME(6) NOT NULL,
                                     updated_at DATETIME(6) NOT NULL,

                                     CONSTRAINT fk_clokey_notification_member FOREIGN KEY (member_id) REFERENCES member(id)
);

CREATE TABLE report (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,

                        target_id BIGINT NOT NULL,
                        member_id BIGINT NOT NULL,

                        report_reason VARCHAR(255) NOT NULL CHECK (
                            report_reason IN (
                                              'ANNOYING_COMMENT', 'ANNOYING_HISTORY', 'DISCRIMINATORY_AND_HATEFUL',
                                              'ETC_COMMENT', 'ETC_HISTORY', 'HARMFUL_TO_MINORS', 'PRIVACY_EXPOSURE',
                                              'PRIVATE_INFO', 'SEXUAL', 'SPAM_OR_PROMOTION', 'SWEARING_AND_CURSING', 'VIOLENT'
                                )
                            ),

                        target_type VARCHAR(255) NOT NULL CHECK (
                            target_type IN ('COMMENT', 'HISTORY')
                            ),

                        report_status VARCHAR(255) NOT NULL DEFAULT 'UNCHECKED' CHECK (
                            report_status IN ('APPROVED', 'DISAPPROVED', 'UNCHECKED')
                            ),

                        content VARCHAR(200),

                        created_at DATETIME(6) NOT NULL,
                        updated_at DATETIME(6) NOT NULL,

                        CONSTRAINT fk_report_member FOREIGN KEY (member_id) REFERENCES member(id)
);

CREATE TABLE member_term (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,

                             member_id BIGINT NOT NULL,
                             term_id BIGINT NOT NULL,
                             agreed BOOLEAN NOT NULL,

                             created_at DATETIME(6) NOT NULL,
                             updated_at DATETIME(6) NOT NULL,

                             CONSTRAINT fk_member_term_member FOREIGN KEY (member_id) REFERENCES member(id),
                             CONSTRAINT fk_member_term_term FOREIGN KEY (term_id) REFERENCES term(id)
);

CREATE TABLE look_book (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,

                             name VARCHAR(50) NOT NULL,
                             member_id BIGINT NOT NULL,

                             created_at DATETIME(6) NOT NULL,
                             updated_at DATETIME(6) NOT NULL,

                             CONSTRAINT fk_look_book_member FOREIGN KEY (member_id) REFERENCES member(id)
);

CREATE TABLE coordinate (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,

                           name VARCHAR(50),
                           memo VARCHAR(100),
                           liked BOOLEAN NOT NULL,

                           coordinate_type VARCHAR(255) NOT NULL CHECK (
                               coordinate_type IN ('DEFAULT', 'DAILY')
                               ),

                           image_url VARCHAR(255),
                           member_id BIGINT NOT NULL,
                           look_book_id BIGINT,

                           created_at DATETIME(6) NOT NULL,
                           updated_at DATETIME(6) NOT NULL,


                           CONSTRAINT fk_coordinate_member FOREIGN KEY (member_id) REFERENCES member(id),
                           CONSTRAINT fk_coordinate_look_book FOREIGN KEY (look_book_id) REFERENCES look_book(id)
);

CREATE TABLE coordinate_cloth (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                  location_x DOUBLE NOT NULL,
                                  location_y DOUBLE NOT NULL,

                                  ratio DOUBLE NOT NULL CHECK (ratio > 0),
                                  degree DOUBLE NOT NULL,
                                  `order` INT NOT NULL,

                                  coordinate_id BIGINT NOT NULL,
                                  cloth_id BIGINT NOT NULL,

                                  created_at DATETIME(6) NOT NULL,
                                  updated_at DATETIME(6) NOT NULL,

                                  CONSTRAINT fk_coordinate_cloth_coordinate FOREIGN KEY (coordinate_id) REFERENCES coordinate(id),
                                  CONSTRAINT fk_coordinate_cloth_cloth FOREIGN KEY (cloth_id) REFERENCES cloth(id)
);


