<template>
  <v-container fluid>
    <v-row>
      <v-col cols="12">
        <div class="mb-4">
          <h1 class="text-h4">Loan Approvals</h1>
          <p class="text-subtitle-1 text-grey">Review and approve pending loan applications</p>
        </div>
      </v-col>
    </v-row>

    <!-- Stats -->
    <v-row>
      <v-col cols="12" sm="4">
        <v-card>
          <v-card-text>
            <div class="text-caption text-grey">Pending Approvals</div>
            <div class="text-h5 text-warning">{{ pendingLoans.length }}</div>
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" sm="4">
        <v-card>
          <v-card-text>
            <div class="text-caption text-grey">Total Amount Pending</div>
            <div class="text-h5">{{ formatCurrency(totalPendingAmount) }}</div>
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" sm="4">
        <v-card>
          <v-card-text>
            <div class="text-caption text-grey">Approved Today</div>
            <div class="text-h5 text-success">{{ approvedTodayCount }}</div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Loading State -->
    <v-row v-if="loansStore.loading" class="mt-4">
      <v-col cols="12" class="text-center">
        <v-progress-circular indeterminate color="primary" size="64" />
        <p class="mt-4 text-grey">Loading pending loans...</p>
      </v-col>
    </v-row>

    <!-- Pending Loans List -->
    <v-row v-else-if="pendingLoans.length > 0" class="mt-4">
      <v-col cols="12">
        <v-card>
          <v-card-title>Pending Loan Applications ({{ pendingLoans.length }})</v-card-title>
          <v-divider />

          <!-- Desktop View -->
          <div class="d-none d-md-block">
            <v-table>
              <thead>
                <tr>
                  <th>Account</th>
                  <th>Agent</th>
                  <th>Principal</th>
                  <th>Interest Rate</th>
                  <th>Tenure</th>
                  <th>Total Amount</th>
                  <th>Created</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="loan in pendingLoans" :key="loan.id">
                  <td>
                    <div>
                      <div class="font-weight-bold">{{ loan.account?.name }}</div>
                      <div class="text-caption text-grey">{{ loan.account?.contact_number }}</div>
                    </div>
                  </td>
                  <td>{{ loan.agent?.full_name || 'N/A' }}</td>
                  <td class="font-weight-bold">{{ formatCurrency(loan.principal_amount) }}</td>
                  <td>{{ loan.interest_rate }}%</td>
                  <td>{{ loan.tenure_months }} months</td>
                  <td class="font-weight-bold">{{ formatCurrency(calculateTotalAmount(loan)) }}</td>
                  <td>{{ formatDate(loan.created_at) }}</td>
                  <td>
                    <div class="d-flex gap-1">
                      <v-btn
                        size="small"
                        color="success"
                        variant="tonal"
                        @click="openApproveDialog(loan)"
                      >
                        Approve
                      </v-btn>
                      <v-btn
                        size="small"
                        color="error"
                        variant="outlined"
                        @click="openRejectDialog(loan)"
                      >
                        Reject
                      </v-btn>
                      <v-btn
                        size="small"
                        variant="text"
                        icon
                        @click="openDetailsDialog(loan)"
                      >
                        <v-icon>mdi-eye</v-icon>
                      </v-btn>
                    </div>
                  </td>
                </tr>
              </tbody>
            </v-table>
          </div>

          <!-- Mobile View -->
          <div class="d-md-none">
            <v-card
              v-for="loan in pendingLoans"
              :key="loan.id"
              variant="outlined"
              class="ma-2"
            >
              <v-card-text>
                <div class="mb-2">
                  <div class="font-weight-bold">{{ loan.account?.name }}</div>
                  <div class="text-caption text-grey">{{ loan.account?.contact_number }}</div>
                </div>
                <div class="mb-2">
                  <v-chip size="small" class="mr-1">{{ formatCurrency(loan.principal_amount) }}</v-chip>
                  <v-chip size="small" class="mr-1">{{ loan.interest_rate }}%</v-chip>
                  <v-chip size="small">{{ loan.tenure_months }}mo</v-chip>
                </div>
                <div class="text-caption text-grey mb-3">
                  Agent: {{ loan.agent?.full_name }} • {{ formatDate(loan.created_at) }}
                </div>
                <div class="d-flex gap-2">
                  <v-btn
                    size="small"
                    color="success"
                    block
                    @click="openApproveDialog(loan)"
                  >
                    Approve
                  </v-btn>
                  <v-btn
                    size="small"
                    color="error"
                    variant="outlined"
                    block
                    @click="openRejectDialog(loan)"
                  >
                    Reject
                  </v-btn>
                  <v-btn
                    size="small"
                    variant="text"
                    icon
                    @click="openDetailsDialog(loan)"
                  >
                    <v-icon>mdi-eye</v-icon>
                  </v-btn>
                </div>
              </v-card-text>
            </v-card>
          </div>
        </v-card>
      </v-col>
    </v-row>

    <!-- Empty State -->
    <v-row v-else class="mt-8">
      <v-col cols="12" class="text-center">
        <v-icon size="120" color="grey-lighten-2">mdi-check-all</v-icon>
        <h3 class="text-h6 mt-4 text-grey">No pending approvals</h3>
        <p class="text-body-2 text-grey">All loan applications have been processed</p>
      </v-col>
    </v-row>

    <!-- Approve Dialog -->
    <v-dialog v-model="approveDialog" max-width="500">
      <v-card v-if="selectedLoan">
        <v-card-title>Approve Loan</v-card-title>
        <v-card-text>
          <v-alert type="info" variant="tonal" class="mb-4">
            <div class="mb-2">
              <strong>Account:</strong> {{ selectedLoan.account?.name }}
            </div>
            <div class="mb-2">
              <strong>Principal:</strong> {{ formatCurrency(selectedLoan.principal_amount) }}
            </div>
            <div class="mb-2">
              <strong>Total Amount:</strong> {{ formatCurrency(calculateTotalAmount(selectedLoan)) }}
            </div>
            <div>
              <strong>Tenure:</strong> {{ selectedLoan.tenure_months }} months
            </div>
          </v-alert>

          <p>Are you sure you want to approve this loan application?</p>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="approveDialog = false">Cancel</v-btn>
          <v-btn
            color="success"
            @click="handleApproveLoan"
            :loading="actionLoading"
          >
            Approve Loan
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Reject Dialog -->
    <v-dialog v-model="rejectDialog" max-width="500">
      <v-card v-if="selectedLoan">
        <v-card-title>Reject Loan</v-card-title>
        <v-card-text>
          <v-alert type="warning" variant="tonal" class="mb-4">
            <div class="mb-2">
              <strong>Account:</strong> {{ selectedLoan.account?.name }}
            </div>
            <div>
              <strong>Principal:</strong> {{ formatCurrency(selectedLoan.principal_amount) }}
            </div>
          </v-alert>

          <v-textarea
            v-model="rejectionReason"
            label="Reason for rejection *"
            variant="outlined"
            rows="3"
            :rules="[rules.required]"
            placeholder="Please provide a reason for rejecting this loan application..."
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="rejectDialog = false">Cancel</v-btn>
          <v-btn
            color="error"
            @click="handleRejectLoan"
            :loading="actionLoading"
            :disabled="!rejectionReason"
          >
            Reject Loan
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Details Dialog -->
    <v-dialog v-model="detailsDialog" max-width="800">
      <v-card v-if="selectedLoan">
        <v-card-title class="d-flex justify-space-between align-center">
          <span>Loan Application Details</span>
          <v-btn icon variant="text" @click="detailsDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-card-title>
        <v-divider />
        <v-card-text>
          <v-row>
            <v-col cols="12" md="6">
              <h4 class="text-subtitle-1 mb-3">Borrower Information</h4>
              <div class="mb-2">
                <strong>Name:</strong> {{ selectedLoan.account?.name }}
              </div>
              <div class="mb-2">
                <strong>Contact:</strong> {{ selectedLoan.account?.contact_number }}
              </div>
              <div class="mb-2">
                <strong>Address:</strong> {{ selectedLoan.account?.address }}
              </div>
            </v-col>
            <v-col cols="12" md="6">
              <h4 class="text-subtitle-1 mb-3">Loan Details</h4>
              <div class="mb-2">
                <strong>Principal:</strong> {{ formatCurrency(selectedLoan.principal_amount) }}
              </div>
              <div class="mb-2">
                <strong>Interest Rate:</strong> {{ selectedLoan.interest_rate }}% per month
              </div>
              <div class="mb-2">
                <strong>Tenure:</strong> {{ selectedLoan.tenure_months }} months
              </div>
              <div class="mb-2">
                <strong>Frequency:</strong> {{ selectedLoan.payment_frequency }}
              </div>
              <div class="mb-2">
                <strong>Total Amount:</strong> {{ formatCurrency(calculateTotalAmount(selectedLoan)) }}
              </div>
            </v-col>
          </v-row>

          <v-divider class="my-4" />

          <h4 class="text-subtitle-1 mb-3">Amortization Schedule Preview</h4>
          <div style="max-height: 300px; overflow-y: auto;">
            <v-table density="compact">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Due Date</th>
                  <th>Principal</th>
                  <th>Interest</th>
                  <th>Total</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in selectedLoan.amortization_schedule" :key="item.payment_number">
                  <td>{{ item.payment_number }}</td>
                  <td>{{ formatDate(item.due_date) }}</td>
                  <td>{{ formatCurrency(item.principal_due) }}</td>
                  <td>{{ formatCurrency(item.interest_due) }}</td>
                  <td>{{ formatCurrency(item.total_due) }}</td>
                </tr>
              </tbody>
            </v-table>
          </div>
        </v-card-text>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { formatCurrency, formatDate } from '~/utils/formatters'
