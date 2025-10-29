import { format, parseISO, differenceInDays, addDays, addWeeks, addMonths } from 'date-fns'

/**
 * Format number as currency
 */
export function formatCurrency(amount: number, currency = 'PHP'): string {
  return new Intl.NumberFormat('en-PH', {
    style: 'currency',
    currency: currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount)
}

/**
 * Parse currency string to number
 */
export function parseCurrency(value: string): number {
  return parseFloat(value.replace(/[^0-9.-]+/g, ''))
}

/**
 * Format number with K, M notation
 */
export function formatCompactNumber(num: number): string {
  if (num >= 1000000) {
    return `${(num / 1000000).toFixed(1)}M`
  }
  if (num >= 1000) {
    return `${(num / 1000).toFixed(1)}K`
  }
  return num.toString()
}

/**
 * Format date to readable string
 */
export function formatDate(date: string | Date, formatStr = 'MMM dd, yyyy'): string {
  const dateObj = typeof date === 'string' ? parseISO(date) : date
  return format(dateObj, formatStr)
}

/**
 * Format date to input value (YYYY-MM-DD)
 */
export function formatDateInput(date: string | Date): string {
  const dateObj = typeof date === 'string' ? parseISO(date) : date
  return format(dateObj, 'yyyy-MM-dd')
}

/**
 * Format relative time (e.g., "2 days ago")
 */
export function formatRelativeTime(date: string | Date): string {
  const dateObj = typeof date === 'string' ? parseISO(date) : date
  const days = differenceInDays(new Date(), dateObj)

  if (days === 0) return 'Today'
  if (days === 1) return 'Yesterday'
  if (days < 7) return `${days} days ago`
  if (days < 30) return `${Math.floor(days / 7)} weeks ago`
  if (days < 365) return `${Math.floor(days / 30)} months ago`
  return `${Math.floor(days / 365)} years ago`
}

/**
 * Calculate days between two dates
 */
export function daysBetween(start: string | Date, end: string | Date): number {
  const startDate = typeof start === 'string' ? parseISO(start) : start
  const endDate = typeof end === 'string' ? parseISO(end) : end
  return differenceInDays(endDate, startDate)
}

/**
 * Check if date is past due
 */
export function isPastDue(dueDate: string | Date): boolean {
  const dueDateObj = typeof dueDate === 'string' ? parseISO(dueDate) : dueDate
  return differenceInDays(new Date(), dueDateObj) > 0
}

/**
 * Check if date is upcoming (within specified days)
 */
export function isUpcoming(dueDate: string | Date, days = 5): boolean {
  const dueDateObj = typeof dueDate === 'string' ? parseISO(dueDate) : dueDate
  const daysUntilDue = differenceInDays(dueDateObj, new Date())
  return daysUntilDue > 0 && daysUntilDue <= days
}

/**
 * Add payment period to date based on frequency
 */
export function addPaymentPeriod(date: Date, frequency: 'bi-monthly' | 'monthly' | 'weekly'): Date {
  switch (frequency) {
    case 'bi-monthly':
      return addDays(date, 15)
    case 'monthly':
      return addMonths(date, 1)
    case 'weekly':
      return addWeeks(date, 1)
    default:
      return date
  }
}

/**
 * Format percentage
 */
export function formatPercentage(value: number, decimals = 2): string {
  return `${value.toFixed(decimals)}%`
}
