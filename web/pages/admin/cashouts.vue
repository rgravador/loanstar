<template>
  <v-container fluid>
    <v-row>
      <v-col cols="12">
        <div class="mb-4">
          <h1 class="text-h4">Cashout Management</h1>
          <p class="text-subtitle-1 text-grey">Review and process agent cashout requests</p>
        </div>
      </v-col>
    </v-row>

    <!-- Stats -->
    <v-row>
      <v-col cols="12" sm="6" md="3">
        <v-card>
          <v-card-text>
            <div class="text-caption text-grey">Pending Requests</div>
            <div class="text-h5 text-warning">{{ pendingCashouts.length }}</div>
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" sm="6" md="3">
        <v-card>
          <v-card-text>
            <div class="text-caption text-grey">Total Pending Amount</div>
            <div class="text-h5">{{ formatCurrency(totalPendingAmount) }}</div>
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" sm="6" md="3">
        <v-card>
          <v-card-text>
            <div class="text-caption text-grey">Approved Today</div>
            <div class="text-h5 text-success">{{ approvedTodayCount }}</div>
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" sm="6" md="3">
        <v-card>
          <v-card-text>
            <div class="text-caption text-grey">Total Processed Today</div>
            <div class="text-h5 text-success">{{ formatCurrency(approvedTodayAmount) }}</div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Loading State -->
    <v-row v-if="cashoutsStore.loading" class="mt-4">
      <v-col cols="12" class="text-center">
        <v-progress-circular indeterminate color="primary" size="64" />
        <p class="mt-4 text-grey">Loading cashout requests...</p>
      </v-col>
    </v-row>

    <!-- Cashout Requests List -->
    <v-row v-else-if="cashoutsStore.cashouts.length > 0" class="mt-4">
      <v-col cols="12">
        <v-card>
          <v-card-title>
            <v-tabs v-model="tab">
              <v-tab value="pending">Pending ({{ pendingCashouts.length }})</v-tab>
              <v-tab value="approved">Approved</v-tab>
              <v-tab value="rejected">Rejected</v-tab>
            </v-tabs>
          </v-card-title>
          <v-divider />

          <v-window v-model="tab">
            <!-- Pending Tab -->
            <v-windowItem value="pending">
              <v-table v-if="pendingCashouts.length > 0">
                <thead>
                  <tr>
                    <th>Agent</th>
                    <th>Request Date</th>
                    <th>Amount</th>
                    <th>Notes</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="cashout in pendingCashouts" :key="cashout.id">
                    <td>
                      <div>
                        <div class="font-weight-bold">{{ cashout.agent?.full_name }}</div>
                        <div class="text-caption text-grey">{{ cashout.agent?.username }}</div>
                      </div>
                    </td>
                    <td>{{ formatDate(cashout.created_at) }}</td>
                    <td class="font-weight-bold">{{ formatCurrency(cashout.amount) }}</td>
                    <td>
                      <span v-if="cashout.notes" class="text-caption">{{ cashout.notes }}</span>
                      <span v-else class="text-grey text-caption">-</span>
                    </td>
                    <td>
                      <div class="d-flex gap-1">
                        <v-btn
                          size="small"
                          color="success"
                          variant="tonal"
                          @click="openApproveDialog(cashout)"
                        >
                          Approve
                        </v-btn>
                        <v-btn
                          size="small"
                          color="error"
                          variant="outlined"
                          @click="openRejectDialog(cashout)"
                        >
                          Reject
                        </v-btn>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </v-table>
              <v-card-text v-else class="text-center text-grey py-8">
                No pending cashout requests
              </v-card-text>
            </v-windowItem>

            <!-- Approved Tab -->
            <v-windowItem value="approved">
              <v-table v-if="approvedCashouts.length > 0">
                <thead>
                  <tr>
                    <th>Agent</th>
                    <th>Request Date</th>
                    <th>Amount</th>
                    <th>Processed Date</th>
                    <th>Processed By</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="cashout in approvedCashouts" :key="cashout.id">
                    <td>{{ cashout.agent?.full_name }}</td>
                    <td>{{ formatDate(cashout.created_at) }}</td>
                    <td class="font-weight-bold text-success">{{ formatCurrency(cashout.amount) }}</td>
                    <td>{{ cashout.processed_at ? formatDate(cashout.processed_at) : '-' }}</td>
                    <td>{{ cashout.processed_by_user?.full_name || '-' }}</td>
                  </tr>
                </tbody>
              </v-table>
              <v-card-text v-else class="text-center text-grey py-8">
                No approved cashouts yet
              </v-card-text>
            </v-windowItem>

            <!-- Rejected Tab -->
            <v-windowItem value="rejected">
              <v-table v-if="rejectedCashouts.length > 0">
                <thead>
                  <tr>
                    <th>Agent</th>
                    <th>Request Date</th>
                    <th>Amount</th>
                    <th>Processed Date</th>
                    <th>Rejection Reason</th>
                    <th>Processed By</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="cashout in rejectedCashouts" :key="cashout.id">
                    <td>{{ cashout.agent?.full_name }}</td>
                    <td>{{ formatDate(cashout.created_at) }}</td>
                    <td class="font-weight-bold">{{ formatCurrency(cashout.amount) }}</td>
                    <td>{{ cashout.processed_at ? formatDate(cashout.processed_at) : '-' }}</td>
                    <td>
                      <span v-if="cashout.notes" class="text-caption text-error">{{ cashout.notes }}</span>
                      <span v-else>-</span>
                    </td>
                    <td>{{ cashout.processed_by_user?.full_name || '-' }}</td>
                  </tr>
                </tbody>
              </v-table>
              <v-card-text v-else class="text-center text-grey py-8">
                No rejected cashouts
              </v-card-text>
            </v-windowItem>
          </v-window>
        </v-card>
      </v-col>
    </v-row>

    <!-- Empty State -->
    <v-row v-else class="mt-8">
      <v-col cols="12" class="text-center">
        <v-icon size="120" color="grey-lighten-2">mdi-cash-multiple</v-icon>
        <h3 class="text-h6 mt-4 text-grey">No cashout requests</h3>
        <p class="text-body-2 text-grey">Agent cashout requests will appear here</p>
      </v-col>
    </v-row>

    <!-- Approve Dialog -->
    <v-dialog v-model="approveDialog" max-width="500">
      <v-card v-if="selectedCashout">
        <v-card-title>Approve Cashout</v-card-title>
        <v-card-text>
          <v-alert type="info" variant="tonal" class="mb-4">
            <div class="mb-2">
              <strong>Agent:</strong> {{ selectedCashout.agent?.full_name }}
            </div>
            <div class="mb-2">
              <strong>Amount:</strong> {{ formatCurrency(selectedCashout.amount) }}
            </div>
            <div v-if="selectedCashout.notes">
              <strong>Notes:</strong> {{ selectedCashout.notes }}
            </div>
          </v-alert>

          <p>Are you sure you want to approve this cashout request?</p>
          <p class="text-caption text-grey mt-2">
            This will deduct {{ formatCurrency(selectedCashout.amount) }} from the agent's collectible earnings.
          </p>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="approveDialog = false">Cancel</v-btn>
          <v-btn
            color="success"
            @click="handleApproveCashout"
            :loading="actionLoading"
          >
            Approve
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Reject Dialog -->
    <v-dialog v-model="rejectDialog" max-width="500">
      <v-card v-if="selectedCashout">
        <v-card-title>Reject Cashout</v-card-title>
        <v-card-text>
          <v-alert type="warning" variant="tonal" class="mb-4">
            <div class="mb-2">
              <strong>Agent:</strong> {{ selectedCashout.agent?.full_name }}
            </div>
            <div>
              <strong>Amount:</strong> {{ formatCurrency(selectedCashout.amount) }}
            </div>
          </v-alert>

          <v-textarea
            v-model="rejectionReason"
            label="Reason for rejection *"
            variant="outlined"
            rows="3"
            :rules="[rules.required]"
            placeholder="Please provide a reason for rejecting this cashout request..."
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="rejectDialog = false">Cancel</v-btn>
          <v-btn
            color="error"
            @click="handleRejectCashout"
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
import { formatCurrency, formatDate } from '~/utils/formatters'
import type { Cashout } from '~/types'

