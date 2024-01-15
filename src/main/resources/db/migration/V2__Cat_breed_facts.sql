ALTER TABLE cat_fact
    ADD COLUMN active_state VARCHAR(8) DEFAULT 'ACTIVE';

ALTER TABLE cat_fact_translation
    ADD COLUMN update_mode VARCHAR(6);

ALTER TABLE breed_translation
    ADD COLUMN update_mode VARCHAR(6);
