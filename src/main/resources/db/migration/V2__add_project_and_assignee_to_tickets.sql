ALTER TABLE tickets
    ADD COLUMN project VARCHAR(100)
        NOT NULL DEFAULT 'General';

ALTER TABLE tickets
    ADD COLUMN assignee VARCHAR(100)
        NOT NULL DEFAULT 'Unassigned';