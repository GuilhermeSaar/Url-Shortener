INSERT INTO users (email, password, role, created_at)
VALUES
    (
        'idor.owner@example.com',
        '$2b$10$GDZOtG.35nEz7jIr4Fj1fOm0Z00p7TjOeW6Q10uEhK6vRBVpJFwj6',
        'ROLE_USER',
        CURRENT_TIMESTAMP
    ),
    (
        'idor.other@example.com',
        '$2b$10$GDZOtG.35nEz7jIr4Fj1fOm0Z00p7TjOeW6Q10uEhK6vRBVpJFwj6',
        'ROLE_USER',
        CURRENT_TIMESTAMP
    );
