# CoinTracker 🪙

A comprehensive cross-platform coin tracking application for managing personal finances and digital tokens. Available on **Android**, **Desktop** (Windows, macOS, Linux), and **Web**.

## 🌟 Features

- **Multi-platform Support**: Native Android app, PyQt5 desktop GUI, and Flask web interface
- **Transaction Management**: Track income, expenses, and coin balances across multiple profiles
- **Analytics Dashboard**: Visual charts and statistics for spending patterns and trends
- **Goal Tracking**: Set and monitor financial goals with progress indicators
- **Firebase Integration**: Real-time cloud sync across all platforms
- **User Authentication**: Secure login system with role-based access (user/admin)
- **Profile Management**: Create multiple profiles for different tracking needs
- **Data Import/Export**: Backup and restore your data in JSON format
- **Achievements System**: Unlock achievements based on your tracking milestones

---

## 🏗️ Project Structure

```
CoinTracker/
├── android/                    # Android app (Kotlin + Jetpack Compose)
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/cointracker/mobile/
│   │   │   └── AndroidManifest.xml
│   │   ├── build.gradle
│   │   └── google-services.json    # Firebase config (not in git)
│   ├── build.gradle
│   ├── settings.gradle
│   └── README.md
│
├── desktop/                    # Desktop app (PyQt5)
│   ├── coin_tracker.py        # Main application logic
│   ├── build.py               # PyInstaller build script
│   └── coin_icon.py           # Icon generator script
│
├── web/                        # Web app (Flask)
│   ├── app.py                 # Flask application core
│   ├── requirements.txt       # Python dependencies
│   ├── render.yaml            # Render deployment config
│   ├── static/
│   │   ├── css/              # Stylesheets
│   │   ├── js/               # JavaScript files
│   │   └── images/           # Static images
│   └── templates/             # HTML templates
│       ├── index.html
│       ├── admin.html
│       └── login.html
│
├── .gitignore
├── LICENSE
└── README.md
```

---

## 🚀 Quick Start

### Prerequisites

- **For Android**: Android Studio (AGP 8.5+, Kotlin 1.9+), Android SDK 26+
- **For Desktop**: Python 3.8+, PyQt5, PyQtChart (optional)
- **For Web**: Python 3.8+, Flask, Firebase Admin SDK
- **Firebase Account**: Required for cloud sync features

---

## 📱 Android App

### Features

- Native Android UI with Jetpack Compose
- Material 3 design with glassmorphism effects
- Offline-first architecture with Firestore sync
- Compatible with web/desktop password hashing (Werkzeug PBKDF2)
- Admin panel for user management

### Setup

1. **Open in Android Studio**
   ```bash
   cd android
   # Open this directory in Android Studio
   ```

2. **Configure Firebase**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Add an Android app with package name: `com.cointracker.mobile`
   - Download `google-services.json` and place it in `android/app/`
   - Enable Firestore Database in Native mode

3. **Build and Run**
   - Connect an Android device or start an emulator
   - Click "Run" in Android Studio or use:
   ```bash
   ./gradlew installDebug
   ```

### Requirements

- Android SDK 26+ (Android 8.0 Oreo)
- Target SDK: 34 (Android 14)
- Kotlin 1.9+

---

## 💻 Desktop App

### Features

- Cross-platform PyQt5 GUI
- Local JSON storage with optional Firebase sync
- PyQtChart integration for visual analytics
- Dark/Light theme support
- Standalone executable builds

### Setup

1. **Install Dependencies**
   ```bash
   cd desktop
   pip install PyQt5 PyQt5-sip firebase-admin python-dotenv
   # Optional for charts:
   pip install PyQtChart
   ```

2. **Run the Application**
   ```bash
   python coin_tracker.py
   ```

3. **Build Executable (Optional)**
   ```bash
   # Install PyInstaller
   pip install pyinstaller
   
   # Generate icon (optional)
   python coin_icon.py
   
   # Build executable
   python build.py
   ```
   The executable will be created in the `dist/` folder.

