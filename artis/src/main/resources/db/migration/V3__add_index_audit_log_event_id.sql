-- O PostgreSQL não cria índice automaticamente para chave estrangeira.
-- Esse índice acelera a busca dos registros de auditoria de um evento (findAllByEventId).

CREATE INDEX idx_audit_log_event_id ON audit_log (event_id);
