INSERT INTO projects (name, description)
SELECT 'C14 Ticket System', 'Sistema de gerenciamento de tickets.'
WHERE NOT EXISTS (
    SELECT 1 FROM projects WHERE name = 'C14 Ticket System'
);

INSERT INTO projects (name, description)
SELECT 'OpenLift', 'Aplicativo de gerenciamento de academias.'
WHERE NOT EXISTS (
    SELECT 1 FROM projects WHERE name = 'OpenLift'
);

INSERT INTO projects (name, description)
SELECT 'Portal Interno', 'Portal de serviços e solicitações internas.'
WHERE NOT EXISTS (
    SELECT 1 FROM projects WHERE name = 'Portal Interno'
);