### Data Storage

- Local data stored at: `~/Documents/CoinTracker/<Profile>.json`
- Firebase sync optional (requires configuration)

---

## 🌐 Web App

### Features

- Responsive Flask web interface
- Real-time Firebase integration
- Admin dashboard for user management
- Session-based authentication
- RESTful API endpoints

### Local Development

1. **Install Dependencies**
   ```bash
   cd web
   pip install -r requirements.txt
   ```

2. **Configure Firebase**
   
   Create a `.env` file in the `web/` directory:
   ```env
   SECRET_KEY=your-secret-key-here
   FIREBASE_PROJECT_ID=your-project-id
   FIREBASE_PRIVATE_KEY_ID=your-private-key-id
   FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n"
   FIREBASE_CLIENT_EMAIL=your-service-account@project.iam.gserviceaccount.com
   FIREBASE_CLIENT_ID=your-client-id
   ```
   
   **OR** place your `firebase-key.json` in the `web/` directory.

3. **Run the Application**
   ```bash
   python app.py
   ```
   Access at: `http://localhost:5000`

### Production Deployment (Render)

1. **Push to GitHub**
   ```bash
   git add .
   git commit -m "Initial commit"
   git push origin main
   ```

2. **Deploy on Render**
   - Connect your GitHub repository
   - Render will automatically detect `render.yaml`
   - Add environment variables in Render dashboard
   - Deploy!

3. **Environment Variables Required**
   - `SECRET_KEY`
   - `FIREBASE_PROJECT_ID`
   - `FIREBASE_PRIVATE_KEY`
   - `FIREBASE_PRIVATE_KEY_ID`
   - `FIREBASE_CLIENT_EMAIL`
   - `FIREBASE_CLIENT_ID` (optional)

---

## 🔥 Firebase Setup

All platforms use the same Firebase project for data synchronization.

### 1. Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" and follow the setup wizard
3. Enable Firestore Database in **Native** mode

### 2. Configure Firestore

Create two collections with the following structure:

**`users` collection:**
```json
{
  "username": "john_doe",
  "username_lower": "john_doe",
  "password_hash": "pbkdf2:sha256:...",
  "created_at": "2026-01-03T10:30:00Z",
  "role": "user"
}
```

**`user_data` collection:**
```json
{
  "profiles": {
    "Default": {
      "transactions": [],
      "settings": {
        "initial_balance": 0,
        "currency": "USD"
      },
      "last_updated": "2026-01-03T10:30:00Z"
    }
  },
  "last_active_profile": "Default"
}
```

### 3. Set Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users collection
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      allow read: if request.auth != null && 
                    get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }
    
    // User data collection
    match /user_data/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      allow read: if request.auth != null && 
                    get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }
  }
}
```

### 4. Get Service Account Credentials

1. Go to **Project Settings** → **Service Accounts**
2. Click **Generate New Private Key**
3. Save the JSON file securely

---

## 📊 Data Model

### Transaction Structure
```json
{
  "id": "uuid-v4",
  "type": "income|expense",
  "amount": 100.50,
  "category": "Salary|Food|Entertainment|...",
  "date": "2026-01-03",
  "time": "14:30:00",
  "description": "Optional note",
  "timestamp": "2026-01-03T14:30:00Z"
}
```

### Profile Settings
```json
{
  "initial_balance": 1000.00,
  "currency": "USD",
  "notification_enabled": true,
  "goals": [
    {
      "id": "uuid-v4",
      "name": "Save for vacation",
      "target_amount": 5000.00,
      "current_amount": 2500.00,
      "deadline": "2026-12-31"
    }
  ]
}
```

---

## 🔐 Authentication

All platforms use **Werkzeug PBKDF2** password hashing for compatibility:
- Hash format: `pbkdf2:sha256:600000$<salt>$<hash>`
- Passwords are never stored in plain text
- Role-based access: `user` or `admin`

---

## 🛠️ Development

### Running Tests

```bash
# Android
cd android
./gradlew test

