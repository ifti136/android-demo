# Coin Tracker Android (Compose + Firebase)

This module is a Kotlin/Jetpack Compose rewrite of the web app, keeping the same flows (dashboard, analytics, history, settings, admin, login) and talking directly to Firestore using the same data model as `web/app.py` (`users` and `user_data` collections with profiles, transactions, and settings).

## Project layout

- Gradle + Compose app lives in `android/`
- App entry: `app/src/main/java/com/cointracker/mobile/MainActivity.kt`
- UI + nav: `ui/` (screens and `CoinTrackerApp`), glassmorphism `GlassCard`
- Data + logic: `data/FirestoreRepository.kt` (CRUD, admin, profiles), `data/WerkzeugPasswordHasher.kt` (compatible with Flask hashes), `domain/AchievementCalculator.kt`
- Theme: `ui/theme/`

## Firebase setup

1. Create a Firebase project with Firestore in _Native_ mode.
2. Download `google-services.json` and place it at `android/app/google-services.json`.
3. Collections expected (matching the Flask backend):
   - `users` docs: `username`, `username_lower`, `password_hash` (Werkzeug pbkdf2), `created_at`, `role` ("user" or "admin").
   - `user_data` docs: `profiles` map -> `{ profileName: { transactions, settings, last_updated } }`, `last_active_profile`.
4. Enable Firestore rules to restrict users to their own docs; admins can read all.

## Running

- Open `android/` in Android Studio (AGP 8.5, Kotlin 1.9+).
- Plug in or start an emulator, then run the `app` configuration.

## Feature parity notes

- Login/Register uses Firestore and a PBKDF2 hash compatible with Werkzeug so existing web passwords keep working.
- Dashboard, Analytics, History, Settings, Profiles, Quick Actions, Achievements, and Admin stats mirror the Flask endpoints but run client-side via Firestore.
- Styling follows the web glassmorphism look with translucent cards and gradients.

## Next steps

- Add charts (e.g., Compose multiplatform charts) for analytics timeline/breakdowns.
- Wire push broadcast to mirror `/api/broadcast` if needed.
- Harden error handling and offline cache (e.g., Room or Firestore persistence).
