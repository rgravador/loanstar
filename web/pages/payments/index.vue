<template>
  <v-container fluid>
    <v-row>
      <v-col cols="12">
        <div class="d-flex justify-space-between align-center mb-4">
          <div>
            <h1 class="text-h4">Payments</h1>
            <p class="text-subtitle-1 text-grey">Payment history and records</p>
          </div>
          <v-btn color="primary" prepend-icon="mdi-cash-plus" to="/payments/create">
            Record Payment
          </v-btn>
        </div>
      </v-col>
    </v-row>

    <!-- Stats -->
    <v-row>
      <v-col cols="12" sm="6" md="3">
        <v-card>
          <v-card-text>
            <div class="text-caption text-grey">Total Payments</div>
            <div class="text-h5">{{ paymentsStore.payments.length }}</div>
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" sm="6" md="3">
        <v-card>
          <v-card-text>
            <div class="text-caption text-grey">Today's Collections</div>
            <div class="text-h5">{{ formatCurrency(paymentsStore.totalPaymentsToday) }}</div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Loading State -->
    <v-row v-if="paymentsStore.loading" class="mt-4">
      <v-col cols="12" class="text-center">
        <v-progress-circular indeterminate color="primary" size="64" />
        <p class="mt-4 text-grey">Loading payments...</p>
      </v-col>
    </v-row>

    <!-- Payments List -->
    <v-row v-else-if="paymentsStore.payments.length > 0" class="mt-4">
      <v-col cols="12">
        <v-card>
          <v-card-title>Payment History</v-card-title>
          <v-divider />
          <v-table>
            <thead>
              <tr>
                <th>Date</th>
                <th>Loan</th>
                <th>Account</th>
                <th>Amount</th>
                <th>Applied To</th>
                <th>Received By</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="payment in paymentsStore.payments" :key="payment.id">
                <td>{{ formatDate(payment.payment_date) }}</td>
                <td>
                  <NuxtLink :to="`/loans/${payment.loan_id}`" class="text-primary">
                    {{ formatCurrency(payment.loan?.principal_amount || 0) }}
                  </NuxtLink>
                </td>
                <td>{{ payment.loan?.account?.name || 'N/A' }}</td>
                <td class="font-weight-bold">{{ formatCurrency(payment.amount) }}</td>
                <td>
                  <div class="text-caption">
                    <div v-if="payment.applied_to_penalty > 0">
                      Penalty: {{ formatCurrency(payment.applied_to_penalty) }}
                    </div>
                    <div>Interest: {{ formatCurrency(payment.applied_to_interest) }}</div>
                    <div>Principal: {{ formatCurrency(payment.applied_to_principal) }}</div>
                  </div>
                </td>
                <td>{{ payment.receiver?.full_name || 'N/A' }}</td>
                <td>
                  <v-btn
                    size="small"
                    variant="text"
                    icon
                    :to="`/loans/${payment.loan_id}`"
                  >
                    <v-icon>mdi-eye</v-icon>
                  </v-btn>
                </td>
              </tr>
            </tbody>
          </v-table>
        </v-card>
      </v-col>
    </v-row>

    <!-- Empty State -->
    <v-row v-else class="mt-8">
      <v-col cols="12" class="text-center">
        <v-icon size="120" color="grey-lighten-2">mdi-cash-multiple</v-icon>
        <h3 class="text-h6 mt-4 text-grey">No payments recorded</h3>
        <p class="text-body-2 text-grey mb-4">
          Record your first payment to get started
        </p>
        <v-btn color="primary" prepend-icon="mdi-cash-plus" to="/payments/create">
          Record Payment
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

const paymentsStore = usePaymentsStore()
const uiStore = useUIStore()

// Fetch payments on mount
onMounted(async () => {
  try {
    await paymentsStore.fetchPayments()
  } catch (error: any) {
    uiStore.showError('Failed to load payments')
  }
})
</script>
