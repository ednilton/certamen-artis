CREATE TABLE event (
                       id UUID PRIMARY KEY,
                       event_id VARCHAR(255) NOT NULL UNIQUE,
                       event_type VARCHAR(50) NOT NULL,
                       description TEXT,
                       event_time TIMESTAMP NOT NULL,
                       created_at TIMESTAMP NOT NULL
);

CREATE TABLE audit_log (
                           id UUID PRIMARY KEY,
                           event_id VARCHAR(255) NOT NULL,
                           action VARCHAR(100) NOT NULL,
                           created_at TIMESTAMP NOT NULL,
                           CONSTRAINT fk_audit_event FOREIGN KEY (event_id) REFERENCES event(event_id)
);