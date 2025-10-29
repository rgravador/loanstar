# LoanStar - Lending Management Web Application

A comprehensive, mobile-first responsive web application for managing lending operations built with Nuxt 3, TypeScript, Vuetify 3, Pinia, Supabase, and tRPC.

## Features

### Multi-Role System
- **Admin**: Full system access, loan approvals, cashout management, commission settings
- **Agent**: Account management, loan creation, payment recording, earnings tracking

### Core Functionality
- Borrower account management
- Loan creation with amortization schedule generation
- Loan approval workflow
- Payment recording with automatic balance updates
- Penalty calculation (3% per month, daily computation)
- Commission tracking and earnings management
- Cashout request system
- Real-time notifications
- Comprehensive audit logging

## Project Structure

```
web/
├── assets/
│   └── styles/
│       └── main.scss
├── components/
│   ├── common/           # Reusable components
│   ├── lending/          # Lending-specific components
│   ├── layout/           # Layout components
│   └── auth/             # Auth components
├── composables/
│   ├── useAmortization.ts   ✅ Complete
│   ├── usePenalties.ts      ✅ Complete
│   ├── useCommissions.ts    ✅ Complete
│   ├── useAuth.ts           ⏳ To implement
│   ├── useNotification.ts   ⏳ To implement
│   └── useRealtime.ts       ⏳ To implement
├── layouts/
│   ├── default.vue          ⏳ To implement
│   └── auth.vue             ⏳ To implement
├── middleware/
│   └── auth.ts              ⏳ To implement
├── pages/
│   ├── index.vue            ✅ Complete
│   ├── auth/
│   │   ├── login.vue        ⏳ To implement
│   │   └── signup.vue       ⏳ To implement
│   ├── dashboard.vue        ⏳ To implement
│   ├── accounts/            ⏳ To implement
│   ├── loans/               ⏳ To implement
│   ├── payments/            ⏳ To implement
│   ├── earnings/            ⏳ To implement
│   ├── cashouts/            ⏳ To implement
│   └── admin/               ⏳ To implement
├── server/
│   └── trpc/                ⏳ To implement
│       ├── context.ts
│       ├── trpc.ts
│       └── routers/
├── stores/
│   ├── auth.ts              ✅ Complete
│   ├── accounts.ts          ⏳ To implement
│   ├── loans.ts             ⏳ To implement
│   ├── payments.ts          ⏳ To implement
│   ├── earnings.ts          ⏳ To implement
│   ├── cashouts.ts          ⏳ To implement
│   ├── notifications.ts     ⏳ To implement
│   ├── ui.ts                ⏳ To implement
│   └── admin.ts             ⏳ To implement
├── types/
│   ├── database.ts          ✅ Complete
│   └── index.ts             ✅ Complete
├── utils/
│   ├── constants.ts         ✅ Complete
│   ├── formatters.ts        ✅ Complete
│   └── validators.ts        ⏳ To implement
├── .env.example             ✅ Complete
├── nuxt.config.ts           ✅ Complete
├── package.json             ✅ Complete
└── tsconfig.json            ✅ Complete
```

## Setup Instructions

### 1. Install Dependencies

```bash
cd web
npm install
```

### 2. Configure Environment Variables

Create a `.env` file in the `/web` directory:

```bash
cp .env.example .env
```

Update the `.env` file with your Supabase credentials:

```env
SUPABASE_URL=your_supabase_url_here
SUPABASE_KEY=your_supabase_anon_key_here
SUPABASE_SERVICE_KEY=your_supabase_service_key_here
```

### 3. Set Up Supabase Database

Create the following tables in your Supabase database (see `financer.md` for complete schema):

#### Core Tables:
1. **users_profile** - User data (roles, profile info)
2. **accounts** - Borrower profiles
3. **loans** - Loan records with amortization
4. **payments** - Payment history
5. **notifications** - User notifications
6. **earnings** - Agent earnings tracking
7. **cashout_requests** - Cashout requests
8. **transactions** - Audit log
9. **penalties** - Penalty records

#### Row Level Security (RLS):
- Enable RLS on all tables
- Agents can only access their assigned accounts/loans
- Admins have full access

### 4. Run Development Server

```bash
npm run dev
```

The application will be available at `http://localhost:3000`

