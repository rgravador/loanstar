<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" md="8" lg="6">
        <v-card>
          <v-card-title class="d-flex align-center">
            <v-btn icon variant="text" @click="$router.back()">
              <v-icon>mdi-arrow-left</v-icon>
            </v-btn>
            <span class="ml-2">Create New Account</span>
          </v-card-title>

          <v-divider />

          <v-card-text class="pt-6">
            <v-form ref="formRef" v-model="formValid" @submit.prevent="handleSubmit">
              <v-text-field
                v-model="form.name"
                label="Full Name *"
                prepend-inner-icon="mdi-account"
                :rules="[rules.required]"
                variant="outlined"
                class="mb-4"
              />

              <v-text-field
                v-model="form.contact_info"
                label="Contact Information *"
                prepend-inner-icon="mdi-phone"
                :rules="[rules.required]"
                variant="outlined"
                hint="Phone number or email"
                class="mb-4"
              />

              <v-textarea
                v-model="form.address"
                label="Address *"
                prepend-inner-icon="mdi-map-marker"
                :rules="[rules.required]"
                variant="outlined"
                rows="3"
                class="mb-4"
              />

              <v-file-input
                v-model="form.id_proof_file"
                label="ID Proof (Optional)"
                prepend-inner-icon="mdi-file-image"
                variant="outlined"
                accept="image/*"
                hint="Upload a government-issued ID"
                show-size
                class="mb-4"
              />

              <v-alert v-if="error" type="error" variant="tonal" class="mb-4">
                {{ error }}
              </v-alert>

              <v-alert v-if="success" type="success" variant="tonal" class="mb-4">
                Account created successfully! Redirecting...
              </v-alert>

              <div class="d-flex gap-2">
                <v-btn
                  type="submit"
                  color="primary"
                  size="large"
                  :loading="loading"
                  :disabled="!formValid"
                >
                  Create Account
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
definePageMeta({
  middleware: 'auth'
})

const router = useRouter()
const accountsStore = useAccountsStore()
const uiStore = useUIStore()

const formRef = ref()
const formValid = ref(false)
const loading = ref(false)
const error = ref('')
const success = ref(false)

const form = reactive({
  name: '',
  contact_info: '',
  address: '',
  id_proof_file: null as File | null
})

const rules = {
  required: (v: string) => !!v || 'This field is required'
}

const handleSubmit = async () => {
  if (!formRef.value) return

  const { valid } = await formRef.value.validate()
  if (!valid) return

  loading.value = true
  error.value = ''
  success.value = false

  const result = await accountsStore.createAccount({
    name: form.name,
    contact_info: form.contact_info,
    address: form.address,
    id_proof_file: form.id_proof_file
  })

  if (result.success) {
    success.value = true
    uiStore.showSuccess('Account created successfully')
    setTimeout(() => {
      router.push('/accounts')
    }, 1500)
  } else {
    error.value = result.error || 'Failed to create account'
    uiStore.showError(error.value)
  }

  loading.value = false
}
</script>
