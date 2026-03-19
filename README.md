# AAOS Maps Virtual Display Multi-Instance Architecture

This project demonstrates how to inject and project native Google Automotive Services (GAS) Maps onto a `VirtualDisplay` via a `SurfaceView` in Android Automotive OS 14. 

## The Core Technical Challenge: The `singleTask` Limitation
On Android 14, the Android `ActivityTaskManagerService` introduces strict multi-display task enforcement. The core Google Maps APK (`com.google.android.apps.maps`) explicitly hardcodes its primary activities to `launchMode=LAUNCH_SINGLE_TASK`. 

When an application attempts to launch a `singleTask` activity (like `MapsActivity` or `LimitedMapsActivity`) onto a Virtual Display, Android 14 will search the entire system for an existing instance of that activity. If it is already running on another screen (e.g., the car launcher or the system app drawer), Android will forcefully rip the map off the original display and teleport the task to the new Virtual Display, resulting in a blank/red screen on the original display and a complete loss of dual-screen capability. 

**Note on Android 12:** Legacy systems running Android 12 utilized a looser window management framework that occasionally permitted duplicate tasks on separate logical displays. This loophole was completely closed by Google in Android 13/14 to enforce seamless navigation handoff between screens.

## Supported GAS Map Activities
To prevent focus-stealing collisions across multiple displays, the system must utilize three distinct Google Maps activities, assigning exactly one unique class to each display:

1. **`MapsActivity`** (`com.google.android.maps.MapsActivity`): The primary full-screen navigation container. Designed for the system app menu.
2. **`LimitedMapsActivity`** (`com.google.android.maps.LimitedMapsActivity`): The restricted turn-by-turn container. Designed exclusively for the Home Screen / Launcher dashboard.
3. **`EmbeddedClusterActivity`** (`com.google.android.apps.gmm.car.embedded.auxiliarymap.EmbeddedClusterActivity`): The auxiliary map container specifically built by Google to be projected onto secondary isolated screens (like Virtual Displays or instrument clusters).

## Architecture Solutions

### Solution 1: The Three-Tier Architecture (Recommended for GAS Native)
By targeting `EmbeddedClusterActivity` for the Virtual Display, the map successfully renders without stealing focus from the launcher's `LimitedMapsActivity` or the system's `MapsActivity`. 

**Limitation**: `EmbeddedClusterActivity` is hardcoded internally by Google to force a dark "satellite/cluster" presentation mode. It natively ignores overriding intent styling parameters like `geo:0,0?t=m`. If native GAS map styling is acceptable, this is the most stable and isolated approach.

### Solution 2: The Document Task Override Trick
In certain A14 environments, sending a `geo:` location Intent coupled explicitly with `FLAG_ACTIVITY_NEW_DOCUMENT` to `LimitedMapsActivity` can trick the Android Document Task Manager into bypassing the standard `singleTask` restriction, forcing a completely new document task window to render in a standard white street-view mode.

```kotlin
val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("geo:0,0?q=location")).apply {
    setClassName("com.google.android.apps.maps", "com.google.android.maps.LimitedMapsActivity")
    addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
}
```
*Verification:* You can verify if the system successfully spawned two parallel instances by running:
`adb shell dumpsys activity activities | grep "ActivityRecord.*LimitedMapsActivity"`
If two distinct memory hashes are returned, the system has successfully cloned the map without crashing the launcher. If only one is returned, the system stole the launcher's task in two steps.

### Solution 3: The Maps SDK Alternative (For Absolute Control)
If the project strictly requires programmatic control over the map style (e.g., forcing a standard street-view roadmap), explicit destination camera bounding independent of the vehicle's navigation state, and absolutely zero risk of focus-stealing cross-app:

Do not use Intent Activity Injection. Instead, abandon the Virtual Display and integrate the **Google Maps SDK for Android**.
Replace the `VirtualDisplay` logic entirely by dropping a `SupportMapFragment` into the app hierarchy. The SDK renders the map locally within the app's memory process, naturally sidestepping all global `singleTask` window constraints imposed by the pre-compiled native GAS application.

---

## Technical Deep-Dive: Why This Happens on Android 14

### 1. The `singleTask` Native Restriction
All three primary mapping activities provided by Google Automotive Services are explicitly hardcoded to operate as strict singletons. Querying the Android Package Manager (`adb shell cmd package resolve-activity`) on the internal GAS binaries yields the following identical launch constraints:

```text
com.google.android.maps.MapsActivity:
  launchMode=LAUNCH_SINGLE_TASK 

com.google.android.maps.LimitedMapsActivity:
  launchMode=LAUNCH_SINGLE_TASK 

com.google.android.apps.gmm.car.embedded.auxiliarymap.EmbeddedClusterActivity:
  launchMode=LAUNCH_SINGLE_TASK 
```

Because Android 14's `ActivityTaskManager` strictly enforces this across all physiological and virtual displays, it guarantees that only **one instance of each class** can exist in the system memory. Launching `MapsActivity` on a Virtual Display forces the OS to locate the active dashboard instance, detach its window binding, and teleport it to the new display—causing the original screen to turn red or blank.

### 2. Android 12 vs Android 14 Architecture Shift
- **Android 12 (Loose Multi-Display Mode)**: Previously, the window manager allowed a "phantom" instance of a `singleTask` Activity to spin up on secondary logical displays without tearing down the dashboard instance.
- **Android 14 (Strict Multi-Display Windowing)**: The system was completely rewritten to support `ActivityEmbedding` and `CarTaskViewController`. Android 14 mathematically guarantees strict Task limits to support continuous "Seamless Navigation Handover". The OS now ruthlessly prevents duplicate tasks from bypassing the `singleTask` manifest declarations.

### 3. Intent Limitations on `EmbeddedClusterActivity`
While `EmbeddedClusterActivity` is the safest native injection point, it ignores location and styling intents (such as `geo:0,0?t=m`). 
This occurs because the activity is inherently designed as an "Automotive Mirror" for the instrument cluster. It actively strips external parameters because it statically binds directly to two internal systems:
1. The vehicle's physical `LocationManager`.
2. The active navigation session running inside the primary `MapsActivity`.
If a third-party app attempts to programmatically force a roadmap style or jump to a coordinate, the `EmbeddedClusterActivity` actively overrides it to default cluster styling and snaps back to the vehicle GPS puck.

### 4. Mathematical Verification of Task Creation
To conclusively prove that a secondary map task has spun up (and hasn't just stolen the `ActivityRecord` from the dashboard), developers must query the system memory hashes for the given component.

Run the following command while the maps are visible:
`adb shell dumpsys activity activities | grep "ActivityRecord.*LimitedMapsActivity"`

- **Safe State (Two Identifiers)**: `ActivityRecord{a1b2c3d...}` and `ActivityRecord{x9y8z7w...}` indicates that the Document Task Override successfully coerced the Activity Manager into parallelizing two distinct instances in separate application tasks.
- **Unsafe State (One Identifier)**: If only one hash is present, the map has been stolen.

### 5. Why the Google Maps SDK is the Ultimate Fix
The Google Maps SDK (`com.google.android.gms.maps.SupportMapFragment`) avoids all these constraints because it does not rely on Android's `ActivityTaskManager` to route proprietary pre-compiled activities. 
- It is instantiated strictly within the host application's memory heap (`com.example.aaosmaptest`).
- It allows absolute programmatic control (`googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL`).
- It is completely insulated from system navigation, meaning it guarantees zero red screens or focus stealing across complex automotive UI layouts.
