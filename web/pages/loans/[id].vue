<template>
  <v-container fluid>
    <!-- Loading State -->
    <v-row v-if="loansStore.loading" class="mt-8">
      <v-col cols="12" class="text-center">
        <v-progress-circular indeterminate color="primary" size="64" />
        <p class="mt-4 text-grey">Loading loan details...</p>
      </v-col>
    </v-row>

    <!-- Loan Details -->
    <template v-else-if="loan">
      <v-row>
        <v-col cols="12">
          <div class="d-flex align-center mb-4">
            <v-btn icon variant="text" @click="$router.back()">
              <v-icon>mdi-arrow-left</v-icon>
            </v-btn>
            <div class="ml-4 flex-grow-1">
              <h1 class="text-h4">{{ formatCurrency(loan.principal_amount) }} Loan</h1>
              <p class="text-subtitle-1">{{ loan.account?.name }}</p>
            </div>
            <v-chip :color="getLoanStatusColor(loan.status)" size="large">
              {{ loan.status.replace('_', ' ').toUpperCase() }}
            </v-chip>
          </div>
        </v-col>
      </v-row>

      <v-row>
        <!-- Loan Summary -->
        <v-col cols="12" md="4">
          <v-card class="mb-4">
            <v-card-title>Loan Summary</v-card-title>
            <v-divider />
            <v-card-text>
              <div class="mb-3">
                <div class="text-caption text-grey">Principal Amount</div>
                <div class="text-h6">{{ formatCurrency(loan.principal_amount) }}</div>
              </div>
              <div class="mb-3">
                <div class="text-caption text-grey">Current Balance</div>
                <div class="text-h6" :class="loan.current_balance > 0 ? 'text-warning' : 'text-success'">
                  {{ formatCurrency(loan.current_balance) }}
                </div>
              </div>
              <div class="mb-3">
                <div class="text-caption text-grey">Total Paid</div>
                <div class="text-h6 text-success">{{ formatCurrency(loan.total_paid) }}</div>
              </div>
              <div v-if="loan.total_penalties > 0" class="mb-3">
                <div class="text-caption text-grey">Total Penalties</div>
                <div class="text-h6 text-error">{{ formatCurrency(loan.total_penalties) }}</div>
              </div>
              <v-divider class="my-3" />
              <div class="mb-2">
                <v-icon size="small" class="mr-2">mdi-percent</v-icon>
                <span>{{ loan.interest_rate }}% per month</span>
              </div>
              <div class="mb-2">
                <v-icon size="small" class="mr-2">mdi-calendar</v-icon>
                <span>{{ loan.tenure_months }} months</span>
              </div>
              <div class="mb-2">
                <v-icon size="small" class="mr-2">mdi-calendar-clock</v-icon>
                <span>{{ loan.payment_frequency }}</span>
              </div>
              <div class="mb-2">
                <v-icon size="small" class="mr-2">mdi-calendar-start</v-icon>
                <span>{{ formatDate(loan.start_date) }}</span>
              </div>
              <div>
                <v-icon size="small" class="mr-2">mdi-calendar-end</v-icon>
                <span>{{ formatDate(loan.end_date) }}</span>
              </div>
            </v-card-text>
            <v-card-actions v-if="loan.status === 'active'">
              <v-btn
                block
                color="primary"
                prepend-icon="mdi-cash-plus"
                :to="`/payments/create?loan_id=${loan.id}`"
              >
                Record Payment
              </v-btn>
            </v-card-actions>
          </v-card>

          <!-- Admin Actions -->
          <v-card v-if="authStore.isAdmin && loan.status === 'pending_approval'" class="mb-4">
            <v-card-title>Admin Actions</v-card-title>
            <v-divider />
            <v-card-text>
              <v-btn
                block
                color="success"
                prepend-icon="mdi-check"
                class="mb-2"
                @click="approveLoanDialog = true"
              >
                Approve Loan
              </v-btn>
              <v-btn
                block
                color="error"
                prepend-icon="mdi-close"
                variant="outlined"
                @click="rejectLoanDialog = true"
              >
                Reject Loan
              </v-btn>
            </v-card-text>
          </v-card>
        </v-col>

        <!-- Amortization Schedule -->
        <v-col cols="12" md="8">
          <v-card>
            <v-card-title class="d-flex justify-space-between align-center">
              <span>Amortization Schedule</span>
              <v-btn size="small" variant="outlined" prepend-icon="mdi-download">
                Export
              </v-btn>
            </v-card-title>
            <v-divider />
            <div style="overflow-x: auto;">
              <v-table density="compact">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Due Date</th>
                    <th>Principal</th>
                    <th>Interest</th>
                    <th>Total Due</th>
                    <th>Balance</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="item in loan.amortization_schedule"
                    :key="item.payment_number"
                    :class="getScheduleRowClass(item)"
                  >
                    <td>{{ item.payment_number }}</td>
                    <td>{{ formatDate(item.due_date) }}</td>
                    <td>{{ formatCurrency(item.principal_due) }}</td>
                    <td>{{ formatCurrency(item.interest_due) }}</td>
                    <td class="font-weight-bold">{{ formatCurrency(item.total_due) }}</td>
                    <td>{{ formatCurrency(item.remaining_balance) }}</td>
                    <td>
                      <v-chip size="x-small" :color="getPaymentStatusColor(item)">
                        {{ getPaymentStatus(item) }}
                      </v-chip>
                    </td>
                  </tr>
                </tbody>
              </v-table>
            </div>
          </v-card>

          <!-- Payment History -->
          <v-card class="mt-4">
            <v-card-title>Payment History ({{ loan.payments?.length || 0 }})</v-card-title>
            <v-divider />
            <v-list v-if="loan.payments && loan.payments.length > 0">
              <v-list-item v-for="payment in loan.payments" :key="payment.id">
                <template #prepend>
                  <v-avatar color="success">
                    <v-icon>mdi-cash</v-icon>
                  </v-avatar>
                </template>
                <v-list-item-title>{{ formatCurrency(payment.amount) }}</v-list-item-title>
                <v-list-item-subtitle>
                  {{ formatDate(payment.payment_date) }} •
                  Principal: {{ formatCurrency(payment.applied_to_principal) }} •
                  Interest: {{ formatCurrency(payment.applied_to_interest) }}
                  <span v-if="payment.applied_to_penalty > 0">
                    • Penalty: {{ formatCurrency(payment.applied_to_penalty) }}
                  </span>
                </v-list-item-subtitle>
              </v-list-item>
            </v-list>
            <v-card-text v-else class="text-center text-grey py-8">
              No payments recorded yet
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </template>

    <!-- Error State -->
    <v-row v-else class="mt-8">
      <v-col cols="12" class="text-center">
        <v-icon size="120" color="error">mdi-alert-circle-outline</v-icon>
        <h3 class="text-h6 mt-4">Loan not found</h3>
        <v-btn color="primary" class="mt-4" @click="$router.back()">
          Go Back
        </v-btn>
      </v-col>
    </v-row>

    <!-- Approve Dialog -->
    <v-dialog v-model="approveLoanDialog" max-width="400">
      <v-card>
        <v-card-title>Approve Loan</v-card-title>
        <v-card-text>
          Are you sure you want to approve this loan?
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="approveLoanDialog = false">Cancel</v-btn>
          <v-btn color="success" @click="handleApproveLoan" :loading="actionLoading">Approve</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Reject Dialog -->
    <v-dialog v-model="rejectLoanDialog" max-width="500">
      <v-card>
        <v-card-title>Reject Loan</v-card-title>
        <v-card-text>
          <v-textarea
            v-model="rejectionReason"
            label="Reason for rejection"
            rows="3"
            variant="outlined"
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="rejectLoanDialog = false">Cancel</v-btn>
          <v-btn
            color="error"
            @click="handleRejectLoan"
            :loading="actionLoading"
            :disabled="!rejectionReason"
          >
            Reject
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { formatCurrency, formatDate, isPastDue } from '~/utils/formatters'
import type { AmortizationScheduleItem } from '~/types'

