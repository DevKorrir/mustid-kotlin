
## README for MUSTID QR Scanner App

# 📱 MUSTID QR Scanner for smart student identification system

## 📖 Overview

The MUSTID QR Scanner app is a modern mobile application designed to scan QR codes containing encrypted student data. 
Utilizing state-of-the-art technologies such as Kotlin, Jetpack Compose, and CameraX, the application reads QR codes containing
image URLs and displays the corresponding student ID photo. It also supports decrypting QR data (encrypted using CryptoJS AES in passphrase mode) and displays the student's details securely
with the functionality to detect expired QR codes and prompt the users accordingly. 
The app is built with an MVVM architecture, employs Hilt for dependency injection,
and uses ML Kit for real-time QR code detection.

## 🚀 Features

- **Real-Time QR Code Scanning:**  
   - Utilizes CameraX and ML Kit to analyze frames in real time and detect QR codes.
- **Image URL Extraction:**  
   - QR codes are expected to contain an image URL. The app reads this URL and displays the associated image.
- **Data Decryption:**
  - Decrypts encrypted QR code data using a secret passphrase.
- **Expiration Handling:**
  - Checks an embedded expiration timestamp and displays an error if the QR code is expired.
- **MVVM Architecture:**  
   - Separates business logic from UI, enhancing maintainability and testability.
- **Dependency Injection:**  
  - Hilt is used for efficient dependency management.
- **Modern UI:**  
   - Built with Jetpack Compose for a clean, modern, and responsive user interface.
- **Dynamic UI with Jetpack Compose:**
   - Displays student information, error messages, and a full-screen image preview with a zoom feature.
- **Error Handling:**
  - Provides user feedback via Toasts, dialogs, and error displays.
- **Image Loading:**  
   - Uses Coil to load and display images efficiently.

## 🛠️ Technologies Used

- **Core Language:** Kotlin
- **UI framework:** Jetpack Compose
- **ViewModel + State** – MVVM Architecture
- **CryptoJS AES** (passphrase mode) – For encryption (used outside Android in main web App)
- **AES Decryption (Javax.crypto)** – To decrypt on the device (replicating CryptoJS behavior)
- **Dependency Injection:** Hilt (Dagger)
- **Networking**: Retrofit, OkHttp
- **Camera Integration:** CameraX
- **QR Code Scanning:** ML Kit Barcode Scanning
- **Image Loading:** Coil

# Prerequisites
- Android Studio
- JDK Version 11
- Android SDK 28
- Minimum Android API 24 (or higher as specified in your Gradle config)
- Knowledge of Jetpack Compose
- Git

# **Code Structure**
- ## **ViewModel:**
  QRScannerViewModel handles scanning, decryption, state management (using Result sealed class), and expiration checking.
- ## **UI Components:**
 - **QRScannerScreen:**
    The main screen responsible for camera preview, error display, and student details.
  
- **ErrorDisplay:**
   A composable for showing error messages (e.g., expired QR code).

- **StudentDetailsCard:**
   A composable that displays the student's data.

- **ZoomableImage:**
   Enables full-screen zooming of the student's image.

```
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/dev/korryr/digitalid/ui/features/qrReader/view
│   │   │   │   ├── QRScannerScreen.kt       // Composable UI for scanning
│   │   │   │   └── ...
│   │   │   ├── java/dev/korryr/digitalid/ui/features/qrReader/viewModel
│   │   │   │   └── QRScannerViewModel.kt      // ViewModel for QR scanning logic
│   │   │   ├── java/dev/korryr/digitalid/ui/features/qrReader/repo
│   │   │   │   └── StudentRepository.kt       // Repository for API calls (if applicable)
│   │   │   └── res/                           // Layouts, themes, etc.
│   └── build.gradle
└── build.gradle

```

## 🔐 **Decryption:**
The app decrypts QR data using an EVP_KDF implementation and AES/CBC/PKCS5Padding, matching the CryptoJS encryption on the server.

### How It Works:

- The QR code contains data encrypted using **CryptoJS AES (passphrase mode)**.
- When scanned, the app detects encrypted content (by checking if the QR string starts with `"U2FsdGVk"`).
- It then decrypts the QR code by:
  1. Extracting the salt and cipher text.
  2. Deriving the AES key and IV using a method similar to CryptoJS's `evpKDF` (MD5-based).
  3. Decrypting using AES/CBC/PKCS5Padding on Android.
  4. Parsing the JSON content and checking the expiration (`expiresAt`).
- If the QR code is valid and not expired, the app displays the student details.
- If expired or invalid, an error message is shown.


## 📸 Screenshots



## Future Enhancements
- Expand the scanned data to include more student details.
- Implement navigation to a detailed student profile screen.
- Add offline support and local caching for scanned data.
- Improve error handling and real-time feedback.

## Setup and Installation

1. **Clone the Repository:**
   ```bash
   git clone https://git@github.com:DevKorrir/mustid-kotlin.git
   cd DigitalId
   
---
2. **Config local.properties**
   File named `local.properties` exist in the root directory with the following content:

```properties
#Ecryption config
SECRET_KEY=SECRET_KEY_goes_here

```

### Why `local.properties`?

- Keeps sensitive information (such as API keys and decryption key) out of version control
- Ensures that contributors can configure their own environment without affecting others
- Prevents accidental exposure of security credentials in public repositories

### Explanation of Properties

- `SECRET_KEY`: This key used to decrypt the encrypted codes to retrieve students data securely

### Ensure the File is Ignored in Version Control

To prevent accidental commits of sensitive information, make sure `local.properties` is included in
the `.gitignore` file:

```gitignore
local.properties
```

### How the File is Used in `build.gradle.kts`

The properties are loaded in the build script and used to configure the backend URL and signing
configuration:

```kotlin
# imports
import java.io.FileInputStream
import java.util.Properties

#variable
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

# defaultConfig 
 buildConfigField("String", "SECRET_KEY", "\"${localProperties.getProperty("SECRET_KEY", "")}\"")

# buildFeatures
buildFeatures {
        //other codes
        buildConfig = true
    }

# example in the use code
val secretKey = BuildConfig.SECRET_KEY.trim() /

```

- ## **This project is licensed under the MIT License.**

Feel free to adjust sections, wording, or structure to best match your projects and personal preferences. These README files provide a comprehensive yet concise documentation of the technologies used, project structure, and core functionalities for your presentation.

# Conclusion
By properly setting up the lacal.properties file, contributors can securely configure their local development environment and ensure smooth builds. This setup helps maintain security and project integrity while allowing flexibility in managing sensitive credentials.

