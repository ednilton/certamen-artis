'use client';

import Alert, { type AlertColor } from '@mui/material/Alert';
import Snackbar from '@mui/material/Snackbar';

export interface Feedback {
  severity: AlertColor;
  message: string;
}

interface FeedbackSnackbarProps {
  feedback: Feedback | null;
  onClose: () => void;
}

export default function FeedbackSnackbar({ feedback, onClose }: FeedbackSnackbarProps) {
  return (
    <Snackbar
      open={Boolean(feedback)}
      autoHideDuration={5000}
      onClose={(_, reason) => {
        if (reason !== 'clickaway') onClose();
      }}
      anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
    >
      {feedback ? (
        <Alert severity={feedback.severity} variant="filled" onClose={onClose} sx={{ width: '100%' }}>
          {feedback.message}
        </Alert>
      ) : undefined}
    </Snackbar>
  );
}
