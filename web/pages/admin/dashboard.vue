<template>
  <v-container fluid>
    <v-row>
      <v-col cols="12">
        <div class="mb-4">
          <h1 class="text-h4">Admin Dashboard</h1>
          <p class="text-subtitle-1 text-grey">System overview and performance metrics</p>
        </div>
      </v-col>
    </v-row>

    <!-- Loading State -->
    <v-row v-if="adminStore.loading">
      <v-col cols="12" class="text-center">
        <v-progress-circular indeterminate color="primary" size="64" />
        <p class="mt-4 text-grey">Loading dashboard...</p>
      </v-col>
    </v-row>

    <!-- Dashboard Content -->
    <template v-else-if="stats">
      <!-- System Stats -->
      <v-row>
        <v-col cols="12">
          <h3 class="text-h6 mb-3">System Statistics</h3>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card>
            <v-card-text>
              <div class="d-flex align-center mb-2">
                <v-icon color="primary" class="mr-2">mdi-account-group</v-icon>
                <span class="text-caption text-grey">Total Users</span>
              </div>
              <div class="text-h4">{{ stats.totalUsers }}</div>
              <div class="text-caption text-grey mt-1">
                {{ stats.activeAgents }} active agents
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card>
            <v-card-text>
              <div class="d-flex align-center mb-2">
                <v-icon color="info" class="mr-2">mdi-account-multiple</v-icon>
                <span class="text-caption text-grey">Total Accounts</span>
              </div>
              <div class="text-h4">{{ stats.totalAccounts }}</div>
              <div class="text-caption text-success mt-1">Borrower accounts</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card>
            <v-card-text>
              <div class="d-flex align-center mb-2">
                <v-icon color="warning" class="mr-2">mdi-file-document</v-icon>
                <span class="text-caption text-grey">Total Loans</span>
              </div>
              <div class="text-h4">{{ stats.totalLoans }}</div>
              <div class="text-caption mt-1">
                <span class="text-success">{{ stats.activeLoans }} active</span>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card>
            <v-card-text>
              <div class="d-flex align-center mb-2">
                <v-icon color="success" class="mr-2">mdi-cash-multiple</v-icon>
                <span class="text-caption text-grey">Total Disbursed</span>
              </div>
              <div class="text-h4">{{ formatCurrency(stats.totalDisbursed) }}</div>
              <div class="text-caption text-grey mt-1">Principal amount</div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Financial Overview -->
      <v-row class="mt-4">
        <v-col cols="12">
          <h3 class="text-h6 mb-3">Financial Overview</h3>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card>
            <v-card-text>
              <div class="text-caption text-grey mb-2">Total Collected</div>
              <div class="text-h5 text-success">{{ formatCurrency(stats.totalCollected) }}</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card>
            <v-card-text>
              <div class="text-caption text-grey mb-2">Outstanding Balance</div>
              <div class="text-h5 text-warning">{{ formatCurrency(stats.outstandingBalance) }}</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card>
            <v-card-text>
              <div class="text-caption text-grey mb-2">Total Penalties</div>
              <div class="text-h5 text-error">{{ formatCurrency(stats.totalPenalties) }}</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card>
            <v-card-text>
              <div class="text-caption text-grey mb-2">Total Commissions</div>
              <div class="text-h5">{{ formatCurrency(stats.totalCommissions) }}</div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Pending Actions -->
      <v-row class="mt-4">
        <v-col cols="12">
          <h3 class="text-h6 mb-3">Pending Actions</h3>
        </v-col>
        <v-col cols="12" sm="6" md="4">
          <v-card
            :to="stats.pendingLoans > 0 ? '/admin/approvals' : undefined"
            :class="stats.pendingLoans > 0 ? 'cursor-pointer' : ''"
          >
            <v-card-text>
              <div class="d-flex justify-space-between align-center">
                <div>
                  <div class="text-caption text-grey">Pending Loan Approvals</div>
                  <div class="text-h4 text-warning mt-2">{{ stats.pendingLoans }}</div>
                </div>
                <v-avatar color="warning" size="56">
                  <v-icon size="32">mdi-clock-alert</v-icon>
                </v-avatar>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="4">
          <v-card
            :to="stats.pendingCashouts > 0 ? '/admin/cashouts' : undefined"
            :class="stats.pendingCashouts > 0 ? 'cursor-pointer' : ''"
          >
            <v-card-text>
              <div class="d-flex justify-space-between align-center">
                <div>
                  <div class="text-caption text-grey">Pending Cashouts</div>
                  <div class="text-h4 text-info mt-2">{{ stats.pendingCashouts }}</div>
                </div>
                <v-avatar color="info" size="56">
                  <v-icon size="32">mdi-cash-clock</v-icon>
                </v-avatar>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="4">
          <v-card>
            <v-card-text>
              <div class="d-flex justify-space-between align-center">
                <div>
                  <div class="text-caption text-grey">Overdue Loans</div>
                  <div class="text-h4 text-error mt-2">{{ stats.overdueLoans }}</div>
                </div>
                <v-avatar color="error" size="56">
                  <v-icon size="32">mdi-alert</v-icon>
                </v-avatar>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Agent Performance -->
      <v-row class="mt-4">
        <v-col cols="12" md="8">
          <v-card>
            <v-card-title class="d-flex justify-space-between align-center">
              <span>Agent Performance</span>
              <v-btn size="small" variant="outlined" to="/admin/users">
                View All
              </v-btn>
            </v-card-title>
            <v-divider />
            <v-table v-if="agentPerformance.length > 0">
              <thead>
                <tr>
                  <th>Agent</th>
                  <th>Active Loans</th>
                  <th>Total Collected</th>
                  <th>Commissions</th>
                  <th>Performance</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="agent in agentPerformance" :key="agent.id">
                  <td>
                    <div class="d-flex align-center">
                      <v-avatar size="32" color="primary" class="mr-2">
                        <span class="text-caption">{{ getInitials(agent.full_name) }}</span>
                      </v-avatar>
                      <div>
                        <div class="font-weight-bold">{{ agent.full_name }}</div>
                        <div class="text-caption text-grey">{{ agent.username }}</div>
                      </div>
                    </div>
                  </td>
                  <td>{{ agent.activeLoans }}</td>
                  <td>{{ formatCurrency(agent.totalCollected) }}</td>
                  <td class="text-success">{{ formatCurrency(agent.totalCommissions) }}</td>
                  <td>
                    <v-chip
                      size="small"
                      :color="getPerformanceColor(agent.performance)"
                    >
                      {{ agent.performance }}
                    </v-chip>
                  </td>
                </tr>
              </tbody>
            </v-table>
            <v-card-text v-else class="text-center text-grey py-8">
              No agent performance data available
            </v-card-text>
          </v-card>
        </v-col>

        <!-- Recent Activity -->
        <v-col cols="12" md="4">
          <v-card>
            <v-card-title class="d-flex justify-space-between align-center">
              <span>Recent Activity</span>
              <v-btn size="small" variant="outlined" to="/admin/audit">
                View All
              </v-btn>
            </v-card-title>
            <v-divider />
            <v-list v-if="adminStore.recentTransactions.length > 0" class="py-0">
              <v-list-item
                v-for="transaction in adminStore.recentTransactions.slice(0, 8)"
                :key="transaction.id"
                density="compact"
              >
                <template #prepend>
                  <v-icon :color="getTransactionColor(transaction.transaction_type)" size="small">
                    {{ getTransactionIcon(transaction.transaction_type) }}
                  </v-icon>
                </template>
                <v-list-item-title class="text-caption">
                  {{ transaction.transaction_type.replace('_', ' ').toUpperCase() }}
                </v-list-item-title>
                <v-list-item-subtitle class="text-caption">
                  {{ formatDate(transaction.created_at) }}
                </v-list-item-subtitle>
              </v-list-item>
            </v-list>
            <v-card-text v-else class="text-center text-grey py-4">
              No recent activity
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Quick Actions -->
      <v-row class="mt-4">
        <v-col cols="12">
          <v-card>
            <v-card-title>Quick Actions</v-card-title>
            <v-divider />
            <v-card-text>
              <div class="d-flex flex-wrap gap-2">
                <v-btn
                  color="primary"
                  prepend-icon="mdi-check-circle"
                  to="/admin/approvals"
                >
                  Loan Approvals
                </v-btn>
                <v-btn
                  color="info"
                  prepend-icon="mdi-cash-check"
                  to="/admin/cashouts"
                >
                  Cashout Requests
                </v-btn>
                <v-btn
                  color="success"
                  prepend-icon="mdi-account-multiple"
                  to="/admin/users"
                >
                  User Management
                </v-btn>
                <v-btn
                  variant="outlined"
                  prepend-icon="mdi-clipboard-text"
                  to="/admin/audit"
                >
                  Audit Log
                </v-btn>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </template>
  </v-container>
