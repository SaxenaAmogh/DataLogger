# 📊Data Logger App

An Android application built to interface with an ESP32-based hardware system. The app connects to the ESP32, retrieves live sensor data, displays it in real time, and uploads the readings to Firebase for cloud storage and further analysis.
## Tech Stack

- Android (Kotlin / Jetpack Compose) – App development

- ESP32 (Arduino Framework) – Hardware firmware for data communication

- Firebase Realtime Database – Cloud storage and synchronization

- MVVM Architecture


## Features

- 🔗 ESP32 Connectivity – Connects to an ESP32 microcontroller over Wi-Fi/Bluetooth to retrieve sensor data.

- 📟 Real-time Monitoring – Displays live sensor values inside the app.

- ☁️ Cloud Integration – Uploads collected data to Firebase for secure storage and remote access.

- 📱 User-friendly UI – Simple interface for viewing and managing sensor logs.


## Installation

Follow these steps to set up the project locally:

- Open Android Studio.

- Click on Get from Version Control.

- Paste the repo URL:

    ```bash
    https://github.com/SaxenaAmogh/DataLogger.git
    ```

OR clone it using Git:

```bash
git clone https://github.com/SaxenaAmogh/DataLogger.git
```

- Open the project in Android Studio.

Download the google-services.json file from the Firebase project.

Place the file inside the app’s "/app" directory.