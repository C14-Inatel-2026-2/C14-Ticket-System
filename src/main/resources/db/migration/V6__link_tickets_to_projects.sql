ALTER TABLE tickets
    ADD COLUMN project_id BIGINT;

INSERT INTO projects (name)
SELECT DISTINCT t.project
FROM tickets t
WHERE NOT EXISTS (
    SELECT 1
    FROM projects p
    WHERE p.name = t.project
);

UPDATE tickets t
SET project_id = (
    SELECT MIN(p.id)
    FROM projects p
    WHERE p.name = t.project
);

ALTER TABLE tickets
    ADD CONSTRAINT fk_tickets_project
    FOREIGN KEY (project_id) REFERENCES projects (id);

CREATE INDEX idx_tickets_project_id
    ON tickets (project_id);
