# 🌟 Smart Rating Bar (Android)

A beautiful, customizable, and animated rating bar built in Kotlin.  
Designed to bring smooth animations, fun interactions, and a clean look to your Android apps!

https://github.com/Nimisha-Gajera/SmartRatingBar

---

## 🎬 Demo

![Demo](demo.gif)
![Image](https://github.com/user-attachments/assets/a688e729-8ccb-42b5-9a0b-4919a4c5c207)

---

## ⚙️ Setup (Add SmartRatingBar to your project)

### 1. Add the JitPack repository
In your **project-level `settings.gradle.kts`**, add the JitPack repository at the end of `repositories`:
   ```gradle
   allprojects {
       repositories {
           ...
           maven { url 'https://jitpack.io' }
       }
   }
   ```
### 2. Add the dependency
In your **app-level build.gradle.kts**, add the library dependency:

    ```gradle
   dependencies {
   implementation("com.github.Nimisha-Gajera:SmartRatingBar:Tag")
   }
    ```

---

## ✨ Features

- ⭐ Custom star count, size, spacing
- 🎨 Adjustable fill & empty colors
- ⚡ Smooth animation with custom duration
- 🌈 Flying star animation effect
- 🖼️ Supports custom filled/empty star drawables
- 🛠️ XML & programmatic customization

---

## 🧩 XML Attributes

| Attribute                 | Format    | Description               |
|---------------------------|-----------|---------------------------|
| `starCount`               | integer   | Number of stars           |
| `starSize`                | dimension | Star size                 |
| `starSpacing`             | dimension | Spacing between stars     |
| `starFillColor`           | color     | Filled star color         |
| `starEmptyColor`          | color     | Empty star color          |
| `bounceAnimationDuration` | integer   | Animation speed           |
| `bounceAnimationEnabled`  | boolean   | Enable/disable animation  |
| `starFilled`              | reference | Drawable for filled star  |
| `starEmpty`               | reference | Drawable for empty star   |
| `enableFlyingAnimation`   | boolean   | Enable flying star effect |

---

## 🧰 Usage

### In XML

```xml
<com.library.smart_rating_bar.SmartRatingBar
    android:id="@+id/starRatingBar"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:bounceAnimationDuration="300"
    app:bounceAnimationEnabled="true"
    app:enableFlyingAnimation="false"
    app:starCount="5"
    app:starEmpty="@drawable/ic_unfill_star"
    app:starEmptyColor="@color/outline"
    app:starFillColor="@color/Primary"
    app:starFilled="@drawable/ic_fill_star"
    app:starSize="36dp"
    app:starSpacing="8dp" />

