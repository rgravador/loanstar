<template>
  <v-container fluid>
    <v-row>
      <v-col cols="12">
        <div class="mb-4">
          <h1 class="text-h4">Audit Log</h1>
          <p class="text-subtitle-1 text-grey">System transaction history and activity log</p>
        </div>
      </v-col>
    </v-row>

    <!-- Filters -->
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-text>
            <v-row>
              <v-col cols="12" md="4">
                <v-select
                  v-model="filterType"
                  :items="transactionTypes"
                  label="Transaction Type"
                  variant="outlined"
                  density="compact"
                  clearable
                  prepend-inner-icon="mdi-filter"
                />
              </v-col>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model="filterUser"
                  label="Search by user"
                  variant="outlined"
                  density="compact"
                  clearable
                  prepend-inner-icon="mdi-magnify"
                />
              </v-col>
              <v-col cols="12" md="4">
                <v-select
                  v-model="dateRange"
                  :items="dateRangeOptions"
                  label="Date Range"
                  variant="outlined"
                  density="compact"
                  prepend-inner-icon="mdi-calendar"
                />
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Stats -->
    <v-row class="mt-4">
      <v-col cols="12" sm="6" md="3">
        <v-card>
          <v-card-text>
            <div class="text-caption text-grey">Total Transactions</div>
            <div class="text-h5">{{ filteredTransactions.length }}</div>
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" sm="6" md="3">
        <v-card>
          <v-card-text>
            <div class="text-caption text-grey">Today's Transactions</div>
            <div class="text-h5">{{ todayTransactions.length }}</div>
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" sm="6" md="3">
        <v-card>
          <v-card-text>
            <div class="text-caption text-grey">Loan Transactions</div>
            <div class="text-h5 text-info">{{ loanTransactions.length }}</div>
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" sm="6" md="3">
        <v-card>
          <v-card-text>
            <div class="text-caption text-grey">Payment Transactions</div>
            <div class="text-h5 text-success">{{ paymentTransactions.length }}</div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Loading State -->
    <v-row v-if="adminStore.loading" class="mt-4">
      <v-col cols="12" class="text-center">
        <v-progress-circular indeterminate color="primary" size="64" />
        <p class="mt-4 text-grey">Loading audit log...</p>
      </v-col>
    </v-row>

    <!-- Transactions List -->
    <v-row v-else-if="filteredTransactions.length > 0" class="mt-4">
      <v-col cols="12">
        <v-card>
          <v-card-title class="d-flex justify-space-between align-center">
            <span>Transaction History ({{ filteredTransactions.length }})</span>
            <v-btn
              size="small"
              variant="outlined"
              prepend-icon="mdi-download"
              @click="exportToCSV"
            >
              Export
            </v-btn>
          </v-card-title>
          <v-divider />

          <!-- Desktop Table -->
          <div class="d-none d-md-block" style="overflow-x: auto;">
            <v-table>
              <thead>
                <tr>
                  <th>Date & Time</th>
                  <th>Type</th>
                  <th>User</th>
                  <th>Details</th>
                  <th>Amount</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="transaction in paginatedTransactions" :key="transaction.id">
                  <td>
                    <div class="text-body-2">{{ formatDate(transaction.created_at) }}</div>
                    <div class="text-caption text-grey">{{ formatTime(transaction.created_at) }}</div>
                  </td>
                  <td>
                    <v-chip
                      size="small"
                      :color="getTransactionColor(transaction.transaction_type)"
                      variant="tonal"
                    >
                      <v-icon size="small" class="mr-1">
                        {{ getTransactionIcon(transaction.transaction_type) }}
                      </v-icon>
                      {{ formatTransactionType(transaction.transaction_type) }}
                    </v-chip>
                  </td>
                  <td>
                    <div v-if="transaction.user">
                      <div class="font-weight-bold text-body-2">{{ transaction.user.full_name }}</div>
                      <div class="text-caption text-grey">{{ transaction.user.username }}</div>
                    </div>
                    <span v-else class="text-grey">-</span>
                  </td>
                  <td>
                    <div class="text-caption">{{ transaction.details || '-' }}</div>
                  </td>
                  <td>
                    <span v-if="transaction.amount" class="font-weight-bold">
                      {{ formatCurrency(transaction.amount) }}
                    </span>
                    <span v-else class="text-grey">-</span>
                  </td>
                  <td>
                    <v-icon
                      :color="transaction.success ? 'success' : 'error'"
                      size="small"
                    >
                      {{ transaction.success ? 'mdi-check-circle' : 'mdi-alert-circle' }}
                    </v-icon>
                  </td>
                </tr>
              </tbody>
            </v-table>
          </div>

          <!-- Mobile List -->
          <div class="d-md-none">
            <v-list>
              <v-list-item
                v-for="transaction in paginatedTransactions"
                :key="transaction.id"
                class="mb-2"
              >
                <template #prepend>
                  <v-avatar :color="getTransactionColor(transaction.transaction_type)">
                    <v-icon>{{ getTransactionIcon(transaction.transaction_type) }}</v-icon>
                  </v-avatar>
                </template>
                <v-list-item-title>
                  {{ formatTransactionType(transaction.transaction_type) }}
                  <v-chip
                    v-if="transaction.amount"
                    size="x-small"
                    class="ml-2"
                  >
                    {{ formatCurrency(transaction.amount) }}
                  </v-chip>
                </v-list-item-title>
                <v-list-item-subtitle>
                  {{ transaction.user?.full_name || 'System' }} •
                  {{ formatDate(transaction.created_at) }}
                </v-list-item-subtitle>
              </v-list-item>
            </v-list>
          </div>

          <!-- Pagination -->
          <v-divider />
          <v-card-text>
            <div class="d-flex justify-space-between align-center">
              <div class="text-caption text-grey">
                Showing {{ paginationStart + 1 }}-{{ paginationEnd }} of {{ filteredTransactions.length }}
              </div>
              <div class="d-flex gap-2">
                <v-btn
                  size="small"
                  variant="outlined"
                  :disabled="page === 1"
                  @click="page--"
                >
                  Previous
                </v-btn>
                <v-btn
                  size="small"
                  variant="outlined"
                  :disabled="page >= totalPages"
                  @click="page++"
                >
                  Next
                </v-btn>
              </div>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Empty State -->
    <v-row v-else class="mt-8">
      <v-col cols="12" class="text-center">
        <v-icon size="120" color="grey-lighten-2">mdi-clipboard-text</v-icon>
        <h3 class="text-h6 mt-4 text-grey">No transactions found</h3>
        <p class="text-body-2 text-grey">Try adjusting your filters</p>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { formatCurrency, formatDate } from '~/utils/formatters'

