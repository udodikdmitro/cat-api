CREATE TABLE cat_image(
    id BIGSERIAL PRIMARY KEY,
    bread_id BIGINT
        REFERENCES breed(id)
        ON DELETE CASCADE,
    file_location text
);