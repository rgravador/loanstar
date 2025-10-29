<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6" lg="5">
        <v-card class="elevation-12">
          <v-card-title class="bg-primary text-center py-6">
            <div class="text-h4 text-white">Create Account</div>
            <div class="text-subtitle-2 text-white">Join LoanStar</div>
          </v-card-title>

          <v-card-text class="pt-6">
            <v-form ref="formRef" v-model="formValid" @submit.prevent="handleSignup">
              <v-text-field
                v-model="form.full_name"
                label="Full Name"
                prepend-inner-icon="mdi-account"
                :rules="[rules.required]"
                variant="outlined"
                class="mb-3"
              />

              <v-text-field
                v-model="form.username"
                label="Username"
                prepend-inner-icon="mdi-account-circle"
                :rules="[rules.required, rules.minLength]"
                variant="outlined"
                class="mb-3"
              />

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
                :rules="[rules.required, rules.password]"
                variant="outlined"
                class="mb-3"
                @click:append-inner="showPassword = !showPassword"
              />

              <v-text-field
                v-model="form.confirmPassword"
                label="Confirm Password"
                :type="showConfirmPassword ? 'text' : 'password'"
                prepend-inner-icon="mdi-lock-check"
                :append-inner-icon="showConfirmPassword ? 'mdi-eye' : 'mdi-eye-off'"
                :rules="[rules.required, rules.passwordMatch]"
                variant="outlined"
                class="mb-3"
                @click:append-inner="showConfirmPassword = !showConfirmPassword"
              />

              <v-select
                v-model="form.role"
                label="Role"
                :items="roleOptions"
                item-title="label"
                item-value="value"
                prepend-inner-icon="mdi-shield-account"
                :rules="[rules.required]"
                variant="outlined"
                class="mb-3"
              />

              <v-alert v-if="error" type="error" variant="tonal" class="mb-3">
                {{ error }}
              </v-alert>

              <v-alert v-if="success" type="success" variant="tonal" class="mb-3">
                Account created successfully! Redirecting to login...
              </v-alert>

              <v-btn
                type="submit"
                color="primary"
                size="large"
                block
                :loading="loading"
                :disabled="!formValid"
              >
                Sign Up
              </v-btn>
            </v-form>
          </v-card-text>

          <v-divider />

          <v-card-actions class="pa-4">
            <span class="text-caption">Already have an account?</span>
            <v-btn variant="text" size="small" to="/auth/login">
              Login
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
const showConfirmPassword = ref(false)
const loading = ref(false)
const error = ref('')
const success = ref(false)

const form = reactive({
  full_name: '',
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  role: 'agent' as 'admin' | 'agent'
})

const roleOptions = [
  { label: 'Agent', value: 'agent' },
  { label: 'Admin', value: 'admin' }
]

const rules = {
  required: (v: string) => !!v || 'Field is required',
  email: (v: string) => /.+@.+\..+/.test(v) || 'E-mail must be valid',
  minLength: (v: string) => v.length >= 3 || 'Must be at least 3 characters',
  password: (v: string) => v.length >= 6 || 'Password must be at least 6 characters',
  passwordMatch: (v: string) => v === form.password || 'Passwords must match'
}

const authStore = useAuthStore()
const router = useRouter()

const handleSignup = async () => {
  if (!formRef.value) return

  const { valid } = await formRef.value.validate()
  if (!valid) return

  loading.value = true
  error.value = ''
  success.value = false

  const result = await authStore.signup({
    email: form.email,
    password: form.password,
    username: form.username,
    full_name: form.full_name,
    role: form.role
  })

  if (result.success) {
    success.value = true
    setTimeout(() => {
      router.push('/auth/login')
    }, 2000)
  } else {
    error.value = result.error || 'Signup failed'
  }

  loading.value = false
}
</script>
