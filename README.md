# Order Management & POS Printing System
**Author**: Mark Ogari (mbonke33@gmail.com)

---

## Time Spent & Log

* **Monday, Aug 31, 2026 (3 hrs)**: Read through all specifications, requirements, and gathered initial development resources.
* **Thursday, Sep 3, 2026 (4 hrs)**: Started development, focusing primarily on building and structuring the Material 3 UI.
* **Friday, Sep 4 & Sunday, Sep 6, 2026 (4 hrs)**: Implemented business logic and connected backend domain logic to the UI components.
* **Monday, Sep 7, 2026 (4 hrs)**: Polished UI and codebase, removed boilerplate/remnants, cleaned up code, ran full test suites, and authored project documentation.

---

## How It Works

### 1. Accurate Money Handling
* **Value Classes (`Long`)**: We store prices in cents using Kotlin value classes. This avoids floating-point rounding errors completely while keeping memory usage at zero.

### 2. Menu Catalog (`MenuItem`)
* **Data Classes**: Each menu item is represented by an immutable data class. Instead of modifying objects directly.

### 3. Taking Orders (`TakeOrderScreen`)
* **Unified Quantity Logic**: Instead of writing separate functions for adding and removing items, a simple `+1` / `-1` delta updates the cart state. Built using modern Material 3 UI components.

### 4. Cart Snapshots & Safe Discounts
* **`CartSnapshot`**: Freezes the cart state when an order is placed. If a manager updates prices in the background, the active order price stays accurate.
* **`Discount` (Sealed Class)**: Handles percentage and fixed discounts safely. The compiler checks every case, keeping the app crash-proof.


---

## Receipt Printing Architecture

Printing is broken into three simple steps:

1. **`ReceiptData`**: Gathers what needs to be printed.
2. **`ReceiptRenderer`**: Formats the layout into line-by-line printing instructions.
3. **`BluetoothPrinterService`**: Converts those instructions into raw bytes for the thermal printer.

---

## Testing & Hardware Notice

* **Logic & Unit Tests**: All calculations, formatting logic, and unit tests pass cleanly.
* **Hardware Connection**: Physical Bluetooth printer connections and statuses could not be tested on an actual device due to missing hardware. However, the implementation uses proven code from previous projects, ensuring it will connect and function correctly once configured with a physical printer. Unit tests fully verify the layout rendering logic.

---

## Build & Installation

### Prerequisites
* **Android Studio**: Ladybug (or newer recommended)
* **JDK**: Version 17 or 11 (Project configured for Java 11 compatibility)
* **Android SDK**: API level 24 (min) to 37 (target)

### Build Instructions
1. **Open Project**: Import the project into Android Studio.
2. **Gradle Sync**: Perform a Gradle sync to download all necessary libraries and modules (`:app` and `:core`).
3. **Build APK**: Use `Build > Build Bundle(s) / APK(s) > Build APK(s)` in the IDE.
   * *Command Line*: Run `./gradlew assembleDebug` to generate the debug APK.

### Installation Instructions
1. **Enable Debugging**: Ensure USB Debugging is enabled on your Android device.
2. **Run App**: Click the **Run** button in Android Studio to automatically build, install, and launch the app on your connected device or emulator.
3. **Manual Sideload**: Locating the APK in `app/build/outputs/apk/debug/app-debug.apk`, transfer it to your device and open it to install.
