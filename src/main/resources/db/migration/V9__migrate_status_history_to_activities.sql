INSERT INTO ticket_activities (
    ticket_id,
    author,
    activity_type,
    old_value,
    new_value,
    created_at
)
SELECT
    ticket_id,
    'Sistema',
    'STATUS_CHANGED',
    previous_status,
    new_status,
    changed_at
FROM ticket_status_history;


DROP TABLE ticket_status_history;