## Business Logic Implementation

### Amortization Calculation ✅
The `useAmortization` composable implements:
- EMI calculation using standard formula: `EMI = P * r * (1+r)^n / ((1+r)^n - 1)`
- Support for bi-monthly (15 days), monthly, and weekly payment frequencies
- Complete schedule generation with due dates, principal, interest, and balance

### Penalty Calculation ✅
The `usePenalties` composable implements:
- Daily penalty rate: `(Due amount * 3%) / 30`
- Automatic accumulation from due date
- Penalty breakdown and details

### Commission Calculation ✅
The `useCommissions` composable implements:
- Commission earned on interest portion only
- Percentage-based calculation (set by admin per agent)
- Total and projected commission calculations

## Loan Business Rules

- **Interest Rate**: 3-5% per month
- **Tenure**: 2-12 months
- **Payment Frequencies**: Bi-monthly (15 days), Monthly, Weekly (7 days)
- **Penalties**: 3% per month, calculated daily (3% / 30)
- **Approval Workflow**: All loans start as "pending_approval"
- **Payment Priority**: Penalties → Interest → Principal

## Implementation Checklist

### Phase 1: Foundation ✅ COMPLETE
- [x] Project initialization
- [x] TypeScript configuration
- [x] Environment setup
- [x] Database types
- [x] Utility functions
- [x] Core composables (amortization, penalties, commissions)
- [x] Auth store

### Phase 2: Stores & API (IN PROGRESS)
- [ ] Complete remaining Pinia stores
- [ ] Set up tRPC server
- [ ] Create tRPC routers (auth, accounts, loans, payments, earnings, cashouts, notifications, admin)
- [ ] Implement RPC endpoints

### Phase 3: Components
- [ ] Layout components (AppBar, NavigationDrawer, BottomNavigation)
- [ ] Common components (StatCard, DataTable, ConfirmDialog, etc.)
- [ ] Lending components (LoanForm, PaymentForm, AmortizationTable, etc.)

### Phase 4: Pages
- [ ] Authentication pages (login, signup, password reset)
- [ ] Agent Dashboard
- [ ] Admin Dashboard
- [ ] Accounts management
- [ ] Loans management
- [ ] Payments
- [ ] Earnings
- [ ] Cashouts
- [ ] Notifications
- [ ] Admin tools

### Phase 5: Real-time & Polish
- [ ] Implement Supabase real-time subscriptions
- [ ] Add notification system
- [ ] Create automated penalty calculation (Edge Function)
- [ ] Mobile responsive testing
- [ ] Performance optimization

## Key Dependencies

```json
{
  "nuxt": "^3.10.0",
  "vue": "^3.4.15",
  "vuetify": "^3.5.1",
  "@pinia/nuxt": "^0.5.1",
  "pinia": "^2.1.7",
  "@supabase/supabase-js": "^2.39.3",
  "@nuxtjs/supabase": "^1.1.6",
  "@trpc/server": "^10.45.0",
  "@trpc/client": "^10.45.0",
  "trpc-nuxt": "^0.10.21",
  "zod": "^3.22.4",
  "date-fns": "^3.0.6"
}
```

## Scripts

```bash
npm run dev          # Development server
npm run build        # Production build
npm run generate     # Static generation
npm run preview      # Preview build
npm run lint         # Lint code
npm run lint:fix     # Fix linting issues
npm run type-check   # TypeScript checking
```

## Next Steps

To complete the implementation, continue with:

1. **Stores**: Create remaining Pinia stores for accounts, loans, payments, etc.
2. **tRPC**: Set up server-side procedures with Supabase integration
3. **Components**: Build reusable UI components with Vuetify
4. **Pages**: Implement all pages following the specifications in `financer.md`
5. **Real-time**: Add Supabase subscriptions for live updates
6. **Testing**: Write unit tests for business logic

## Reference Documentation

- Full specifications: See `/financer.md` for complete requirements
- Supabase docs: https://supabase.com/docs
- Nuxt 3 docs: https://nuxt.com/docs
- Vuetify 3 docs: https://vuetifyjs.com
- tRPC docs: https://trpc.io/docs
- Pinia docs: https://pinia.vuejs.org

## License

Private - LoanStar Lending Management System
