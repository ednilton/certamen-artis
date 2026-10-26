import Chip from '@mui/material/Chip';
import type { EventType } from '@/lib/api';
import { EVENT_TYPE_LABELS } from '@/utils/format';

const COLORS: Record<EventType, 'primary' | 'secondary'> = {
  PRODUCTION: 'primary',
  QUALITY: 'secondary',
};

export default function EventTypeChip({ type }: { type: EventType }) {
  return <Chip label={EVENT_TYPE_LABELS[type]} color={COLORS[type]} size="small" variant="outlined" />;
}
