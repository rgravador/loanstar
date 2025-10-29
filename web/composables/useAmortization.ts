import type { AmortizationScheduleItem, PaymentFrequency } from '~/types'
import { addPaymentPeriod, formatDateInput } from '~/utils/formatters'

/**
 * Calculate amortization schedule for a loan
 */
export function useAmortization() {
  /**
   * Generate complete amortization schedule
   */
  const generateSchedule = (
    principalAmount: number,
    interestRateMonthly: number, // e.g., 3.5 for 3.5%
    tenureMonths: number,
    paymentFrequency: PaymentFrequency,
    startDate: Date = new Date()
  ): AmortizationScheduleItem[] => {
    // Convert monthly interest rate to decimal
    const monthlyRate = interestRateMonthly / 100

    // Calculate number of payments based on frequency
    let numberOfPayments: number
    let periodRate: number

    switch (paymentFrequency) {
      case 'bi-monthly':
        // 2 payments per month
        numberOfPayments = tenureMonths * 2
        periodRate = monthlyRate / 2
        break
      case 'weekly':
        // ~4.33 payments per month
        numberOfPayments = Math.ceil(tenureMonths * 4.33)
        periodRate = monthlyRate / 4.33
        break
      case 'monthly':
      default:
        numberOfPayments = tenureMonths
        periodRate = monthlyRate
        break
    }

    // Calculate EMI using formula: EMI = P * r * (1+r)^n / ((1+r)^n - 1)
    const emi = calculateEMI(principalAmount, periodRate, numberOfPayments)

    const schedule: AmortizationScheduleItem[] = []
    let remainingBalance = principalAmount
    let currentDate = new Date(startDate)

    for (let i = 1; i <= numberOfPayments; i++) {
      // Calculate due date
      currentDate = addPaymentPeriod(currentDate, paymentFrequency)

      // Calculate interest for this period
      const interestDue = remainingBalance * periodRate

      // Calculate principal for this period
      let principalDue = emi - interestDue

      // Adjust last payment to account for rounding
      if (i === numberOfPayments) {
        principalDue = remainingBalance
      }

      // Update remaining balance
      remainingBalance -= principalDue

      // Add to schedule
      schedule.push({
        payment_number: i,
        due_date: formatDateInput(currentDate),
        principal_due: Math.round(principalDue * 100) / 100,
        interest_due: Math.round(interestDue * 100) / 100,
        total_due: Math.round((principalDue + interestDue) * 100) / 100,
        remaining_balance: Math.max(0, Math.round(remainingBalance * 100) / 100)
      })
    }

    return schedule
  }

  /**
   * Calculate EMI (Equated Monthly Installment)
   */
  const calculateEMI = (principal: number, rate: number, periods: number): number => {
    if (rate === 0) {
      return principal / periods
    }

    const emi = (principal * rate * Math.pow(1 + rate, periods)) / (Math.pow(1 + rate, periods) - 1)
    return Math.round(emi * 100) / 100
  }

  /**
   * Calculate total interest for a loan
   */
  const calculateTotalInterest = (
    principalAmount: number,
    interestRateMonthly: number,
    tenureMonths: number,
    paymentFrequency: PaymentFrequency
  ): number => {
    const schedule = generateSchedule(principalAmount, interestRateMonthly, tenureMonths, paymentFrequency)
    const totalInterest = schedule.reduce((sum, item) => sum + item.interest_due, 0)
    return Math.round(totalInterest * 100) / 100
  }

  /**
   * Calculate total amount to be paid (principal + interest)
   */
  const calculateTotalAmount = (
    principalAmount: number,
    interestRateMonthly: number,
    tenureMonths: number,
    paymentFrequency: PaymentFrequency
  ): number => {
    const totalInterest = calculateTotalInterest(principalAmount, interestRateMonthly, tenureMonths, paymentFrequency)
    return principalAmount + totalInterest
  }

  return {
    generateSchedule,
    calculateEMI,
    calculateTotalInterest,
    calculateTotalAmount
  }
}
