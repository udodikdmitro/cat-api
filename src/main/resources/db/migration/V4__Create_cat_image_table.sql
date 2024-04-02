CREATE TABLE cat_image(
    id BIGINT PRIMARY KEY,
    bread_id BIGINT NOT NULL
        REFERENCES breed(id)
        ON DELETE CASCADE,
    file_location varchar(1000)
);