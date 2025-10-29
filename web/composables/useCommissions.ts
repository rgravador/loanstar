/**
 * Calculate agent commissions
 */
export function useCommissions() {
  /**
   * Calculate commission from a payment
   * Commission is earned only on the interest portion of the payment
   */
  const calculateCommission = (interestPaid: number, commissionPercentage: number): number => {
    const commission = interestPaid * (commissionPercentage / 100)
    return Math.round(commission * 100) / 100
  }

  /**
   * Calculate commission from payment breakdown
   */
  const calculateCommissionFromPayment = (
    appliedToInterest: number,
    commissionPercentage: number
  ): number => {
    return calculateCommission(appliedToInterest, commissionPercentage)
  }

  /**
   * Calculate total commission from multiple payments
   */
  const calculateTotalCommission = (
    payments: Array<{ appliedToInterest: number }>,
    commissionPercentage: number
  ): number => {
    const totalInterest = payments.reduce((sum, payment) => sum + payment.appliedToInterest, 0)
    return calculateCommission(totalInterest, commissionPercentage)
  }

  /**
   * Calculate projected commission for a loan
   * Based on the total interest that will be collected
   */
  const calculateProjectedCommission = (
    totalInterest: number,
    commissionPercentage: number
  ): number => {
    return calculateCommission(totalInterest, commissionPercentage)
  }

  return {
    calculateCommission,
    calculateCommissionFromPayment,
    calculateTotalCommission,
    calculateProjectedCommission
  }
}
