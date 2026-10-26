'use client';

import { useState, type FormEvent } from 'react';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import InputAdornment from '@mui/material/InputAdornment';
import MenuItem from '@mui/material/MenuItem';
import Paper from '@mui/material/Paper';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import Tooltip from '@mui/material/Tooltip';
import Typography from '@mui/material/Typography';
import AutorenewIcon from '@mui/icons-material/Autorenew';
import SendIcon from '@mui/icons-material/Send';
import type { CreateEventRequest, EventType } from '@/lib/api';
import { EVENT_TYPE_LABELS, generateEventId, localInputToIso } from '@/utils/format';

interface EventFormProps {
  /** Envia o evento. Deve resolver `true` em caso de sucesso (o formulário é limpo). */
  onSubmit: (payload: CreateEventRequest) => Promise<boolean>;
  submitting: boolean;
  /** Erros por campo devolvidos pela API (invalid_fields). */
  serverErrors?: Record<string, string>;
}

const EVENT_TYPES: EventType[] = ['PRODUCTION', 'QUALITY'];
const MAX_EVENT_ID = 255;

export default function EventForm({ onSubmit, submitting, serverErrors = {} }: EventFormProps) {
  const [eventId, setEventId] = useState('');
  const [eventType, setEventType] = useState<EventType>('PRODUCTION');
  const [description, setDescription] = useState('');
  const [eventTime, setEventTime] = useState('');
  const [errors, setErrors] = useState<Record<string, string>>({});

  const fieldError = (field: string) => errors[field] ?? serverErrors[field];

  const validate = () => {
    const next: Record<string, string> = {};
    if (!eventId.trim()) next.eventId = 'Informe o ID do evento.';
    else if (eventId.trim().length > MAX_EVENT_ID) next.eventId = `Máximo de ${MAX_EVENT_ID} caracteres.`;
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!validate()) return;

    const ok = await onSubmit({
      eventId: eventId.trim(),
      eventType,
      description: description.trim(),
      // Sem data informada, o evento é registrado com o horário atual
      eventTime: eventTime ? localInputToIso(eventTime) : new Date().toISOString(),
    });

    if (ok) {
      setEventId('');
      setDescription('');
      setEventTime('');
    }
  };

  return (
    <Paper variant="outlined" sx={{ p: 3 }}>
      <Typography variant="h6" component="h2" gutterBottom>
        Novo evento
      </Typography>

      <Box component="form" noValidate onSubmit={handleSubmit}>
        <Stack spacing={2.5}>
          <TextField
            label="ID do evento"
            placeholder="EVT-1001"
            value={eventId}
            onChange={(e) => setEventId(e.target.value)}
            error={Boolean(fieldError('eventId'))}
            helperText={fieldError('eventId') ?? 'Identificador único enviado pelo equipamento.'}
            required
            fullWidth
            slotProps={{
              input: {
                endAdornment: (
                  <InputAdornment position="end">
                    <Tooltip title="Gerar ID">
                      <IconButton aria-label="Gerar ID" edge="end" onClick={() => setEventId(generateEventId())}>
                        <AutorenewIcon />
                      </IconButton>
                    </Tooltip>
                  </InputAdornment>
                ),
              },
            }}
          />

          <TextField
            select
            label="Tipo do evento"
            value={eventType}
            onChange={(e) => setEventType(e.target.value as EventType)}
            error={Boolean(fieldError('eventType'))}
            helperText={
              fieldError('eventType') ??
              (eventType === 'QUALITY' ? 'Também gera um registro de auditoria.' : 'Apenas registra o evento.')
            }
            required
            fullWidth
          >
            {EVENT_TYPES.map((type) => (
              <MenuItem key={type} value={type}>
                {EVENT_TYPE_LABELS[type]}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            label="Descrição"
            placeholder="Production line started"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            error={Boolean(fieldError('description'))}
            helperText={fieldError('description')}
            multiline
            minRows={3}
            fullWidth
          />

          <TextField
            label="Data/hora do evento"
            type="datetime-local"
            value={eventTime}
            onChange={(e) => setEventTime(e.target.value)}
            error={Boolean(fieldError('eventTime'))}
            helperText={fieldError('eventTime') ?? 'Opcional. Se vazio, usa o horário atual.'}
            fullWidth
            slotProps={{ inputLabel: { shrink: true } }}
          />

          <Button
            type="submit"
            variant="contained"
            size="large"
            endIcon={<SendIcon />}
            loading={submitting}
            loadingPosition="end"
          >
            Registrar evento
          </Button>
        </Stack>
      </Box>
    </Paper>
  );
}
