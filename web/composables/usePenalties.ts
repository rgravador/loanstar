import { PENALTY_RATE_MONTHLY, PENALTY_DAYS_IN_MONTH } from '~/utils/constants'
import { daysBetween } from '~/utils/formatters'

/**
 * Calculate penalties for overdue payments
 */
export function usePenalties() {
  /**
   * Calculate penalty for a single overdue payment
   * Formula: Penalty per day = (Due amount * 3%) / 30
   * Total penalty = Penalty per day * Days overdue
   */
  const calculatePenalty = (dueAmount: number, dueDate: string | Date, currentDate: Date = new Date()): number => {
    const daysOverdue = daysBetween(dueDate, currentDate)

    if (daysOverdue <= 0) {
      return 0
    }

    // Calculate daily penalty rate
    const penaltyPerDay = (dueAmount * PENALTY_RATE_MONTHLY) / PENALTY_DAYS_IN_MONTH

    // Calculate total penalty
    const totalPenalty = penaltyPerDay * daysOverdue

    return Math.round(totalPenalty * 100) / 100
  }

  /**
   * Calculate penalty for multiple overdue payments
   */
  const calculateTotalPenalties = (
    payments: Array<{ amount: number; dueDate: string }>,
    currentDate: Date = new Date()
  ): number => {
    let totalPenalty = 0

    for (const payment of payments) {
      const penalty = calculatePenalty(payment.amount, payment.dueDate, currentDate)
      totalPenalty += penalty
    }

    return Math.round(totalPenalty * 100) / 100
  }

  /**
   * Get penalty details with breakdown
   */
  const getPenaltyDetails = (
    dueAmount: number,
    dueDate: string | Date,
    currentDate: Date = new Date()
  ) => {
    const daysOverdue = daysBetween(dueDate, currentDate)

    if (daysOverdue <= 0) {
      return {
        daysOverdue: 0,
        penaltyPerDay: 0,
        totalPenalty: 0,
        isPastDue: false
      }
    }

    const penaltyPerDay = (dueAmount * PENALTY_RATE_MONTHLY) / PENALTY_DAYS_IN_MONTH
    const totalPenalty = penaltyPerDay * daysOverdue

    return {
      daysOverdue,
      penaltyPerDay: Math.round(penaltyPerDay * 100) / 100,
      totalPenalty: Math.round(totalPenalty * 100) / 100,
      isPastDue: true
    }
  }

  return {
    calculatePenalty,
    calculateTotalPenalties,
    getPenaltyDetails
  }
}
