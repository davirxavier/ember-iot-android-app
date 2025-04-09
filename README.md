# 🔥 Ember IoT – Host Your Own IoT Cloud

Ember IoT is a simple proof of concept for a Firebase-hosted IoT cloud designed to work with Arduino-based devices and an Android mobile app. It enables microcontrollers to connect to the cloud, securely sync data, and interact with a mobile interface all while using the Firebase Authentication and Firebase Realtime Database services.

**This project's aim is to enable you to create a simple IoT infrastructure without all the hassle of setting up a server in your home network or in a cloud provider.**

---

## 🔍 What Is Ember IoT?

Ember IoT allows you to:

- Connect Arduino and similar microcontroller devices to the cloud
- Interact with your devices in real-time using an Android app
- Securely authenticate users and devices with Firebase Authentication
- Store and sync your microcontroller state between devices

This project includes an Android application that features device management and a customizable UI for interacting with connected devices. Alternatively, the Ember IoT library can also be used for microcontroller-to-microcontroller communication, allowing devices to communicate directly without the need for the Android app.

---

## ❓ Why Would I Use Firebase for IoT?

### Short Answer:
Firebase’s free Spark plan from Google seems to be suitable for handling small-scale IoT projects — potentially supporting dozens of devices with real-time data sync and built-in user authentication. This enables you to create a simple IoT infrastructure without all the hassle of setting up a server in your home network or in a cloud provider.

### Detailed Answer:

Most free-tier IoT platforms come with major limitations:

- Device limits (e.g., only a few devices without paying)
- Strict quotas on messages, data storage, or connection time
- High costs to unlock even basic scalability

Firebase, on the other hand, offers:

- Real-time sync with low bandwidth
- Simple and secure authentication with support for email and password
- A relatively generous free tier under the Spark plan:
    - 100 simultaneous database connections (each microcontroller and Android device will use 1 connection slot)
    - 1 GB storage (each device will use less than 1kb of storage)
    - 50,000 reads/day, 20,000 writes/day

This could make Firebase a compelling backend for IoT hobbyists, educators, and developers looking for an easy-to-use platform with minimal upfront cost. You have the flexibility to decide exactly how much you need and tailor your setup accordingly, optimizing it as your project grows. This ensures you’re not bound by the limitations of third-party platforms, giving you full control over your project’s scalability and requirements.

Plus, you don't have to enter any credit card info to create a project :)

---

# 🔥 Firebase Setup

See the [firebase project setup tutorial](https://github.com/davirxavier/ember-iot-arduino/blob/main/FIREBASE_SETUP.md).

---

# 📱 App Setup


# Arduino Setup
