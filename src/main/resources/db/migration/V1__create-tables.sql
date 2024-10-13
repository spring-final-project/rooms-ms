CREATE TABLE rooms(
    id UUID DEFAULT gen_random_uuid(),
    num INT,
    name VARCHAR(150),
    floor INT,
    max_capacity INT,
    description VARCHAR,
    simple_beds INT,
    medium_beds INT,
    double_beds INT,
    owner_id UUID NOT NULL,
    created_at TIMESTAMPTZ default now(),
    last_updated TIMESTAMPTZ,
    PRIMARY KEY(id)
);

CREATE TABLE images(
    id UUID DEFAULT gen_random_uuid(),
    url VARCHAR(255) NOT NULL,
    room_id UUID NOT NULL,
    CONSTRAINT fk_room
        FOREIGN KEY(room_id)
            REFERENCES rooms(id)
                ON DELETE CASCADE
);