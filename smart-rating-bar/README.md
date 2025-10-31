# 🌟 Smart Rating Bar (Android)

A beautiful, customizable, and animated rating bar built in Kotlin.  
Designed to bring smooth animations, fun interactions, and a clean look to your Android apps!

https://github.com/YourUsername/SmartRatingBar

---

## 🎬 Demo
![Demo](demo.gif)
![Image](https://github.com/user-attachments/assets/a688e729-8ccb-42b5-9a0b-4919a4c5c207)

---

## ✨ Features
- ⭐ Custom star count, size, spacing
- 🎨 Adjustable fill & empty colors
- ⚡ Smooth animation with custom duration
- 🔊 Optional sound feedback
- 🌈 Flying star animation effect
- 🖼️ Supports custom filled/empty star drawables
- 🛠️ XML & programmatic customization

---

## 🧩 XML Attributes

| Attribute | Format | Description |
|------------|---------|-------------|
| `starCount` | integer | Number of stars |
| `starSize` | dimension | Star size |
| `starSpacing` | dimension | Spacing between stars |
| `starFillColor` | color | Filled star color |
| `starEmptyColor` | color | Empty star color |
| `animationDuration` | integer | Animation speed |
| `animationEnabled` | boolean | Enable/disable animation |
| `starFilled` | reference | Drawable for filled star |
| `starEmpty` | reference | Drawable for empty star |
| `enableFlyingAnimation` | boolean | Enable flying star effect |

---

## 🧰 Usage

### In XML
```xml
<com.yourpackage.SmartRatingBar
    android:id="@+id/smartRatingBar"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:starCount="5"
    app:starSize="32dp"
    app:starFillColor="@color/yellow"
    app:animationEnabled="true"
    />
