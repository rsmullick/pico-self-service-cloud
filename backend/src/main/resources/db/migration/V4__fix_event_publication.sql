-- V3__fix_event_publication.sql

ALTER TABLE event_publication
    ADD COLUMN IF NOT EXISTS listener_id VARCHAR(1000);

ALTER TABLE event_publication
    ADD COLUMN IF NOT EXISTS publication_date TIMESTAMP WITH TIME ZONE;