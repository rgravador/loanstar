<template>
  <v-container fluid>
    <v-row>
      <v-col cols="12">
        <div class="d-flex justify-space-between align-center mb-4">
          <div>
            <h1 class="text-h4">Accounts</h1>
            <p class="text-subtitle-1 text-grey">Manage borrower accounts</p>
          </div>
          <v-btn color="primary" prepend-icon="mdi-plus" to="/accounts/create">
            Add Account
          </v-btn>
        </div>
      </v-col>
    </v-row>

    <!-- Filters -->
    <v-row>
      <v-col cols="12" md="6">
        <v-text-field
          v-model="accountsStore.filters.search"
          prepend-inner-icon="mdi-magnify"
          label="Search accounts"
          variant="outlined"
          density="compact"
          clearable
          hide-details
        />
      </v-col>
      <v-col cols="12" md="3">
        <v-select
          v-model="accountsStore.filters.status"
          :items="statusOptions"
          label="Status"
          variant="outlined"
          density="compact"
          clearable
          hide-details
        />
      </v-col>
      <v-col cols="12" md="3">
        <v-btn block variant="outlined" @click="accountsStore.clearFilters()">
          Clear Filters
        </v-btn>
      </v-col>
    </v-row>

    <!-- Loading State -->
    <v-row v-if="accountsStore.loading" class="mt-4">
      <v-col cols="12" class="text-center">
        <v-progress-circular indeterminate color="primary" size="64" />
        <p class="mt-4 text-grey">Loading accounts...</p>
      </v-col>
    </v-row>

    <!-- Accounts List -->
    <v-row v-else-if="accountsStore.filteredAccounts.length > 0" class="mt-4">
      <v-col
        v-for="account in accountsStore.filteredAccounts"
        :key="account.id"
        cols="12"
        sm="6"
        md="4"
      >
        <v-card class="card-hover" :to="`/accounts/${account.id}`">
          <v-card-title class="d-flex justify-space-between align-center">
            <span class="text-truncate">{{ account.name }}</span>
            <v-chip :color="getStatusColor(account.status)" size="small">
              {{ account.status }}
            </v-chip>
          </v-card-title>

          <v-card-text>
            <div class="mb-2">
              <v-icon size="small" class="mr-2">mdi-phone</v-icon>
              <span class="text-body-2">{{ account.contact_info }}</span>
            </div>
            <div class="mb-2">
              <v-icon size="small" class="mr-2">mdi-map-marker</v-icon>
              <span class="text-body-2 text-truncate">{{ account.address }}</span>
            </div>
            <div>
              <v-icon size="small" class="mr-2">mdi-file-document</v-icon>
              <span class="text-body-2">{{ account.loans?.length || 0 }} loans</span>
            </div>
          </v-card-text>

          <v-divider />

          <v-card-actions>
            <v-btn
              size="small"
              variant="text"
              prepend-icon="mdi-eye"
              :to="`/accounts/${account.id}`"
            >
              View Details
            </v-btn>
            <v-spacer />
            <v-btn
              size="small"
              variant="text"
              color="primary"
              prepend-icon="mdi-file-document-plus"
              :to="`/loans/create?account_id=${account.id}`"
            >
              New Loan
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    <!-- Empty State -->
    <v-row v-else class="mt-8">
      <v-col cols="12" class="text-center">
        <v-icon size="120" color="grey-lighten-2">mdi-account-multiple-outline</v-icon>
        <h3 class="text-h6 mt-4 text-grey">No accounts found</h3>
        <p class="text-body-2 text-grey mb-4">
          {{ accountsStore.filters.search ? 'Try adjusting your filters' : 'Create your first borrower account to get started' }}
        </p>
        <v-btn color="primary" prepend-icon="mdi-plus" to="/accounts/create">
          Add Account
        </v-btn>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth'
})

const accountsStore = useAccountsStore()
const uiStore = useUIStore()

const statusOptions = [
  { title: 'Active', value: 'active' },
  { title: 'Inactive', value: 'inactive' },
  { title: 'Suspended', value: 'suspended' }
]

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    active: 'success',
    inactive: 'grey',
    suspended: 'error'
  }
  return colors[status] || 'grey'
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

<style scoped>
.card-hover {
  transition: transform 0.2s ease;
}

.card-hover:hover {
  transform: translateY(-4px);
}
</style>
