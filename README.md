# FixIt - On-Demand Mechanic Finder App 🚗🔧

> **⚠️ PROJECT STATUS: ARCHIVED / PORTFOLIO SHOWCASE**
>
> This project was developed as a final year undergraduate assignment. The backend services (Firebase/Google Maps) are currently decommissioned, so the app may not fully function if compiled today.
>
> **Goal:** This repository serves as a showcase of Android development skills, software architecture knowledge, and the ability to implement complex features like real-time geolocation and payment gateways.

---

## 📖 About The Project

**FixIt** is a native Android application designed to solve a critical problem: **Vehicle breakdowns in unfamiliar locations.**

Traditional methods of finding a mechanic rely on word-of-mouth or outdated directories. FixIt solves this by using **Location-Based Services (LBS)** to connect stranded vehicle owners with the nearest qualified mechanics in real-time.

The system features two distinct user roles:
* **Mechanics:** Can manage profiles, set hourly rates, and undergo KYC verification.
* **Vehicle Owners:** Can view nearby mechanics on a map, request help, and track arrival.

## ✨ Key Features

### 🗺️ Real-Time Geolocation
* **Proximity Search:** Uses **GeoHash** queries to filter mechanics within a **10 km radius** of the user.
* **Live Tracking:** Users can track the mechanic's location in real-time as they travel to the breakdown site.
* **Distance Calculation:** Displays exact distance (e.g., "10 km away") to help users choose the fastest option.

### 🔐 Trust & Security (KYC)
* **Verification Badge:** Mechanics must upload NIC (National ID) and a selfie for admin approval.
* **OTP Task Start:** To prevent fraud, the mechanic cannot "start" the billing timer until they enter a unique code provided by the user upon arrival.

### 💸 Automated Billing & Payments
* **Dynamic Cost Engine:** Automatically calculates the final bill based on the **Mechanic’s Hourly Rate** × **Exact Time Duration**.
* **Payment Gateway:** Integrated **PayHere SDK** for secure credit/debit card transactions.
* **Cash Option:** Supports traditional cash payments with manual verification.

### 📊 Dashboard & Analytics
* **Task History:** Complete log of past repairs and payments.
* **Performance Charts:** Integrated **MPAndroidChart** to visualize data.

---

## 📱 Application Screenshots

| **Login / Register** | **Nearby Mechanics Map** | **Mechanic Profile** |
|:---:|:---:|:---:|
| <img src="screenshots/login.png" width="250"> | <img src="screenshots/map_view.png" width="250"> | <img src="screenshots/profile.png" width="250"> |

| **Task Timer** | **Invoice Generation** | **Payment Screen** |
|:---:|:---:|:---:|
| <img src="screenshots/timer.png" width="250"> | <img src="screenshots/invoice.png" width="250"> | <img src="screenshots/payment.png" width="250"> |

---

## 🛠️ Tech Stack

This project follows a **Client-Server** architecture using modern Android development practices.

| Component | Technology |
| :--- | :--- |
| **Language** | Java (Android Native) |
| **UI Framework** | Android XML |
| **Database** | Firebase Firestore (NoSQL) |
| **Auth** | Firebase Authentication |
| **Storage** | Firebase Storage (For KYC Documents) |
| **Maps & Location** | Google Maps SDK, Directions API |
| **Networking** | OkHttp |
| **Payment Gateway** | PayHere Android SDK |
| **Local Database** | SQLite (Offline History) |

---

## 🧩 Architecture Overview

The app handles complex logic on the client side to ensure responsiveness.

1.  **User Request:** The app queries Firestore using **GeoFireUtils** to find documents with matching GeoHashes.
2.  **Handshake:** When a mechanic accepts, a session is created.
3.  **Service Loop:**
    * *Arrival* -> *OTP Verification* -> *Timer Start* -> *Repair* -> *Timer Stop*.
4.  **Completion:** The backend calculates the cost:
    > `Total Cost = (Duration_Hours * Hourly_Rate)`

---

## ⚙️ Setup & Installation

Since the backend keys are expired, you will need to supply your own API keys to build this project.

1.  **Clone the Repo**
    ```bash
    git clone [https://github.com/DilanHansaja/FixIt-Mechanic-Finder-App.git](https://github.com/DilanHansaja/FixIt-Mechanic-Finder-App.git)
    ```
2.  **Open in Android Studio** (Recommended: Ladybug or newer).
3.  **Configure Secrets**
    * Create a `local.properties` file in the root directory.
    * Add your keys:
        ```properties
        GOOGLE_API_KEY=your_google_maps_key_here
        PAYHERE_MERCHANT_ID=your_merchant_id
        ```
4.  **Sync Gradle** & **Run** on an Emulator/Device.

---

## 👨‍💻 Author

* **Software Engineering Student**
* This project is open-source and available for educational purposes.
