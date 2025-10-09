package com.example.loanstar.util

import com.example.loanstar.data.model.AmortizationEntry
import com.example.loanstar.data.model.PaymentFrequency
import java.util.Calendar
import kotlin.math.pow

/**
 * Utility class for loan calculations including EMI, amortization schedules, and penalties.
 *
 * Interest rates are simple interest per month (3-5%).
 * Penalties are 3% of due amortization amount per month, calculated daily.
 */
object LoanCalculator {

    /**
     * Calculate EMI (Equated Monthly Installment) using the formula:
     * EMI = P × r × (1+r)^n / ((1+r)^n - 1)
     *
     * @param principal Loan principal amount
     * @param annualInterestRate Annual interest rate in percentage (e.g., 36 for 3% per month)
     * @param tenureMonths Loan tenure in months
     * @return EMI amount
     */
    fun calculateEMI(
        principal: Double,
        monthlyInterestRate: Double, // 3-5% per month
        tenureMonths: Int
    ): Double {
        val rate = monthlyInterestRate / 100.0
        val n = tenureMonths.toDouble()

        return if (rate == 0.0) {
            principal / n
        } else {
            val numerator = principal * rate * (1 + rate).pow(n)
            val denominator = (1 + rate).pow(n) - 1
            numerator / denominator
        }
    }

    /**
     * Generate complete amortization schedule for a loan
     *
     * @param principal Loan principal amount
     * @param monthlyInterestRate Interest rate per month in percentage (3-5%)
     * @param tenureMonths Loan tenure in months
     * @param paymentFrequency Payment frequency (weekly, bi-monthly, monthly)
     * @param startDate Start date of the loan in milliseconds
     * @return List of amortization entries
     */
    fun generateAmortizationSchedule(
        principal: Double,
        monthlyInterestRate: Double,
        tenureMonths: Int,
        paymentFrequency: PaymentFrequency,
        startDate: Long = System.currentTimeMillis()
    ): List<AmortizationEntry> {
        val entries = mutableListOf<AmortizationEntry>()

        // Calculate number of payments based on frequency
        val numberOfPayments = calculateNumberOfPayments(tenureMonths, paymentFrequency)

        // Calculate payment amount per period
        val paymentAmount = calculatePaymentAmount(
            principal,
            monthlyInterestRate,
            tenureMonths,
            paymentFrequency
        )

        // Calculate interest rate per payment period
        val periodInterestRate = calculatePeriodInterestRate(monthlyInterestRate, paymentFrequency)

        var remainingBalance = principal
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startDate

        for (i in 1..numberOfPayments) {
            // Calculate due date based on frequency
            val dueDate = calculateNextDueDate(calendar, paymentFrequency, i - 1)

            // Calculate interest for this period
            val interestDue = remainingBalance * (periodInterestRate / 100.0)

            // Calculate principal for this period
            val principalDue = paymentAmount - interestDue

            // Update remaining balance
            remainingBalance = maxOf(0.0, remainingBalance - principalDue)

            // Create amortization entry
            entries.add(
                AmortizationEntry(
                    paymentNumber = i,
                    dueDate = dueDate,
                    principalDue = principalDue,
                    interestDue = interestDue,
                    totalDue = paymentAmount,
                    remainingBalance = remainingBalance,
                    penalty = 0.0,
                    isPaid = false
                )
            )
        }

        return entries
    }

    /**
     * Calculate number of payments based on tenure and frequency
     */
    private fun calculateNumberOfPayments(
        tenureMonths: Int,
        frequency: PaymentFrequency
    ): Int {
        return when (frequency) {
            PaymentFrequency.WEEKLY -> tenureMonths * 4 // Approximately 4 weeks per month
            PaymentFrequency.BI_MONTHLY -> tenureMonths * 2 // 2 payments per month
            PaymentFrequency.MONTHLY -> tenureMonths // 1 payment per month
        }
    }

    /**
     * Calculate payment amount per period
     */
    private fun calculatePaymentAmount(
        principal: Double,
        monthlyInterestRate: Double,
        tenureMonths: Int,
        frequency: PaymentFrequency
    ): Double {
        val numberOfPayments = calculateNumberOfPayments(tenureMonths, frequency)
        val periodInterestRate = calculatePeriodInterestRate(monthlyInterestRate, frequency)

        val rate = periodInterestRate / 100.0

        return if (rate == 0.0) {
            principal / numberOfPayments
        } else {
            val numerator = principal * rate * (1 + rate).pow(numberOfPayments.toDouble())
            val denominator = (1 + rate).pow(numberOfPayments.toDouble()) - 1
            numerator / denominator
        }
    }

