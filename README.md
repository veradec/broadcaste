# Broadcaste

This Android application serves as a reference implementation for Bluetooth Low Energy (BLE) communication protocols, specifically demonstrating advertising and scanning functionalities. The application is architected using Kotlin and the declarative Jetpack Compose UI toolkit.

## Core Functionalities

*   **BLE Advertising:** Implements GATT server functionality to broadcast service UUIDs, enabling discovery by other BLE-capable devices.
*   **BLE Scanning:** Actively scans for peripheral devices advertising a predefined service UUID.
*   **Device Discovery and Signal Analysis:** Discovered devices are enumerated, displaying their advertised name, unique hardware address, and Received Signal Strength Indicator (RSSI) for proximity analysis.
*   **Connection State Monitoring:** Provides real-time feedback on the connection status for each discovered peripheral.

## Required Permissions

The application's functionality necessitates the following system permissions, which must be granted at runtime:

*   `BLUETOOTH`: For fundamental Bluetooth communication.
*   `BLUETOOTH_ADMIN`: For legacy Bluetooth management tasks.
*   `BLUETOOTH_SCAN`: For discovering nearby BLE devices.
*   `BLUETOOTH_ADVERTISE`: For broadcasting as a BLE peripheral.
*   `BLUETOOTH_CONNECT`: For establishing connections to BLE peripherals.
*   `ACCESS_FINE_LOCATION`
*   `ACCESS_COARSE_LOCATION`

## Build and Deployment

The project is configured for use with Android Studio and the Gradle build system.

1.  Clone the repository:
    ```bash
    git clone https://github.com/veradec/broadcaste.git
    ```
2.  Import the project into Android Studio.
3.  Execute the 'run' configuration to build and deploy the application to a connected Android device or emulator.

## Technology Stack

*   **Primary Language:** Kotlin
*   **UI Framework:** Jetpack Compose
*   **Core Technology:** Bluetooth Low Energy (BLE) API for Android.
