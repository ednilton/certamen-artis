'use client';

import { useState } from 'react';
import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import Container from '@mui/material/Container';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import FactoryIcon from '@mui/icons-material/Factory';
import { useEvents } from '@/hooks/useEvents';
import { ApiError, eventsApi, type CreateEventRequest } from '@/lib/api';
import EventForm from './EventForm';
import EventsTable from './EventsTable';
import FeedbackSnackbar, { type Feedback } from './FeedbackSnackbar';

/**
 * Tela principal: orquestra o cadastro (EventForm), a consulta (EventsTable)
 * e as mensagens de retorno (FeedbackSnackbar).
 */
export default function EventsDashboard() {
  const { events, loading, error, refresh } = useEvents();
  const [submitting, setSubmitting] = useState(false);
  const [feedback, setFeedback] = useState<Feedback | null>(null);
  const [serverErrors, setServerErrors] = useState<Record<string, string>>({});

  const handleCreate = async (payload: CreateEventRequest): Promise<boolean> => {
    setSubmitting(true);
    setServerErrors({});

    try {
      const created = await eventsApi.create(payload);
      setFeedback({ severity: 'success', message: `Evento ${created.eventId} registrado com sucesso.` });
      await refresh();
      return true;
    } catch (e) {
      if (e instanceof ApiError) {
        setServerErrors(e.problem.invalid_fields ?? {});
        // 409 (duplicado) é um aviso de negócio; o resto é erro
        setFeedback({ severity: e.status === 409 ? 'warning' : 'error', message: e.message });
      } else {
        setFeedback({ severity: 'error', message: 'Erro inesperado ao registrar o evento.' });
      }
      return false;
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <AppBar position="static" elevation={0}>
        <Toolbar>
          <FactoryIcon sx={{ mr: 1.5 }} />
          <Typography variant="h6" component="h1">
            Certamen Artis · Eventos da Fábrica
          </Typography>
        </Toolbar>
      </AppBar>

      <Container maxWidth="xl" sx={{ py: 4 }}>
        <Box
          sx={{
            display: 'grid',
            gap: 3,
            gridTemplateColumns: { xs: '1fr', md: '380px 1fr' },
            alignItems: 'start',
          }}
        >
          <EventForm onSubmit={handleCreate} submitting={submitting} serverErrors={serverErrors} />
          <EventsTable events={events} loading={loading} error={error} onRefresh={() => void refresh()} />
        </Box>
      </Container>

      <FeedbackSnackbar feedback={feedback} onClose={() => setFeedback(null)} />
    </>
  );
}