import type { Loan } from '~/types'

definePageMeta({
  middleware: ['auth', 'admin']
})

const loansStore = useLoansStore()
const uiStore = useUIStore()

const approveDialog = ref(false)
const rejectDialog = ref(false)
const detailsDialog = ref(false)
const selectedLoan = ref<Loan | null>(null)
const rejectionReason = ref('')
const actionLoading = ref(false)

const rules = {
  required: (v: any) => !!v || 'This field is required'
}

const pendingLoans = computed(() => {
  return loansStore.loans.filter(loan => loan.status === 'pending_approval')
})

const totalPendingAmount = computed(() => {
  return pendingLoans.value.reduce((sum, loan) => sum + loan.principal_amount, 0)
})

const approvedTodayCount = computed(() => {
  const today = new Date().toDateString()
  return loansStore.loans.filter(
    loan => loan.status === 'approved' &&
    loan.approved_at &&
    new Date(loan.approved_at).toDateString() === today
  ).length
})

const calculateTotalAmount = (loan: Loan) => {
  return loan.amortization_schedule.reduce((sum, item) => sum + item.total_due, 0)
}

const openApproveDialog = (loan: Loan) => {
  selectedLoan.value = loan
  approveDialog.value = true
}

const openRejectDialog = (loan: Loan) => {
  selectedLoan.value = loan
  rejectionReason.value = ''
  rejectDialog.value = true
}

