# Solo Leveling: 90 Days ⚔️

A premium gamified habit tracker and self-improvement application styled after the *Solo Leveling* webtoon system, built strictly following Apple's Human Interface Guidelines (HIG) with the 60-30-10 color scheme.

---

## ⚡ Core Features

- **The Quest System**: Daily tasks generated dynamically based on your onboarding self-improvement goals (Fitness, Sleep, Nutrition, Mindset, etc.).
- **Solo Leveling Progression**: Earn XP for completing daily tasks. Raise your Hunter Level and rank up from **E-Class** to **S-Class**.
- **Streak & Protection**: Log consecutive completion days. Earn **Gate Passes** to protect your streak from breaking on busy days.
- **Hard Mode (No Mercy)**: Real-time XP penalties for missing days, and a compulsory daily **"Hunter's Trial"** quest (cold showers & pushups) that must be finished to advance.
- **Daily Tools Hub**:
  - **Calorie Tracker**: Log foods and estimate daily energy intake.
  - **Meditation Timer**: Clean breathing and mindfulness session timers.
  - **Mood Journal**: Track daily emotional scores and journal thoughts.
  - **Jogging Tracker**: Active route plotting on Google Maps.
- **Cloud Sync (Supabase)**: Offline-first sync model. Use guest mode locally, or sign in to sync all stats, completed dates, cards, and daily logs to the cloud.

---

## 🛠️ Tech Stack

- **UI Framework**: Jetpack Compose (Kotlin)
- **Architecture**: MVI / Repository Pattern
- **Local Cache**: Android Jetpack DataStore (Preferences)
- **Cloud Database & Auth**: Supabase (PostgreSQL, Auth plugin, Postgrest plugin)
- **Background Tasks**: WorkManager (for notifications and streak penalties)
- **Map & Location**: Google Play Services Maps & Location SDK

---

## ⚙️ Setup & Installation

### 1. Prerequisites
- JDK 17 or higher
- Android Studio Koala+ or CLI Android SDK tools

### 2. Clone the Repository
```bash
git clone https://github.com/YOUR_USERNAME/SoloLeveling90Days.git
cd SoloLeveling90Days
```

### 3. Add API Keys
Create a `local.properties` file in the project root directory (this file is excluded from Git tracking via `.gitignore`):

```properties
# Location of the Android SDK
sdk.dir=C\:\\Users\\YOUR_NAME\\AppData\\Local\\Android\\Sdk

# Supabase Configurations
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_ANON_KEY=your-supabase-anon-key

# Google Maps API Key
MAPS_API_KEY=AIzaSyYourGoogleMapsApiKey
```

### 4. Build and Run
Open the project in Android Studio, wait for Gradle sync to complete, and click **Run**.
Alternatively, compile via terminal:
```powershell
.\gradlew.bat assembleDebug
```
The debug APK will be generated under `app/build/outputs/apk/debug/`.
