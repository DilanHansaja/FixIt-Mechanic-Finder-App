<div align="center">

  <h1>🛠️ FixIt</h1>
  
  <p>
    <b>On-Demand Mechanic Finder & Emergency Assistance App</b>
  </p>

  <p>
    <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
    <img src="https://img.shields.io/badge/Language-Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
    <img src="https://img.shields.io/badge/Backend-Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" />
    <img src="https://img.shields.io/badge/Status-Archived-red?style=for-the-badge" />
  </p>

  <p>
    <a href="#-key-features">Key Features</a> •
    <a href="#%EF%B8%8F-tech-stack--dependencies">Tech Stack</a> •
    <a href="#%EF%B8%8F-setup--installation">Setup</a> •
    <a href="#-author">Author</a>
  </p>

</div>

---

> **⚠️ PROJECT STATUS: ARCHIVED / PORTFOLIO SHOWCASE**
> 
> This project was developed as a final year undergraduate assignment. The backend services (Firebase/Google Maps) are currently decommissioned. This repository serves as a showcase of **Android architecture, geolocation logic, and payment integration skills**.

---

## 📖 Overview

**FixIt** solves a critical problem for vehicle owners: **breaking down in unfamiliar locations.** Instead of relying on outdated directories, FixIt uses **Location-Based Services (LBS)** to connect stranded users with the nearest verified mechanics in real-time. It handles the entire lifecycle of the service—from real-time discovery and tracking to automated billing and secure payments.

### 🎯 The Solution
* **For Users:** Instant access to help within a 10km radius.
* **For Mechanics:** A platform to manage tasks, verify identity (KYC), and receive payments.

---

## 📱 Application Preview

<table align="center">
  <tr>
    <td align="center" width="33%"><b>📍 Real-Time Map</b></td>
    <td align="center" width="33%"><b>🔧 Mechanic Profile</b></td>
    <td align="center" width="33%"><b>💳 Secure Payment</b></td>
  </tr>
  <tr>
    <td><img src="screenshots/map_view.png" width="100%" alt="Map View"/></td>
    <td><img src="screenshots/profile.png" width="100%" alt="Profile View"/></td>
    <td><img src="screenshots/payment.png" width="100%" alt="Payment View"/></td>
  </tr>
</table>

---

## ✨ Key Features

### 🗺️ Advanced Geolocation Engine
* **GeoHash Algorithms:** Efficiently queries Firestore to find mechanics strictly within a **10km radius**.
* **Live Tracking:** Utilizes **Google Maps SDK** to render mechanic movement with real-time updates (100m threshold optimization).
* **Distance Matrix:** Automatically calculates arrival time and distance.

### 🔐 Trust & Security Architecture
* **KYC Verification System:** Mechanics must upload **National ID (NIC)** and **selfie** to Firebase Storage. Admin approval is required for the "Verified" badge.
* **OTP Handshake:** To prevent fraud, the task timer *cannot start* until the mechanic enters a unique **6-digit code** provided by the user upon arrival.

### 💸 Dynamic Billing & Payments
* **Automated Cost Engine:**
    > Final Cost = (Duration_Hours × Hourly_Rate)
    
    *Logic handles minimum 1-hour charge and minute-by-minute billing thereafter.*
* **Payment Gateway:** Integrated **PayHere SDK** for seamless credit/debit card processing.

---

## 🛠️ Tech Stack & Dependencies

This project is built using a **native Android** approach, ensuring high performance and smooth animations.

| Category | Technology | Usage |
| :--- | :--- | :--- |
| **Core** | ![Java](https://img.shields.io/badge/-Java-ED8B00?logo=openjdk&logoColor=white) | Main application logic |
| **UI** | ![Android XML](https://img.shields.io/badge/-XML-3DDC84?logo=android&logoColor=white) | Layouts and Material Design components |
| **Database** | ![Firebase Firestore](https://img.shields.io/badge/-Firestore-FFCA28?logo=firebase&logoColor=black) | Real-time NoSQL data sync |
| **Auth** | ![Firebase Auth](https://img.shields.io/badge/-Authentication-FFCA28?logo=firebase&logoColor=black) | User/Mechanic sessions |
| **Maps** | ![Google Maps](https://img.shields.io/badge/-Google_Maps-4285F4?logo=google-maps&logoColor=white) | Rendering and Directions API |
| **Network** | ![OkHttp](https://img.shields.io/badge/-OkHttp-000000?logo=square&logoColor=white) | Handling HTTP requests |
| **Charts** | ![MPAndroidChart](https://img.shields.io/badge/-MPAndroidChart-FF5722) | Visualizing earnings and history |

---

## ⚙️ Setup & Installation

Since the backend keys are decommissioned, you must provide your own API keys to build the project successfully.

1.  **Clone the Repository**
    ```bash
    git clone https://github.com/DilanHansaja/FixIt-Mechanic-Finder-App.git
    ```

2.  **Configure Secrets**
    Create a `local.properties` file in the root directory and add your keys:
    ```properties
    GOOGLE_API_KEY=your_google_maps_key
    PAYHERE_MERCHANT_ID=your_merchant_id
    ```

3.  **Build & Run**
    Open the project in **Android Studio (Ladybug or newer)** and sync Gradle.

---

## 👨‍💻 Author

**Software Engineering Student** | *Open Source Portfolio Showcase*

---