definePageMeta({
  middleware: ['auth', 'admin']
})

const adminStore = useAdminStore()
const uiStore = useUIStore()

const filterType = ref('')
const filterUser = ref('')
const dateRange = ref('all')
const page = ref(1)
const itemsPerPage = 50

const transactionTypes = [
  { title: 'All Types', value: '' },
  { title: 'Loan Created', value: 'loan_created' },
  { title: 'Loan Approved', value: 'loan_approved' },
  { title: 'Loan Rejected', value: 'loan_rejected' },
  { title: 'Payment Recorded', value: 'payment_recorded' },
  { title: 'Cashout Approved', value: 'cashout_approved' },
  { title: 'Cashout Rejected', value: 'cashout_rejected' },
  { title: 'Account Created', value: 'account_created' },
  { title: 'Account Updated', value: 'account_updated' }
]

const dateRangeOptions = [
  { title: 'All Time', value: 'all' },
  { title: 'Today', value: 'today' },
  { title: 'Last 7 Days', value: '7days' },
  { title: 'Last 30 Days', value: '30days' },
  { title: 'This Month', value: 'month' }
]

const filteredTransactions = computed(() => {
  let transactions = [...adminStore.recentTransactions]

  // Filter by type
  if (filterType.value) {
    transactions = transactions.filter(t => t.transaction_type === filterType.value)
  }

  // Filter by user
  if (filterUser.value) {
    const searchLower = filterUser.value.toLowerCase()
    transactions = transactions.filter(t =>
      t.user?.full_name.toLowerCase().includes(searchLower) ||
      t.user?.username.toLowerCase().includes(searchLower)
    )
  }

  // Filter by date range
  const now = new Date()
  transactions = transactions.filter(t => {
    const transactionDate = new Date(t.created_at)

    switch (dateRange.value) {
      case 'today':
        return transactionDate.toDateString() === now.toDateString()
      case '7days':
        const sevenDaysAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
        return transactionDate >= sevenDaysAgo
      case '30days':
        const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
        return transactionDate >= thirtyDaysAgo
      case 'month':
        return transactionDate.getMonth() === now.getMonth() &&
               transactionDate.getFullYear() === now.getFullYear()
      default:
        return true
    }
  })

  return transactions
})

