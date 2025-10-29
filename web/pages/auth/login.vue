<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6" lg="4">
        <v-card class="elevation-12">
          <v-card-title class="bg-primary text-center py-6">
            <div class="text-h4 text-white">LoanStar</div>
            <div class="text-subtitle-2 text-white">Lending Management System</div>
          </v-card-title>

          <v-card-text class="pt-6">
            <v-form ref="formRef" v-model="formValid" @submit.prevent="handleLogin">
              <v-text-field
                v-model="form.email"
                label="Email"
                type="email"
                prepend-inner-icon="mdi-email"
                :rules="[rules.required, rules.email]"
                variant="outlined"
                class="mb-3"
              />

              <v-text-field
                v-model="form.password"
                label="Password"
                :type="showPassword ? 'text' : 'password'"
                prepend-inner-icon="mdi-lock"
                :append-inner-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                :rules="[rules.required]"
                variant="outlined"
                @click:append-inner="showPassword = !showPassword"
              />

              <v-alert v-if="error" type="error" variant="tonal" class="mb-3">
                {{ error }}
              </v-alert>

              <v-btn
                type="submit"
                color="primary"
                size="large"
                block
                :loading="loading"
                :disabled="!formValid"
              >
                Login
              </v-btn>
            </v-form>
          </v-card-text>

          <v-divider />

          <v-card-actions class="pa-4">
            <v-btn variant="text" size="small" to="/auth/reset-password">
              Forgot Password?
            </v-btn>
            <v-spacer />
            <v-btn variant="text" size="small" to="/auth/signup">
              Sign Up
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
definePageMeta({
  layout: 'auth',
  middleware: 'guest'
})

const formRef = ref()
const formValid = ref(false)
const showPassword = ref(false)
const loading = ref(false)
const error = ref('')

const form = reactive({
  email: '',
  password: ''
})

const rules = {
  required: (v: string) => !!v || 'Field is required',
  email: (v: string) => /.+@.+\..+/.test(v) || 'E-mail must be valid'
}

const authStore = useAuthStore()
const router = useRouter()

const handleLogin = async () => {
  if (!formRef.value) return

  const { valid } = await formRef.value.validate()
  if (!valid) return

  loading.value = true
  error.value = ''

  const result = await authStore.login(form.email, form.password)

  if (result.success) {
    // Redirect based on role
    if (authStore.isAdmin) {
      router.push('/admin/dashboard')
    } else {
      router.push('/dashboard')
    }
  } else {
    error.value = result.error || 'Login failed'
  }

  loading.value = false
}
</script>
