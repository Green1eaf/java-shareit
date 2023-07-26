DROP TABLE IF EXISTS feedbacks CASCADE;
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS items CASCADE;

CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name  VARCHAR(255)                            NOT NULL,
    email VARCHAR(128)                            NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS users_unique_email_idx ON users(email);

CREATE TABLE IF NOT EXISTS items
(
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name        VARCHAR(255)                            NOT NULL,
    description VARCHAR(512)                            NOT NULL,
    available   BOOLEAN DEFAULT TRUE                    NOT NULL,
    owner_id    BIGINT REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bookings
(
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    start_date  TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    end_date    TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    status      TEXT                                    NOT NULL,
    item_id     BIGINT REFERENCES items(id) ON DELETE CASCADE,
    owner_id    BIGINT REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comments
(
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    text        VARCHAR NOT NULL,
    item_id     BIGINT REFERENCES items(id) ON DELETE CASCADE,
    author_id   BIGINT REFERENCES users(id) ON DELETE CASCADE,
    start_date  TIMESTAMP WITHOUT TIME ZONE DEFAULT now()
);