const todayTransactions = computed(() => {
  const today = new Date().toDateString()
  return adminStore.recentTransactions.filter(
    t => new Date(t.created_at).toDateString() === today
  )
})

const loanTransactions = computed(() => {
  return adminStore.recentTransactions.filter(t =>
    t.transaction_type.includes('loan')
  )
})

const paymentTransactions = computed(() => {
  return adminStore.recentTransactions.filter(t =>
    t.transaction_type === 'payment_recorded'
  )
})

const totalPages = computed(() => {
  return Math.ceil(filteredTransactions.value.length / itemsPerPage)
})

const paginationStart = computed(() => {
  return (page.value - 1) * itemsPerPage
})

const paginationEnd = computed(() => {
  return Math.min(page.value * itemsPerPage, filteredTransactions.value.length)
})

const paginatedTransactions = computed(() => {
  return filteredTransactions.value.slice(paginationStart.value, paginationEnd.value)
})

const formatTime = (date: string | Date) => {
  return new Date(date).toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatTransactionType = (type: string) => {
  return type.split('_').map(word =>
    word.charAt(0).toUpperCase() + word.slice(1)
  ).join(' ')
}

const getTransactionColor = (type: string) => {
  const colors: Record<string, string> = {
    loan_created: 'info',
    loan_approved: 'success',
    loan_rejected: 'error',
    payment_recorded: 'success',
    cashout_approved: 'success',
    cashout_rejected: 'error',
    account_created: 'info',
    account_updated: 'warning'
  }
  return colors[type] || 'grey'
}

const getTransactionIcon = (type: string) => {
  const icons: Record<string, string> = {
    loan_created: 'mdi-file-document-plus',
    loan_approved: 'mdi-check-circle',
    loan_rejected: 'mdi-close-circle',
    payment_recorded: 'mdi-cash-plus',
    cashout_approved: 'mdi-cash-check',
    cashout_rejected: 'mdi-cash-remove',
    account_created: 'mdi-account-plus',
    account_updated: 'mdi-account-edit'
  }
  return icons[type] || 'mdi-information'
}

const exportToCSV = () => {
  const headers = ['Date', 'Time', 'Type', 'User', 'Details', 'Amount', 'Status']
  const rows = filteredTransactions.value.map(t => [
    formatDate(t.created_at),
    formatTime(t.created_at),
    formatTransactionType(t.transaction_type),
    t.user?.full_name || 'System',
    t.details || '-',
    t.amount ? formatCurrency(t.amount) : '-',
    t.success ? 'Success' : 'Failed'
  ])

  const csv = [
    headers.join(','),
    ...rows.map(row => row.map(cell => `"${cell}"`).join(','))
  ].join('\n')

  const blob = new Blob([csv], { type: 'text/csv' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `audit-log-${new Date().toISOString().split('T')[0]}.csv`
  link.click()
  window.URL.revokeObjectURL(url)

  uiStore.showSuccess('Audit log exported successfully')
}

// Reset pagination when filters change
watch([filterType, filterUser, dateRange], () => {
  page.value = 1
})

// Fetch audit log on mount
onMounted(async () => {
  try {
    await adminStore.fetchRecentTransactions()
  } catch (error: any) {
    uiStore.showError('Failed to load audit log')
  }
})
</script>