    /**
     * Calculate interest rate per payment period
     */
    private fun calculatePeriodInterestRate(
        monthlyInterestRate: Double,
        frequency: PaymentFrequency
    ): Double {
        return when (frequency) {
            PaymentFrequency.WEEKLY -> monthlyInterestRate / 4.0 // Weekly rate
            PaymentFrequency.BI_MONTHLY -> monthlyInterestRate / 2.0 // Bi-monthly rate
            PaymentFrequency.MONTHLY -> monthlyInterestRate // Monthly rate
        }
    }

    /**
     * Calculate next due date based on payment frequency
     */
    private fun calculateNextDueDate(
        startCalendar: Calendar,
        frequency: PaymentFrequency,
        paymentIndex: Int
    ): Long {
        val calendar = startCalendar.clone() as Calendar

        when (frequency) {
            PaymentFrequency.WEEKLY -> {
                calendar.add(Calendar.DAY_OF_MONTH, 7 * (paymentIndex + 1))
            }
            PaymentFrequency.BI_MONTHLY -> {
                calendar.add(Calendar.DAY_OF_MONTH, 15 * (paymentIndex + 1))
            }
            PaymentFrequency.MONTHLY -> {
                calendar.add(Calendar.MONTH, paymentIndex + 1)
            }
        }

        return calendar.timeInMillis
    }

    /**
     * Calculate penalty for overdue payment
     * Penalty is 3% of due amortization amount per month, computed daily
     *
     * @param dueAmount Amount that was due
     * @param daysOverdue Number of days overdue
     * @return Penalty amount
     */
    fun calculatePenalty(dueAmount: Double, daysOverdue: Int): Double {
        if (daysOverdue <= 0) return 0.0

        val monthlyPenaltyRate = 0.03 // 3% per month
        val dailyPenaltyRate = monthlyPenaltyRate / 30.0

        return dueAmount * dailyPenaltyRate * daysOverdue
    }

    /**
     * Calculate days overdue from due date
     *
     * @param dueDate Due date in milliseconds
     * @param currentDate Current date in milliseconds (defaults to now)
     * @return Number of days overdue (0 if not overdue)
     */
    fun calculateDaysOverdue(
        dueDate: Long,
        currentDate: Long = System.currentTimeMillis()
    ): Int {
        val diffMillis = currentDate - dueDate
        val days = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
        return maxOf(0, days)
    }

    /**
     * Calculate total amount due including penalties
     *
     * @param amortizationEntry The amortization entry
     * @param currentDate Current date in milliseconds (defaults to now)
     * @return Total amount due including penalties
     */
    fun calculateTotalDueWithPenalty(
        amortizationEntry: AmortizationEntry,
        currentDate: Long = System.currentTimeMillis()
    ): Double {
        val daysOverdue = calculateDaysOverdue(amortizationEntry.dueDate, currentDate)
        val penalty = calculatePenalty(amortizationEntry.totalDue, daysOverdue)
        return amortizationEntry.totalDue - amortizationEntry.paidAmount + penalty
    }

    /**
     * Validate loan parameters
     *
     * @param principal Loan principal amount
     * @param monthlyInterestRate Interest rate per month in percentage
     * @param tenureMonths Loan tenure in months
     * @return List of validation errors (empty if valid)
     */
    fun validateLoanParameters(
        principal: Double,
        monthlyInterestRate: Double,
        tenureMonths: Int
    ): List<String> {
        val errors = mutableListOf<String>()

        if (principal <= 0) {
            errors.add("Principal amount must be greater than 0")
        }

        if (monthlyInterestRate < 3.0 || monthlyInterestRate > 5.0) {
            errors.add("Interest rate must be between 3% and 5% per month")
        }

        if (tenureMonths < 2 || tenureMonths > 12) {
            errors.add("Tenure must be between 2 and 12 months")
        }

        return errors
    }

    /**
     * Calculate total interest for the loan
     */
    fun calculateTotalInterest(
        principal: Double,
        monthlyInterestRate: Double,
        tenureMonths: Int,
        paymentFrequency: PaymentFrequency
    ): Double {
        val schedule = generateAmortizationSchedule(
            principal,
            monthlyInterestRate,
            tenureMonths,
            paymentFrequency
        )
        return schedule.sumOf { it.interestDue }
    }

    /**
     * Calculate total amount to be paid (principal + interest)
     */
    fun calculateTotalAmount(
        principal: Double,
        monthlyInterestRate: Double,
        tenureMonths: Int,
        paymentFrequency: PaymentFrequency
    ): Double {
        return principal + calculateTotalInterest(
            principal,
            monthlyInterestRate,
            tenureMonths,
            paymentFrequency
        )
    }
}