definePageMeta({
  middleware: ['auth', 'admin']
})

const cashoutsStore = useCashoutsStore()
const uiStore = useUIStore()

const tab = ref('pending')
const approveDialog = ref(false)
const rejectDialog = ref(false)
const selectedCashout = ref<Cashout | null>(null)
const rejectionReason = ref('')
const actionLoading = ref(false)

const rules = {
  required: (v: any) => !!v || 'This field is required'
}

const pendingCashouts = computed(() => {
  return cashoutsStore.cashouts.filter(c => c.status === 'pending')
})

const approvedCashouts = computed(() => {
  return cashoutsStore.cashouts.filter(c => c.status === 'approved')
})

const rejectedCashouts = computed(() => {
  return cashoutsStore.cashouts.filter(c => c.status === 'rejected')
})

const totalPendingAmount = computed(() => {
  return pendingCashouts.value.reduce((sum, c) => sum + c.amount, 0)
})

const approvedTodayCount = computed(() => {
  const today = new Date().toDateString()
  return approvedCashouts.value.filter(
    c => c.processed_at && new Date(c.processed_at).toDateString() === today
  ).length
})

const approvedTodayAmount = computed(() => {
  const today = new Date().toDateString()
  return approvedCashouts.value
    .filter(c => c.processed_at && new Date(c.processed_at).toDateString() === today)
    .reduce((sum, c) => sum + c.amount, 0)
})

const openApproveDialog = (cashout: Cashout) => {
  selectedCashout.value = cashout
  approveDialog.value = true
}

const openRejectDialog = (cashout: Cashout) => {
  selectedCashout.value = cashout
  rejectionReason.value = ''
  rejectDialog.value = true
}

const handleApproveCashout = async () => {
  if (!selectedCashout.value) return

  actionLoading.value = true
  const result = await cashoutsStore.approveCashout(selectedCashout.value.id)

  if (result.success) {
    uiStore.showSuccess('Cashout approved successfully')
    approveDialog.value = false
    selectedCashout.value = null
    await cashoutsStore.fetchCashouts()
  } else {
    uiStore.showError(result.error || 'Failed to approve cashout')
  }

  actionLoading.value = false
}

const handleRejectCashout = async () => {
  if (!selectedCashout.value || !rejectionReason.value) return

  actionLoading.value = true
  const result = await cashoutsStore.rejectCashout(
    selectedCashout.value.id,
    rejectionReason.value
  )

  if (result.success) {
    uiStore.showSuccess('Cashout rejected')
    rejectDialog.value = false
    selectedCashout.value = null
    rejectionReason.value = ''
    await cashoutsStore.fetchCashouts()
  } else {
    uiStore.showError(result.error || 'Failed to reject cashout')
  }

  actionLoading.value = false
}

// Fetch cashouts on mount
onMounted(async () => {
  try {
    await cashoutsStore.fetchCashouts()
  } catch (error: any) {
    uiStore.showError('Failed to load cashout requests')
  }
})
</script>
