# Eventify – Android Event Calendar Builder

**Eventify** is a context-aware Android application that extracts events from notifications, pasted text, or images and syncs them seamlessly with Google Calendar. The app leverages modern Android development practices (Jetpack Compose, MVVM architecture) and intelligent LLMs (via Gemini API) to enhance user productivity by simplifying event management.

---

## Features

- **Smart Notification Parsing**: Captures incoming notifications, determines event relevance using LLM classification, and auto-generates calendar entries.
- **Text/Image-Based Event Extraction**: Users can paste or upload textual content/images; events are extracted using Gemini-powered natural language understanding.
- **Google Calendar Sync**: Extracted events are automatically added to the user's Google Calendar with support for token refresh and multi-user access.
- **Event Management UI**: Full calendar viewer, inline event editing, and discard capabilities integrated in an intuitive Jetpack Compose UI.
- **Multi-Account Support**: Securely handles multiple signed-in Google accounts and their respective tokens using Room persistence.
- **Offline-First Local Storage**: All notifications and events are stored locally via Room, with graceful syncing to Google Calendar when connectivity allows.

---

## Architecture

- **Frontend**: Jetpack Compose + Navigation + Material3
- **Architecture Pattern**: MVVM (Model-View-ViewModel)
- **Persistence**: Room (SQLite ORM)
- **Network**: Retrofit + OkHttp + Google Calendar API
- **AI Integration**: Google Gemini 2.0 (GenerativeModel API)
- **Authentication**: Google Sign-In + OAuth2.0 token management
- **LLM-Based Modules**:
  - `LlmEventExtractor`: Extracts detailed event information from freeform input
  - `LlmEventImportance`: Classifies whether a notification contains a valid event

---

## Maintainability

This project was designed with **maintainability** as a core principle:

- **Separation of Concerns**: Code is modularized across `ViewModels`, `Repositories`, and `UI Screens`, ensuring logical separation of data, business logic, and presentation.
- **Scalable Architecture**: MVVM with dependency injection-ready ViewModels allows for clean extension and testing.
- **Room Database Integration**: Provides robust offline access and local caching for all users, events, and notifications.
- **Retry and Error Handling**: Google Calendar API interactions are wrapped with safe retries, token refresh logic, and detailed logging.
- **State Management**: Uses Kotlin `StateFlow` and Compose state management, minimizing side effects and ensuring UI consistency.
- **Testability**: Logic is encapsulated in ViewModels and Repositories to allow isolated unit testing using coroutine test libraries and MockWebServer.

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- Kotlin 1.9+
- Google Cloud project with Calendar API and Gemini API enabled
- Firebase (optional for backend if using extended features)

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/your-org/eventify-android.git
   ```

2. Create a `Constants.kt` file with:
   ```kotlin
   object Constants {
       const val GEMINI_API_KEY = "your_gemini_api_key"
       const val GOOGLE_CLIENT_ID = "your_client_id"
       const val GOOGLE_CLIENT_SECRET = "your_client_secret"
   }
   ```

3. Enable **Notification Access** manually or via in-app prompt.

4. Run on a device or emulator with Google Play Services.

---

## License

This project is developed as part of a university final year project and is intended for academic and educational use. All API usage complies with respective service terms.
