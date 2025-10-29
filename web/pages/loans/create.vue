<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" lg="10">
        <v-card>
          <v-card-title class="d-flex align-center">
            <v-btn icon variant="text" @click="$router.back()">
              <v-icon>mdi-arrow-left</v-icon>
            </v-btn>
            <span class="ml-2">Create New Loan</span>
          </v-card-title>

          <v-divider />

          <v-card-text class="pt-6">
            <v-form ref="formRef" v-model="formValid" @submit.prevent="handleSubmit">
              <v-row>
                <!-- Loan Details -->
                <v-col cols="12" md="6">
                  <h3 class="text-h6 mb-4">Loan Details</h3>

                  <v-select
                    v-model="form.account_id"
                    :items="accountOptions"
                    item-title="name"
                    item-value="id"
                    label="Select Account *"
                    prepend-inner-icon="mdi-account"
                    :rules="[rules.required]"
                    variant="outlined"
                    class="mb-4"
                    :loading="accountsStore.loading"
                  />

                  <v-text-field
                    v-model.number="form.principal_amount"
                    label="Principal Amount *"
                    type="number"
                    prepend-inner-icon="mdi-cash"
                    :rules="[rules.required, rules.positive]"
                    variant="outlined"
                    class="mb-4"
                  />

                  <div class="mb-4">
                    <v-slider
                      v-model="form.interest_rate"
                      :min="3"
                      :max="5"
                      :step="0.5"
                      thumb-label
                      class="mb-2"
                    >
                      <template #prepend>
                        <span class="text-body-2">3%</span>
                      </template>
                      <template #append>
                        <span class="text-body-2">5%</span>
                      </template>
                    </v-slider>
                    <v-text-field
                      v-model.number="form.interest_rate"
                      label="Interest Rate (% per month) *"
                      type="number"
                      prepend-inner-icon="mdi-percent"
                      :rules="[rules.required, rules.interestRate]"
                      variant="outlined"
                      suffix="%"
                    />
                  </div>

                  <v-select
                    v-model="form.tenure_months"
                    :items="tenureOptions"
                    label="Tenure (Months) *"
                    prepend-inner-icon="mdi-calendar"
                    :rules="[rules.required]"
                    variant="outlined"
                    class="mb-4"
                  />

                  <v-select
                    v-model="form.payment_frequency"
                    :items="frequencyOptions"
                    item-title="label"
                    item-value="value"
                    label="Payment Frequency *"
                    prepend-inner-icon="mdi-calendar-clock"
                    :rules="[rules.required]"
                    variant="outlined"
                    class="mb-4"
                  />

                  <v-text-field
                    v-model="form.start_date"
                    label="Start Date"
                    type="date"
                    prepend-inner-icon="mdi-calendar-start"
                    variant="outlined"
                    hint="Leave empty to use approval date"
                  />
                </v-col>

                <!-- Amortization Preview -->
                <v-col cols="12" md="6">
                  <h3 class="text-h6 mb-4">Amortization Preview</h3>

                  <v-card variant="outlined" class="mb-4">
                    <v-card-text>
                      <div class="d-flex justify-space-between mb-2">
                        <span class="text-grey">Total Amount:</span>
                        <span class="font-weight-bold">{{ formatCurrency(totalAmount) }}</span>
                      </div>
                      <div class="d-flex justify-space-between mb-2">
                        <span class="text-grey">Total Interest:</span>
                        <span class="font-weight-bold text-warning">{{ formatCurrency(totalInterest) }}</span>
                      </div>
                      <div class="d-flex justify-space-between">
                        <span class="text-grey">Number of Payments:</span>
                        <span class="font-weight-bold">{{ schedulePreview.length }}</span>
                      </div>
                    </v-card-text>
                  </v-card>

                  <div v-if="schedulePreview.length > 0" style="max-height: 400px; overflow-y: auto;">
                    <v-table density="compact">
                      <thead>
                        <tr>
                          <th>#</th>
                          <th>Due Date</th>
                          <th>Amount</th>
                          <th>Balance</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="item in schedulePreview" :key="item.payment_number">
                          <td>{{ item.payment_number }}</td>
                          <td>{{ formatDate(item.due_date) }}</td>
                          <td>{{ formatCurrency(item.total_due) }}</td>
                          <td>{{ formatCurrency(item.remaining_balance) }}</td>
                        </tr>
                      </tbody>
                    </v-table>
                  </div>
                </v-col>
              </v-row>

              <v-alert v-if="error" type="error" variant="tonal" class="mb-4 mt-4">
                {{ error }}
              </v-alert>

              <v-alert v-if="success" type="success" variant="tonal" class="mb-4 mt-4">
                Loan created successfully! Pending admin approval.
              </v-alert>

              <v-divider class="my-4" />

              <div class="d-flex gap-2">
                <v-btn
                  type="submit"
                  color="primary"
                  size="large"
                  :loading="loading"
                  :disabled="!formValid || !canSubmit"
                >
                  Create Loan
                </v-btn>
                <v-btn
                  variant="outlined"
                  size="large"
                  @click="$router.back()"
                  :disabled="loading"
                >
                  Cancel
                </v-btn>
              </div>
            </v-form>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { formatCurrency, formatDate } from '~/utils/formatters'
import type { PaymentFrequency } from '~/types'

definePageMeta({
  middleware: 'auth'
})

const route = useRoute()
const router = useRouter()
const accountsStore = useAccountsStore()
const loansStore = useLoansStore()
const uiStore = useUIStore()

const formRef = ref()
const formValid = ref(false)
const loading = ref(false)
const error = ref('')
const success = ref(false)

const form = reactive({
  account_id: (route.query.account_id as string) || '',
  principal_amount: 0,
  interest_rate: 3.5,
  tenure_months: 6,
  payment_frequency: 'monthly' as PaymentFrequency,
  start_date: ''
})

const tenureOptions = Array.from({ length: 11 }, (_, i) => ({
  title: `${i + 2} months`,
  value: i + 2
}))

const frequencyOptions = [
  { label: 'Bi-Monthly (Every 15 days)', value: 'bi-monthly' },
  { label: 'Monthly', value: 'monthly' },
  { label: 'Weekly', value: 'weekly' }
]

const rules = {
  required: (v: any) => !!v || 'This field is required',
  positive: (v: number) => v > 0 || 'Must be greater than 0',
  interestRate: (v: number) => (v >= 3 && v <= 5) || 'Interest rate must be between 3% and 5%'
}

const accountOptions = computed(() => accountsStore.accounts)

const schedulePreview = computed(() => {
  return loansStore.generateSchedulePreview(form)
})

const totalInterest = computed(() => {
  return schedulePreview.value.reduce((sum, item) => sum + item.interest_due, 0)
})

const totalAmount = computed(() => {
  return form.principal_amount + totalInterest.value
})

const canSubmit = computed(() => {
  return form.account_id && form.principal_amount > 0
})

const handleSubmit = async () => {
  if (!formRef.value) return

  const { valid } = await formRef.value.validate()
  if (!valid) return

  loading.value = true
  error.value = ''
  success.value = false

  const result = await loansStore.createLoan(form)

  if (result.success) {
    success.value = true
    uiStore.showSuccess('Loan created and pending approval')
    setTimeout(() => {
      router.push('/loans')
    }, 2000)
  } else {
    error.value = result.error || 'Failed to create loan'
    uiStore.showError(error.value)
  }

  loading.value = false
}

// Fetch accounts on mount
onMounted(async () => {
  try {
    await accountsStore.fetchAccounts()
  } catch (error: any) {
    uiStore.showError('Failed to load accounts')
  }
})
</script>
