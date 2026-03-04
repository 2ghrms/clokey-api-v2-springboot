ALTER TABLE codive_notification
    ADD COLUMN notification_type VARCHAR(255) NOT NULL DEFAULT 'COMMENT' CHECK (
        notification_type IN ('FOLLOW', 'FOLLOW_REQUEST', 'COMMENT', 'REPLY', 'TEMPERATURE_DAILY')
    );

UPDATE codive_notification
SET notification_type =
    CASE
        WHEN redirect_type = 'MEMBER_REDIRECT' THEN 'FOLLOW'
        WHEN redirect_type = 'HISTORY_REDIRECT' THEN 'COMMENT'
        ELSE 'COMMENT'
    END;

ALTER TABLE codive_notification
    MODIFY redirect_type VARCHAR(255) NOT NULL CHECK (
        redirect_type IN ('NONE', 'HISTORY_REDIRECT', 'MEMBER_REDIRECT')
    );
