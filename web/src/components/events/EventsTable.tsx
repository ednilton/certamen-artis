'use client';

import Alert from '@mui/material/Alert';
import IconButton from '@mui/material/IconButton';
import LinearProgress from '@mui/material/LinearProgress';
import Paper from '@mui/material/Paper';
import Stack from '@mui/material/Stack';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Tooltip from '@mui/material/Tooltip';
import Typography from '@mui/material/Typography';
import RefreshIcon from '@mui/icons-material/Refresh';
import type { EventResponse } from '@/lib/api';
import { formatDateTime } from '@/utils/format';
import EventTypeChip from './EventTypeChip';

interface EventsTableProps {
  events: EventResponse[];
  loading: boolean;
  error: string | null;
  onRefresh: () => void;
}

export default function EventsTable({ events, loading, error, onRefresh }: EventsTableProps) {
  return (
      <Paper variant="outlined" sx={{ overflow: 'hidden' }}>
        <Stack direction="row" sx={{ px: 3, py: 2, alignItems: 'center', justifyContent: 'space-between' }}>
          <Typography variant="h6" component="h2">
            Eventos registrados
            {!loading && !error && (
                <Typography component="span" sx={{ ml: 1, color: 'text.secondary' }}>
                  ({events.length})
                </Typography>
            )}
          </Typography>
          <Tooltip title="Atualizar">
          <span>
            <IconButton aria-label="Atualizar lista" onClick={onRefresh} disabled={loading}>
              <RefreshIcon />
            </IconButton>
          </span>
          </Tooltip>
        </Stack>

        {loading && <LinearProgress />}

        {error && (
            <Alert severity="error" sx={{ mx: 3, mb: 2 }}>
              {error}
            </Alert>
        )}

        <TableContainer sx={{ maxHeight: 560 }}>
          <Table stickyHeader size="small" aria-label="Tabela de eventos">
            <TableHead>
              <TableRow>
                <TableCell>ID do evento</TableCell>
                <TableCell>Tipo</TableCell>
                <TableCell>Descrição</TableCell>
                <TableCell>Data do evento</TableCell>
                <TableCell>Registrado em</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {events.map((event) => (
                  <TableRow key={event.id} hover>
                    <TableCell sx={{ fontFamily: 'monospace', whiteSpace: 'nowrap' }}>{event.eventId}</TableCell>
                    <TableCell>
                      <EventTypeChip type={event.eventType} />
                    </TableCell>
                    <TableCell sx={{ maxWidth: 320 }}>{event.description || '—'}</TableCell>
                    <TableCell sx={{ whiteSpace: 'nowrap' }}>{formatDateTime(event.eventTime)}</TableCell>
                    <TableCell sx={{ whiteSpace: 'nowrap' }}>{formatDateTime(event.createdAt)}</TableCell>
                  </TableRow>
              ))}

              {!loading && !error && events.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={5} align="center" sx={{ py: 6, color: 'text.secondary' }}>
                      Nenhum evento registrado ainda.
                    </TableCell>
                  </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>
  );
}