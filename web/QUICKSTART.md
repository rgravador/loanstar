# 🚀 LoanStar Quick Start Guide

## Prerequisites
- Node.js 18+ installed
- npm or yarn package manager
- Supabase account (free tier works)

## Step-by-Step Setup

### 1. Install Dependencies (5 minutes)
```bash
cd web
npm install
```

This will install all required packages including:
- Nuxt 3, Vue 3, TypeScript
- Vuetify 3 (UI framework)
- Pinia (state management)
- Supabase (backend)
- date-fns (date utilities)
- And more...

### 2. Create Supabase Project (10 minutes)

1. Go to [supabase.com](https://supabase.com) and create a free account
2. Click "New Project"
3. Fill in:
   - Project name: `loanstar`
   - Database password: (choose a strong password and save it)
   - Region: (choose closest to you)
4. Wait for project to be created (~2 minutes)

### 3. Set Up Database (5 minutes)

1. In your Supabase project, go to **SQL Editor**
2. Click "New Query"
3. Copy the entire contents of `database-setup.sql` from this project
4. Paste into the SQL editor
5. Click "Run" or press Ctrl+Enter
6. You should see "Success. No rows returned"

This creates:
- 9 tables (users_profile, accounts, loans, payments, etc.)
- All relationships and constraints
- Row Level Security policies
- Indexes for performance
- Triggers for auto-updates

### 4. Get Your Supabase Credentials (2 minutes)

1. In Supabase, go to **Settings** → **API**
2. Copy these values:
   - Project URL (under "Project URL")
   - Anon (public) key (under "Project API keys" → "anon public")
   - Service role key (under "Project API keys" → "service_role" - click "Reveal")

### 5. Configure Environment Variables (1 minute)

1. Copy the example file:
```bash
cp .env.example .env
```

2. Edit `.env` and paste your Supabase credentials:
```env
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_KEY=your_anon_key_here
SUPABASE_SERVICE_KEY=your_service_role_key_here
```

### 6. Start Development Server (1 minute)
```bash
npm run dev
```

The app will start at `http://localhost:3000`

### 7. Create Your First Account (2 minutes)

1. Open `http://localhost:3000` in your browser
2. Click "Sign Up"
3. Fill in the form:
   - Full Name: Your Name
   - Username: yourusername
   - Email: your@email.com
   - Password: (minimum 6 characters)
   - Role: Choose "Agent" (or "Admin" for full access)
4. Click "Sign Up"
5. You'll be redirected to login
6. Login with your credentials
7. You'll see the Agent Dashboard!

---

## ✅ What Works Now

Once you're logged in, you can:

### Agent Features:
- ✅ View dashboard with stats
- ✅ See notifications (bell icon in top bar)
- ✅ Toggle dark/light theme
- ✅ Mobile responsive design works
- ⏳ Create accounts (page needs to be built)
- ⏳ Create loans (page needs to be built)
- ⏳ Record payments (page needs to be built)

### Admin Features (if you signed up as Admin):
- ✅ Access to all agent features
- ✅ View all accounts across all agents
- ⏳ Approve loans (page needs to be built)
- ⏳ Manage cashouts (page needs to be built)
- ⏳ View audit logs (page needs to be built)

---

## 🧪 Testing the Application

### Test with Sample Data

You can manually add test data through Supabase:

1. Go to Supabase → **Table Editor**
2. Create a test account in `accounts` table:
   - assigned_agent_id: Your user ID (from users_profile table)
   - name: "John Doe"
   - contact_info: "555-1234"
   - address: "123 Main St"
   - status: "active"

3. The dashboard will automatically show this account!

### Test Real-time Notifications

1. Open two browser windows/tabs
2. Login as the same user in both
3. In Supabase, go to **Table Editor** → **notifications**
4. Insert a new notification:
   - user_id: Your user ID
   - message: "Test notification"
   - type: "payment_received"
5. Watch both windows receive the notification in real-time!

---

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Kill process on port 3000
npx kill-port 3000

# Or use a different port
PORT=3001 npm run dev
```

### Module Not Found Errors
```bash
# Clear cache and reinstall
rm -rf node_modules .nuxt
npm install
```

### Supabase Connection Issues
- Check that `.env` credentials are correct
- Verify Supabase project is active
- Check that database tables were created successfully

### Login Not Working
- Verify `users_profile` table exists
- Check that auth trigger is working (should auto-create profile)
- Look at browser console for errors (F12)

---

## 📝 Next Development Steps

The application foundation is complete. To continue development:

### Priority 1: Account Management Pages
```bash
# Create these files:
pages/accounts/index.vue       # List all accounts
pages/accounts/create.vue      # Create new account
pages/accounts/[id].vue        # View account details
```

### Priority 2: Loan Management Pages
```bash
pages/loans/index.vue          # List all loans
pages/loans/create.vue         # Create loan with schedule
pages/loans/[id].vue           # View loan details
```

### Priority 3: Payment Pages
```bash
pages/payments/index.vue       # Payment history
pages/payments/create.vue      # Record payment
```

See `PROGRESS_UPDATE.md` for detailed remaining tasks.

---

## 🎯 Development Workflow

```bash
# Start dev server
npm run dev

# Type check
npm run type-check

# Build for production
npm run build

# Preview production build
npm run preview
```

---

## 📖 Key Documentation Files

- **README.md** - Complete project overview
- **PROGRESS_UPDATE.md** - Current implementation status
- **IMPLEMENTATION_STATUS.md** - Detailed task tracking
- **financer.md** - Full specifications
- **database-setup.sql** - Database schema

---

## 💡 Tips

1. **Check the console** - Browser DevTools (F12) shows useful errors
2. **Watch Supabase logs** - See database queries in Supabase dashboard
3. **Use dark mode** - Click sun/moon icon in top bar
4. **Mobile testing** - Resize browser or use device emulation (F12 → Device Toolbar)
5. **Real-time works** - All data updates automatically across tabs

---

## 🎉 You're Ready!

Your LoanStar development environment is now set up and running. The core backend, business logic, authentication, and dashboard are all working.

Start building the remaining pages using the existing stores and composables. Everything you need is already there!

**Happy coding!** 🚀
