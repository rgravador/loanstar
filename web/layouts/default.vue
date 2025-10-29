<template>
  <v-app>
    <!-- App Bar -->
    <v-app-bar color="primary" density="comfortable" elevate-on-scroll>
      <v-app-barNavIcon @click="uiStore.toggleSidebar" class="hide-on-desktop" />

      <v-app-barTitle>
        <span class="text-h6">LoanStar</span>
      </v-app-barTitle>

      <v-spacer />

      <!-- Notifications -->
      <v-menu>
        <template #activator="{ props }">
          <v-btn icon v-bind="props">
            <v-badge
              v-if="notificationsStore.unreadCount > 0"
              :content="notificationsStore.unreadCount"
              color="error"
            >
              <v-icon>mdi-bell</v-icon>
            </v-badge>
            <v-icon v-else>mdi-bell</v-icon>
          </v-btn>
        </template>

        <v-card min-width="300" max-width="400">
          <v-card-title class="d-flex justify-space-between align-center">
            <span>Notifications</span>
            <v-btn
              v-if="notificationsStore.unreadCount > 0"
              size="small"
              variant="text"
              @click="notificationsStore.markAllAsRead()"
            >
              Mark all read
            </v-btn>
          </v-card-title>

          <v-divider />

          <v-list v-if="notificationsStore.notifications.length > 0" max-height="400">
            <v-list-item
              v-for="notification in notificationsStore.recentNotifications"
              :key="notification.id"
              :class="{ 'bg-grey-lighten-4': !notification.is_read }"
              @click="notificationsStore.markAsRead(notification.id)"
            >
              <v-list-item-title>{{ notification.message }}</v-list-item-title>
              <v-list-item-subtitle>{{ formatRelativeTime(notification.timestamp) }}</v-list-item-subtitle>
            </v-list-item>
          </v-list>

          <v-card-text v-else class="text-center">
            No notifications
          </v-card-text>

          <v-divider />

          <v-card-actions>
            <v-btn block variant="text" to="/notifications">View All</v-btn>
          </v-card-actions>
        </v-card>
      </v-menu>

      <!-- Theme Toggle -->
      <v-btn icon @click="uiStore.toggleTheme()">
        <v-icon>{{ uiStore.isDark ? 'mdi-weather-sunny' : 'mdi-weather-night' }}</v-icon>
      </v-btn>

      <!-- User Menu -->
      <v-menu>
        <template #activator="{ props }">
          <v-btn icon v-bind="props">
            <v-avatar size="32" color="secondary">
              <v-icon v-if="!authStore.user?.avatar_url">mdi-account</v-icon>
              <v-img v-else :src="authStore.user.avatar_url" />
            </v-avatar>
          </v-btn>
        </template>

        <v-card min-width="200">
          <v-card-text class="pb-0">
            <div class="text-h6">{{ authStore.user?.full_name }}</div>
            <div class="text-caption text-grey">{{ authStore.user?.email }}</div>
            <v-chip size="small" :color="authStore.isAdmin ? 'error' : 'primary'" class="mt-2">
              {{ authStore.user?.role }}
            </v-chip>
          </v-card-text>

          <v-divider class="my-2" />

          <v-list density="compact">
            <v-list-item to="/profile" prepend-icon="mdi-account">
              <v-list-item-title>Profile</v-list-item-title>
            </v-list-item>
            <v-list-item @click="authStore.logout()" prepend-icon="mdi-logout">
              <v-list-item-title>Logout</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-card>
      </v-menu>
    </v-app-bar>

    <!-- Navigation Drawer -->
    <v-navigation-drawer
      v-model="uiStore.sidebarOpen"
      temporary
      class="hide-on-desktop"
    >
      <v-list>
        <v-list-item
          prepend-icon="mdi-view-dashboard"
          title="Dashboard"
          to="/dashboard"
        />
        <v-list-item
          prepend-icon="mdi-account-multiple"
          title="Accounts"
          to="/accounts"
        />
        <v-list-item
          prepend-icon="mdi-file-document"
          title="Loans"
          to="/loans"
        />
        <v-list-item
          prepend-icon="mdi-cash"
          title="Payments"
          to="/payments"
        />

        <template v-if="authStore.isAgent">
          <v-list-item
            prepend-icon="mdi-currency-usd"
            title="Earnings"
            to="/earnings"
          />
          <v-list-item
            prepend-icon="mdi-cash-check"
            title="Cashouts"
            to="/cashouts"
          />
        </template>

        <template v-if="authStore.isAdmin">
          <v-divider class="my-2" />
          <v-listSubheader>Admin</v-listSubheader>
          <v-list-item
            prepend-icon="mdi-shield-account"
            title="Admin Dashboard"
            to="/admin/dashboard"
          />
          <v-list-item
            prepend-icon="mdi-check-decagram"
            title="Approvals"
            to="/admin/approvals"
          />
          <v-list-item
            prepend-icon="mdi-cash-multiple"
            title="Cashouts"
            to="/admin/cashouts"
          />
          <v-list-item
            prepend-icon="mdi-account-group"
            title="Users"
            to="/admin/users"
          />
        </template>
      </v-list>
    </v-navigation-drawer>

    <!-- Bottom Navigation (Mobile) -->
    <v-bottom-navigation
      v-model="bottomNav"
      grow
      class="hide-on-desktop"
    >
      <v-btn value="dashboard" to="/dashboard">
        <v-icon>mdi-view-dashboard</v-icon>
        <span>Dashboard</span>
      </v-btn>

      <v-btn value="accounts" to="/accounts">
        <v-icon>mdi-account-multiple</v-icon>
        <span>Accounts</span>
      </v-btn>

      <v-btn value="loans" to="/loans">
        <v-icon>mdi-file-document</v-icon>
        <span>Loans</span>
      </v-btn>

      <v-btn v-if="authStore.isAgent" value="earnings" to="/earnings">
        <v-icon>mdi-currency-usd</v-icon>
        <span>Earnings</span>
      </v-btn>

      <v-btn v-if="authStore.isAdmin" value="admin" to="/admin/dashboard">
        <v-icon>mdi-shield-account</v-icon>
        <span>Admin</span>
      </v-btn>
    </v-bottom-navigation>

    <!-- Main Content -->
    <v-main>
      <slot />
    </v-main>

    <!-- Global Snackbar -->
    <v-snackbar
      v-model="uiStore.snackbar.show"
      :color="uiStore.snackbar.color"
      :timeout="uiStore.snackbar.timeout"
      location="top"
    >
      {{ uiStore.snackbar.message }}
      <template #actions>
        <v-btn variant="text" @click="uiStore.hideSnackbar()">Close</v-btn>
      </template>
    </v-snackbar>
  </v-app>
</template>

<script setup lang="ts">
import { formatRelativeTime } from '~/utils/formatters'

const authStore = useAuthStore()
const uiStore = useUIStore()
const notificationsStore = useNotificationsStore()

const bottomNav = ref('dashboard')

// Fetch notifications on mount
onMounted(async () => {
  await notificationsStore.fetchNotifications()
  notificationsStore.subscribeToRealtime()
})
</script>

<style scoped>
@media (min-width: 960px) {
  .hide-on-desktop {
    display: none !important;
  }
}
</style>
