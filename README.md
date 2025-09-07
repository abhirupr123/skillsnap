# SkillSnap - Your Personal Micro-Coach 📱

A micro-skill learning app where users pick a skill (e.g., Fitness, Languages, Journaling), and the app generates a 7-day challenge plan using AI, stores it locally, and reminds them daily with notifications.

## 🛠️ Tech Stack

- **Kotlin + Jetpack Compose** → Modern Android UI
- **Room Database** → Store challenges offline
- **WorkManager** → Daily reminders for tasks
- **Firebase Cloud Messaging (FCM)** → Optional "Weekly Global Challenge" push
- **Logcat** → Simple execution-time logs for performance profiling
- **EncryptedSharedPreferences** → Store user settings securely
- **AI Integration (Gemini API)** → Generate micro-challenges dynamically
- **Hilt** → Dependency injection
- **Retrofit** → Network calls

## 📱 Features

### Core Features
- **Skill Selection**: Users can input any skill they want to learn
- **AI Challenge Generation**: Creates 7 personalized daily micro-challenges (5-10 minutes each)
- **Offline Storage**: All challenges stored locally using Room database
- **Daily Reminders**: WorkManager schedules daily notifications
- **Progress Tracking**: Visual progress indicator and challenge completion tracking
- **Modern UI**: Beautiful Material 3 design with Jetpack Compose

### User Flow
1. **Onboarding**: User enters skill (e.g., "Cooking", "Music", "Programming")
2. **AI Generation**: App generates 7 progressive daily challenges
3. **Daily Tasks**: Shows today's challenge with completion tracking
4. **Reminders**: Daily notifications remind users of their micro-tasks
5. **Progress**: Visual tracking of completed vs pending challenges

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- Android SDK 24+
- OpenAI API key (optional - app has fallback challenges)

### Setup
1. Clone the repository
2. Open in Android Studio
3. Add your OpenAI API key in `NetworkModule.kt`:
   ```kotlin
   .addHeader("Authorization", "Bearer YOUR_API_KEY_HERE")
   ```
4. Replace the placeholder `google-services.json` with your Firebase configuration
5. Build and run the app

### Firebase Setup
1. Create a Firebase project
2. Add your Android app to the project
3. Download `google-services.json` and replace the placeholder file
4. Enable Firebase Cloud Messaging for global challenges

## 📂 Project Structure

```
app/
├── src/main/java/com/skillsnap/app/
│   ├── data/
│   │   ├── api/           # AI service and API models
│   │   ├── database/      # Room database, DAO, converters
│   │   ├── model/         # Data models
│   │   └── repository/    # Repository pattern implementation
│   ├── di/                # Hilt dependency injection modules
│   ├── notification/      # WorkManager, FCM, notification handling
│   ├── ui/
│   │   ├── screen/        # Compose UI screens
│   │   ├── theme/         # Material 3 theme
│   │   └── viewmodel/     # ViewModels
│   ├── MainActivity.kt    # Main activity
│   └── SkillSnapApplication.kt
└── src/main/res/          # Resources (strings, colors, etc.)
```

## 🎯 Key Components

### AI Challenge Generation
- Uses OpenAI API to generate personalized challenges
- Fallback system with pre-defined challenges for popular skills
- Progressive difficulty over 7 days

### Local Storage (Room)
- Stores challenges offline for reliability
- Tracks completion status and timestamps
- Supports multiple skills simultaneously

### Notifications (WorkManager)
- Daily reminders at scheduled times
- Survives app restarts and device reboots
- Shows specific challenge text in notifications

### Modern UI (Compose)
- Material 3 design system
- Responsive layouts
- Smooth animations and transitions

## 🔧 Configuration

### AI Service
The app uses Gemini's API by default but includes fallback challenges for:
- Spanish learning
- Push-up training
- Journaling practice
- Generic skill development

### Notifications
- Default reminder time: 1 hour after challenge generation
- Repeats daily until all challenges are completed
- Requires notification permission on Android 13+

## 🚀 Building for Production

1. Update the API key in `NetworkModule.kt`
2. Replace Firebase configuration with your project's `google-services.json`
3. Update app signing configuration
4. Build release APK:
   ```bash
   ./gradlew assembleRelease
   ```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

---

**SkillSnap** - Turn any skill into a 7-day micro-learning journey! 🎯 
