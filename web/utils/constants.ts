// Loan business rules
export const MIN_INTEREST_RATE = 3
export const MAX_INTEREST_RATE = 5
export const MIN_TENURE_MONTHS = 2
export const MAX_TENURE_MONTHS = 12
export const PENALTY_RATE_MONTHLY = 0.03 // 3% per month
export const PENALTY_DAYS_IN_MONTH = 30
export const MIN_CASHOUT_AMOUNT = 10

// Payment frequencies
export const PAYMENT_FREQUENCIES = {
  'bi-monthly': { days: 15, label: 'Bi-Monthly (Every 15 days)' },
  'monthly': { days: 30, label: 'Monthly' },
  'weekly': { days: 7, label: 'Weekly' }
} as const

// Status colors
export const STATUS_COLORS = {
  // Loan statuses
  pending_approval: 'warning',
  approved: 'info',
  active: 'success',
  closed: 'grey',
  rejected: 'error',

  // Account statuses
  active_account: 'success',
  inactive: 'grey',
  suspended: 'error',

  // Cashout statuses
  pending: 'warning',
  cashout_approved: 'success',
  cashout_rejected: 'error'
} as const

// Notification type colors
export const NOTIFICATION_COLORS = {
  past_due: 'error',
  upcoming_due: 'warning',
  loan_approval: 'success',
  loan_rejection: 'error',
  cashout_approved: 'success',
  cashout_rejected: 'error',
  payment_received: 'success'
} as const

// Notification icons
export const NOTIFICATION_ICONS = {
  past_due: 'mdi-alert-circle',
  upcoming_due: 'mdi-clock-alert',
  loan_approval: 'mdi-check-circle',
  loan_rejection: 'mdi-close-circle',
  cashout_approved: 'mdi-cash-check',
  cashout_rejected: 'mdi-cash-remove',
  payment_received: 'mdi-cash-plus'
} as const