# Python (Desktop/Web)
pytest tests/
```

### Code Style

- **Kotlin**: Follow Android Kotlin style guide
- **Python**: PEP 8 compliant
- **JavaScript**: ES6+ with consistent formatting

---

## 📦 Building for Production

### Android APK/AAB
```bash
cd android
# Debug APK
./gradlew assembleDebug

# Release APK (requires signing)
./gradlew assembleRelease

# Release AAB (for Play Store)
./gradlew bundleRelease
```
Output location: `android/app/build/outputs/`

### Desktop Executable
```bash
cd desktop
python build.py
```
Executable location: `desktop/dist/CoinTracker.exe` (or equivalent for your OS)

### Web Deployment
- See [Production Deployment](#production-deployment-render) section above
- Supports Docker containerization (optional)

---

## 🐛 Troubleshooting

### Android Build Issues
- **Gradle sync failed**: Check internet connection, update Android Studio
- **Firebase not working**: Verify `google-services.json` is in `android/app/`
- **Compose preview error**: File → Invalidate Caches / Restart
- **Build tools version error**: Update Android SDK in SDK Manager

### Desktop Issues
- **PyQt5 not found**: Install with `pip install PyQt5`
- **Firebase import error**: Install with `pip install firebase-admin`
- **Charts not showing**: Install PyQtChart: `pip install PyQtChart`
- **App crashes on startup**: Check Python version (3.8+ required)

### Web Issues
- **Firebase connection failed**: Check environment variables and service account key
- **Session expired**: Increase `PERMANENT_SESSION_LIFETIME` in `app.py`
- **Static files not loading**: Check `static/` folder permissions
- **Port already in use**: Kill process on port 5000 or use different port

---

## 📝 Usage Examples

### Desktop Application
1. Launch the application
2. Create or select a profile
3. Add transactions using quick buttons (+10, +20, -5, etc.) or manual entry
4. View analytics dashboard for spending insights
5. Set financial goals and track progress
6. Export data as JSON for backup

### Web Application
1. Register a new account or login
2. Navigate using the sidebar menu
3. Add income/expense transactions
4. View dashboard for current balance and recent activity
5. Check analytics for monthly/yearly trends
6. Admin users can manage all users and view statistics

### Android Application
1. Install and launch the app
2. Login with existing credentials or register
3. Use bottom navigation for Dashboard, Analytics, History, Settings
4. Add transactions with floating action button
5. View charts and achievements
6. Admin panel available for admin users

---

## 🚀 Roadmap

- [ ] Push notifications for goal milestones
- [ ] Recurring transaction support
- [ ] Budget planning features
- [ ] Multi-currency support with exchange rates
- [ ] Receipt scanning with OCR
- [ ] Widget support for Android and Desktop
- [ ] Dark mode improvements
- [ ] Export to PDF reports
- [ ] Collaborative profiles (family sharing)

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Development Guidelines

- Follow existing code style and conventions
- Write clear commit messages
- Add tests for new features
- Update documentation as needed
- Ensure all platforms remain compatible

---

## 📧 Support

For issues, questions, or suggestions:
- Open an issue on GitHub
- Check existing documentation in platform-specific README files
- Review troubleshooting section above

---

## 🙏 Acknowledgments

- **Firebase** for cloud infrastructure and real-time sync
- **PyQt5** for powerful desktop GUI framework
- **Flask** for lightweight and flexible web framework
- **Jetpack Compose** for modern Android UI development
- **Material Design** for beautiful and consistent UI patterns

---

## 📸 Screenshots

*Screenshots can be added in the `screenshots/` directory*

- Desktop Dashboard
- Web Interface
- Android App
- Analytics Charts
- Admin Panel

---

**Made with ❤️ for better financial tracking**

*Version 6 - Multi-platform Edition*
