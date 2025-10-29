<template>
  <v-container>
    <v-row justify="center" class="mt-10">
      <v-col cols="12" md="8" lg="6">
        <v-card>
          <v-card-title class="text-h4 text-center py-6">
            Welcome to LoanStar
          </v-card-title>
          <v-cardSubtitle class="text-center pb-4">
            Lending Management System
          </v-cardSubtitle>
          <v-card-text>
            <v-row>
              <v-col cols="12" class="text-center">
                <p class="text-body-1 mb-4">
                  A comprehensive web application for managing lending operations with support for:
                </p>
                <v-list>
                  <v-list-item>
                    <v-list-item-title>
                      <v-icon icon="mdi-account-group" class="mr-2" />
                      Multi-role system (Admin & Agent)
                    </v-list-item-title>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-title>
                      <v-icon icon="mdi-account-multiple" class="mr-2" />
                      Borrower account management
                    </v-list-item-title>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-title>
                      <v-icon icon="mdi-file-document" class="mr-2" />
                      Loan creation & approval workflow
                    </v-list-item-title>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-title>
                      <v-icon icon="mdi-cash-multiple" class="mr-2" />
                      Payment tracking & commission management
                    </v-list-item-title>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-title>
                      <v-icon icon="mdi-bell" class="mr-2" />
                      Real-time notifications
                    </v-list-item-title>
                  </v-list-item>
                </v-list>
              </v-col>
            </v-row>
          </v-card-text>
          <v-card-actions class="justify-center pb-6">
            <v-btn
              color="primary"
              size="large"
              to="/auth/login"
              prepend-icon="mdi-login"
            >
              Login
            </v-btn>
            <v-btn
              color="secondary"
              size="large"
              to="/auth/signup"
              prepend-icon="mdi-account-plus"
              variant="outlined"
            >
              Sign Up
            </v-btn>
          </v-card-actions>
        </v-card>

        <v-card class="mt-6">
          <v-card-title>Project Status</v-card-title>
          <v-card-text>
            <v-alert type="info" variant="tonal">
              <strong>Foundation Complete:</strong> The project structure, TypeScript types, business logic composables (amortization, penalties, commissions), and authentication store have been created.
            </v-alert>
            <v-alert type="warning" variant="tonal" class="mt-3">
              <strong>Next Steps:</strong> Complete the remaining Pinia stores, tRPC endpoints, UI components, and pages. See README.md for detailed instructions.
            </v-alert>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
definePageMeta({
  layout: 'default'
})
</script>