definePageMeta({
  middleware: 'auth'
})

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const loansStore = useLoansStore()
const uiStore = useUIStore()

const approveLoanDialog = ref(false)
const rejectLoanDialog = ref(false)
const rejectionReason = ref('')
const actionLoading = ref(false)

const loan = computed(() => loansStore.selectedLoan)

const getLoanStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    pending_approval: 'warning',
    approved: 'info',
    active: 'success',
    closed: 'grey',
    rejected: 'error'
  }
  return colors[status] || 'grey'
}

const getScheduleRowClass = (item: AmortizationScheduleItem) => {
  if (loan.value) {
    const totalDue = loan.value.amortization_schedule
      .slice(0, item.payment_number)
      .reduce((sum, i) => sum + i.total_due, 0)

    if (loan.value.total_paid >= totalDue) {
      return 'bg-success-lighten-5'
    } else if (isPastDue(item.due_date)) {
      return 'bg-error-lighten-5'
    }
  }
  return ''
}

const getPaymentStatus = (item: AmortizationScheduleItem) => {
  if (loan.value) {
    const totalDue = loan.value.amortization_schedule
      .slice(0, item.payment_number)
      .reduce((sum, i) => sum + i.total_due, 0)

    if (loan.value.total_paid >= totalDue) {
      return 'Paid'
    } else if (isPastDue(item.due_date)) {
      return 'Overdue'
    } else {
      return 'Pending'
    }
  }
  return 'Pending'
}

const getPaymentStatusColor = (item: AmortizationScheduleItem) => {
  const status = getPaymentStatus(item)
  return status === 'Paid' ? 'success' : status === 'Overdue' ? 'error' : 'grey'
}

const handleApproveLoan = async () => {
  actionLoading.value = true
  const result = await loansStore.approveLoan(loan.value!.id)

  if (result.success) {
    uiStore.showSuccess('Loan approved successfully')
    approveLoanDialog.value = false
    await loansStore.fetchLoanById(route.params.id as string)
  } else {
    uiStore.showError(result.error || 'Failed to approve loan')
  }

  actionLoading.value = false
}

const handleRejectLoan = async () => {
  actionLoading.value = true
  const result = await loansStore.rejectLoan(loan.value!.id, rejectionReason.value)

  if (result.success) {
    uiStore.showSuccess('Loan rejected')
    rejectLoanDialog.value = false
    router.push('/loans')
  } else {
    uiStore.showError(result.error || 'Failed to reject loan')
  }

  actionLoading.value = false
}

// Fetch loan details on mount
onMounted(async () => {
  try {
    await loansStore.fetchLoanById(route.params.id as string)
  } catch (error: any) {
    uiStore.showError('Failed to load loan details')
  }
})
</script>
