-- Seed data for H2 database
-- This file is automatically executed by Spring Boot on startup

-- Authors
INSERT INTO authors (id, name) VALUES
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'George Orwell'),
    ('b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Jane Austen'),
    ('c3d4e5f6-a7b8-9012-cdef-123456789012', 'F. Scott Fitzgerald'),
    ('d4e5f6a7-b8c9-0123-def0-234567890123', 'Harper Lee'),
    ('e5f6a7b8-c9d0-1234-ef01-345678901234', 'J.R.R. Tolkien');

-- Books (author_id references authors.id)
INSERT INTO books (id, isbn, title, author_id) VALUES
    -- George Orwell's books
    ('11111111-1111-1111-1111-111111111111', '9780451524935', '1984', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890'),
    ('22222222-2222-2222-2222-222222222222', '9780451526342', 'Animal Farm', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890'),

    -- Jane Austen's books
    ('33333333-3333-3333-3333-333333333333', '9780141439518', 'Pride and Prejudice', 'b2c3d4e5-f6a7-8901-bcde-f12345678901'),
    ('44444444-4444-4444-4444-444444444444', '9780141439587', 'Emma', 'b2c3d4e5-f6a7-8901-bcde-f12345678901'),
    ('55555555-5555-5555-5555-555555555555', '9780141439792', 'Sense and Sensibility', 'b2c3d4e5-f6a7-8901-bcde-f12345678901'),

    -- F. Scott Fitzgerald's books
    ('66666666-6666-6666-6666-666666666666', '9780743273565', 'The Great Gatsby', 'c3d4e5f6-a7b8-9012-cdef-123456789012'),

    -- Harper Lee's books
    ('77777777-7777-7777-7777-777777777777', '9780061120084', 'To Kill a Mockingbird', 'd4e5f6a7-b8c9-0123-def0-234567890123'),

    -- J.R.R. Tolkien's books
    ('88888888-8888-8888-8888-888888888888', '9780547928227', 'The Hobbit', 'e5f6a7b8-c9d0-1234-ef01-345678901234'),
    ('99999999-9999-9999-9999-999999999999', '9780544003415', 'The Lord of the Rings', 'e5f6a7b8-c9d0-1234-ef01-345678901234');