const openDetailsDialog = (loan: Loan) => {
  selectedLoan.value = loan
  detailsDialog.value = true
}

const handleApproveLoan = async () => {
  if (!selectedLoan.value) return

  actionLoading.value = true
  const result = await loansStore.approveLoan(selectedLoan.value.id)

  if (result.success) {
    uiStore.showSuccess('Loan approved successfully')
    approveDialog.value = false
    selectedLoan.value = null
    await loansStore.fetchLoans()
  } else {
    uiStore.showError(result.error || 'Failed to approve loan')
  }

  actionLoading.value = false
}

const handleRejectLoan = async () => {
  if (!selectedLoan.value || !rejectionReason.value) return

  actionLoading.value = true
  const result = await loansStore.rejectLoan(selectedLoan.value.id, rejectionReason.value)

  if (result.success) {
    uiStore.showSuccess('Loan rejected')
    rejectDialog.value = false
    selectedLoan.value = null
    rejectionReason.value = ''
    await loansStore.fetchLoans()
  } else {
    uiStore.showError(result.error || 'Failed to reject loan')
  }

  actionLoading.value = false
}

// Fetch loans on mount
onMounted(async () => {
  try {
    await loansStore.fetchLoans()
  } catch (error: any) {
    uiStore.showError('Failed to load loans')
  }
})
</script>
