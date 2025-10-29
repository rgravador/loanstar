// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  devtools: { enabled: true },

  modules: [
    '@pinia/nuxt',
    '@nuxtjs/supabase',
    'vuetify-nuxt-module'
  ],

  typescript: {
    strict: true,
    typeCheck: true
  },

  css: [
    'vuetify/styles',
    '@/assets/styles/main.scss'
  ],

  build: {
    transpile: ['trpc-nuxt', 'vuetify']
  },

  vuetify: {
    moduleOptions: {
      /* module specific options */
    },
    vuetifyOptions: {
      theme: {
        defaultTheme: 'light',
        themes: {
          light: {
            colors: {
              primary: '#1976D2',
              secondary: '#424242',
              accent: '#82B1FF',
              error: '#FF5252',
              info: '#2196F3',
              success: '#4CAF50',
              warning: '#FFC107',
            }
          },
          dark: {
            colors: {
              primary: '#2196F3',
              secondary: '#424242',
              accent: '#82B1FF',
              error: '#FF5252',
              info: '#2196F3',
              success: '#4CAF50',
              warning: '#FFC107',
            }
          }
        }
      }
    }
  },

  supabase: {
    redirect: false,
    redirectOptions: {
      login: '/auth/login',
      callback: '/auth/callback',
      exclude: ['/auth/*']
    }
  },

  pinia: {
    storesDirs: ['./stores/**']
  },

  runtimeConfig: {
    public: {
      supabaseUrl: process.env.SUPABASE_URL,
      supabaseKey: process.env.SUPABASE_KEY,
    },
    supabaseServiceKey: process.env.SUPABASE_SERVICE_KEY,
  },

  compatibilityDate: '2025-01-29'
})
