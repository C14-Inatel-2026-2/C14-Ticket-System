INSERT INTO users (name, email, role)
VALUES
    ('Rodrigo', 'rodrigo@example.com', 'ADMIN'),
    ('Mariana', 'mariana@example.com', 'PROJECT_MANAGER'),
    ('Gabriel', 'gabriel@example.com', 'DEVELOPER');

INSERT INTO project_members (user_id, project_id)
SELECT u.id, p.id
FROM users u
CROSS JOIN projects p
WHERE 
    (u.email = 'rodrigo@example.com' AND p.name = 'C14 Ticket System') OR
    (u.email = 'mariana@example.com' AND p.name = 'C14 Ticket System') OR
    (u.email = 'gabriel@example.com' AND p.name = 'C14 Ticket System') OR
    (u.email = 'rodrigo@example.com' AND p.name = 'OpenLift') OR
    (u.email = 'mariana@example.com' AND p.name = 'Portal Interno');

UPDATE tickets
SET assignee_user_id = (
    SELECT u.id
    FROM users u
    WHERE LOWER(TRIM(u.name)) = LOWER(TRIM(tickets.assignee))
)
WHERE EXISTS (
    SELECT 1
    FROM users u
    WHERE LOWER(TRIM(u.name)) = LOWER(TRIM(tickets.assignee))
);
    
    
