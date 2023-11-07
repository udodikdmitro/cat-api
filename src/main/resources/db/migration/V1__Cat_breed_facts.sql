CREATE TABLE cat_fact(
    id BIGSERIAL PRIMARY KEY,
    fact TEXT NOT NULL
);

CREATE TABLE cat_fact_translation(
    id BIGSERIAL PRIMARY KEY,
    cat_fact_id BIGINT NOT NULL
        CONSTRAINT cat_fact_translation_fk
            REFERENCES cat_fact(id)
        ON DELETE CASCADE,
    locale VARCHAR(5) NOT NULL,
    translation_text TEXT NOT NULL
);

CREATE TABLE breed(
    id BIGSERIAL PRIMARY KEY,
    outer_breed_id VARCHAR(4) NOT NULL,
    breed_name VARCHAR(30) NOT NULL,
    description TEXT NOT NULL
);

CREATE TABLE breed_translation(
    id BIGSERIAL PRIMARY KEY,
    breed_id BIGINT NOT NULL
        CONSTRAINT breed_translation_fk
            REFERENCES breed(id)
                ON DELETE CASCADE,
    locale VARCHAR(5) NOT NULL,
    breed_name VARCHAR(30) NOT NULL,
    description TEXT NOT NULL
);
