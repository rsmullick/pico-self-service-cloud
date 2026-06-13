-- Create event_publication table for Spring Modulith event handling
CREATE TABLE IF NOT EXISTS event_publication (
                                                 id UUID PRIMARY KEY,
                                                 event_type VARCHAR(255) NOT NULL,
    serialized_event TEXT NOT NULL,
    published_date TIMESTAMP WITH TIME ZONE,
    created_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
                               );

CREATE INDEX idx_event_publication_published
    ON event_publication(published_date);