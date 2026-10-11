package com.example

import com.example.data.model.InterestMethod
import com.example.data.model.Loan
import com.example.data.model.LoanStatus
import com.example.data.model.RepaymentFrequency
import com.example.ui.components.Formatters
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testLoanInterestAndContractualAmountCalculation() {
        val principal = 100000L
        val interestRate = 20.0
        val interestAmount = (principal * interestRate / 100.0).toLong()
        val totalContractualAmount = principal + interestAmount
        val dailyInstallment = 5000L
        val durationDays = 24

        assertEquals(20000L, interestAmount)
        assertEquals(120000L, totalContractualAmount)
        assertEquals(120000L, dailyInstallment * durationDays)
    }

    @Test
    fun testPartialPaymentsAndLoanCompletion() {
        var loan = Loan(
            id = "loan_test_001",
            loanNumber = "LN-TEST",
            customerId = "cust_001",
            customerName = "Test Borrower",
            principalAmount = 100000L,
            interestMethod = InterestMethod.PERCENTAGE,
            interestRate = 20.0,
            interestAmount = 20000L,
            totalContractualAmount = 120000L,
            installmentAmount = 5000L,
            durationDays = 24,
            repaymentFrequency = RepaymentFrequency.DAILY,
            remainingBalance = 120000L,
            totalPaid = 0L,
            status = LoanStatus.ACTIVE
        )

        // Payment 1: TSh 5,000
        val p1 = 5000L
        loan = loan.copy(
            remainingBalance = loan.remainingBalance - p1,
            totalPaid = loan.totalPaid + p1
        )
        assertEquals(115000L, loan.remainingBalance)
        assertEquals(5000L, loan.totalPaid)
        assertEquals(LoanStatus.ACTIVE, loan.status)

        // Payment 2: TSh 15,000
        val p2 = 15000L
        loan = loan.copy(
            remainingBalance = loan.remainingBalance - p2,
            totalPaid = loan.totalPaid + p2
        )
        assertEquals(100000L, loan.remainingBalance)
        assertEquals(20000L, loan.totalPaid)

        // Payment 3: TSh 50,000
        val p3 = 5000L * 10 // covers 10 daily installments
        loan = loan.copy(
            remainingBalance = loan.remainingBalance - p3,
            totalPaid = loan.totalPaid + p3
        )
        assertEquals(50000L, loan.remainingBalance)
        assertEquals(70000L, loan.totalPaid)

        // Final Payment: Full remaining balance TSh 50,000
        val finalPayment = loan.remainingBalance
        val newBalance = loan.remainingBalance - finalPayment
        loan = loan.copy(
            remainingBalance = newBalance,
            totalPaid = loan.totalPaid + finalPayment,
            status = if (newBalance <= 0L) LoanStatus.COMPLETED else LoanStatus.ACTIVE
        )
        assertEquals(0L, loan.remainingBalance)
        assertEquals(120000L, loan.totalPaid)
        assertEquals(LoanStatus.COMPLETED, loan.status)
    }

    @Test
    fun testCurrencyFormatting() {
        assertEquals("TSh 100,000", Formatters.formatTsh(100000L))
        assertEquals("TSh 25,000,000", Formatters.formatTsh(25000000L))
        assertEquals("TSh 5,000", Formatters.formatTsh(5000L))
    }
}
