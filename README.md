<div align="center">

#  Goosketch

**Trace anything, anywhere — right through your camera.**

Goosketch overlays line-art from a photo onto your camera's live feed, so you can trace it directly onto real paper. All processing happens on-device — no cloud, no accounts, no tracking.

[

![Android Build](https://github.com/OWNER/goosketch/actions/workflows/android-build.yml/badge.svg)

](https://github.com/OWNER/goosketch/actions/workflows/android-build.yml)


![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)




![Min SDK](https://img.shields.io/badge/minSdk-24-blue)




![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)




![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)




![License](https://img.shields.io/badge/license-unspecified-lightgrey)



</div>

---

## What it does

Point your camera at a piece of paper, drop a photo on top as a semi-transparent overlay, and trace what you see. Goosketch handles the alignment, the line extraction, and the gesture controls so the overlay behaves like it's actually sitting on the page.

| | |
|---|---|
| 🎥 **Live camera overlay** | A translucent image sits on top of your camera feed in real time |
| ✏️ **On-device edge detection** | Sobel gradient edge detection turns a photo into clean line art — fully offline |
| 🎚️ **Edge threshold slider** | Control how much detail the line extraction picks up |
| 🌗 **Opacity control** | Dial the overlay from barely-there to fully opaque |
| 🔒 **Alignment lock** | Freeze position, scale, and rotation once it's lined up |
| 📐 **Grid overlay** | Optional on-screen grid for judging proportions |
| 🔄 **Flip & reset** | Mirror the overlay horizontally, or reset transform to default |
| 🔦 **Torch toggle** | Built-in flashlight control for low-light tracing |
| 🤏 **Pinch / drag / rotate** | Full gesture control to match the overlay to your paper |
| 🖼️ **Import from gallery** | Trace from any photo already on your device |
| ⭐ **Preset stencils** | Four built-in vector stencils (Goose, Floral, Mandala, Star) — no photo needed |

**View modes:** Line Art · Inverted Stencil · High Contrast · Original

---

## 🧱 Tech stack

- **Kotlin** 2.2.10 + **Jetpack Compose** (Compose BOM 2024.09.00)
- **CameraX** for the live camera feed
- **AGP** 9.1.1 / **Gradle** 9.3.1
- Coil for image loading, Navigation Compose for screen flow

---

<div align="center">

Built with 🪿 using [AI Studio](https://ai.studio)

</div>