</template>

<script setup lang="ts">
import { formatCurrency, formatDate } from '~/utils/formatters'

definePageMeta({
  middleware: ['auth', 'admin']
})

const adminStore = useAdminStore()
const uiStore = useUIStore()

const stats = computed(() => adminStore.dashboardStats)
const agentPerformance = computed(() => adminStore.agentPerformance)

const getInitials = (name: string) => {
  return name
    .split(' ')
    .map(n => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2)
}

const getPerformanceColor = (performance: string) => {
  const colors: Record<string, string> = {
    excellent: 'success',
    good: 'info',
    average: 'warning',
    poor: 'error'
  }
  return colors[performance.toLowerCase()] || 'grey'
}

const getTransactionColor = (type: string) => {
  const colors: Record<string, string> = {
    loan_created: 'info',
    loan_approved: 'success',
    loan_rejected: 'error',
    payment_recorded: 'success',
    cashout_approved: 'success',
    cashout_rejected: 'error'
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
    cashout_rejected: 'mdi-cash-remove'
  }
  return icons[type] || 'mdi-information'
}

// Fetch dashboard data on mount
onMounted(async () => {
  try {
    await Promise.all([
      adminStore.fetchDashboardStats(),
      adminStore.fetchAgentPerformance(),
      adminStore.fetchRecentTransactions()
    ])
  } catch (error: any) {
    uiStore.showError('Failed to load dashboard data')
  }
})
</script>

<style scoped>
.cursor-pointer {
  cursor: pointer;
}
</style>
