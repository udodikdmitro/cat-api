CREATE TABLE cat_facts(
    ID SERIAL PRIMARY KEY,
    Fact VARCHAR
);

CREATE TABLE breed(
    ID SERIAL PRIMARY KEY,
    breed_ID VARCHAR,
    breed_name VARCHAR,
    description VARCHAR
);