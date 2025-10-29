<template>
  <v-container fluid>
    <!-- Loading State -->
    <v-row v-if="accountsStore.loading" class="mt-8">
      <v-col cols="12" class="text-center">
        <v-progress-circular indeterminate color="primary" size="64" />
        <p class="mt-4 text-grey">Loading account details...</p>
      </v-col>
    </v-row>

    <!-- Account Details -->
    <template v-else-if="account">
      <v-row>
        <v-col cols="12">
          <div class="d-flex align-center mb-4">
            <v-btn icon variant="text" @click="$router.back()">
              <v-icon>mdi-arrow-left</v-icon>
            </v-btn>
            <div class="ml-4">
              <h1 class="text-h4">{{ account.name }}</h1>
              <v-chip :color="getStatusColor(account.status)" size="small" class="mt-2">
                {{ account.status }}
              </v-chip>
            </div>
          </div>
        </v-col>
      </v-row>

      <v-row>
        <!-- Account Information -->
        <v-col cols="12" md="4">
          <v-card>
            <v-card-title>Account Information</v-card-title>
            <v-divider />
            <v-card-text>
              <div class="mb-4">
                <div class="text-caption text-grey mb-1">Contact Information</div>
                <div class="d-flex align-center">
                  <v-icon size="small" class="mr-2">mdi-phone</v-icon>
                  <span>{{ account.contact_info }}</span>
                </div>
              </div>

              <div class="mb-4">
                <div class="text-caption text-grey mb-1">Address</div>
                <div class="d-flex align-start">
                  <v-icon size="small" class="mr-2 mt-1">mdi-map-marker</v-icon>
                  <span>{{ account.address }}</span>
                </div>
              </div>

              <div class="mb-4">
                <div class="text-caption text-grey mb-1">Assigned Agent</div>
                <div class="d-flex align-center">
                  <v-icon size="small" class="mr-2">mdi-account</v-icon>
                  <span>{{ account.assigned_agent?.full_name || 'N/A' }}</span>
                </div>
              </div>

              <div class="mb-4">
                <div class="text-caption text-grey mb-1">Created</div>
                <div class="d-flex align-center">
                  <v-icon size="small" class="mr-2">mdi-calendar</v-icon>
                  <span>{{ formatDate(account.created_at) }}</span>
                </div>
              </div>

              <v-btn
                v-if="account.id_proof_url"
                block
                variant="outlined"
                prepend-icon="mdi-file-image"
                :href="account.id_proof_url"
                target="_blank"
              >
                View ID Proof
              </v-btn>
            </v-card-text>
            <v-card-actions>
              <v-btn
                block
                color="primary"
                prepend-icon="mdi-file-document-plus"
                :to="`/loans/create?account_id=${account.id}`"
              >
                Create Loan
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>

        <!-- Loans List -->
        <v-col cols="12" md="8">
          <v-card>
            <v-card-title class="d-flex justify-space-between align-center">
              <span>Loans ({{ account.loans?.length || 0 }})</span>
              <v-btn
                size="small"
                color="primary"
                prepend-icon="mdi-plus"
                :to="`/loans/create?account_id=${account.id}`"
              >
                New Loan
              </v-btn>
            </v-card-title>
            <v-divider />

            <v-list v-if="account.loans && account.loans.length > 0">
              <v-list-item
                v-for="loan in account.loans"
                :key="loan.id"
                :to="`/loans/${loan.id}`"
              >
                <template #prepend>
                  <v-avatar :color="getLoanStatusColor(loan.status)">
                    <v-icon>mdi-file-document</v-icon>
                  </v-avatar>
                </template>

                <v-list-item-title>
                  {{ formatCurrency(loan.principal_amount) }}
                </v-list-item-title>
                <v-list-item-subtitle>
                  {{ loan.interest_rate }}% • {{ loan.payment_frequency }} • {{ loan.tenure_months }} months
                </v-list-item-subtitle>

                <template #append>
                  <div class="text-right">
                    <v-chip :color="getLoanStatusColor(loan.status)" size="small">
                      {{ loan.status }}
                    </v-chip>
                    <div class="text-caption text-grey mt-1">
                      Balance: {{ formatCurrency(loan.current_balance) }}
                    </div>
                  </div>
                </template>
              </v-list-item>
            </v-list>

            <v-card-text v-else class="text-center text-grey py-8">
              <v-icon size="64" color="grey-lighten-2">mdi-file-document-outline</v-icon>
              <p class="mt-4">No loans yet</p>
              <v-btn
                color="primary"
                prepend-icon="mdi-plus"
                :to="`/loans/create?account_id=${account.id}`"
                class="mt-2"
              >
                Create First Loan
              </v-btn>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </template>

    <!-- Error State -->
    <v-row v-else class="mt-8">
      <v-col cols="12" class="text-center">
        <v-icon size="120" color="error">mdi-alert-circle-outline</v-icon>
        <h3 class="text-h6 mt-4">Account not found</h3>
        <v-btn color="primary" class="mt-4" @click="$router.back()">
          Go Back
        </v-btn>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { formatCurrency, formatDate } from '~/utils/formatters'

definePageMeta({
  middleware: 'auth'
})

const route = useRoute()
const accountsStore = useAccountsStore()
const uiStore = useUIStore()

const account = computed(() => accountsStore.selectedAccount)

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    active: 'success',
    inactive: 'grey',
    suspended: 'error'
  }
  return colors[status] || 'grey'
}

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

// Fetch account details on mount
onMounted(async () => {
  try {
    await accountsStore.fetchAccountById(route.params.id as string)
  } catch (error: any) {
    uiStore.showError('Failed to load account details')
  }
})
</script>
