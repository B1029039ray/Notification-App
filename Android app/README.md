# Android Studio說明
### 1.取得google-services.json，放入app目錄底下

![image](README_image/google-services.png) 

### 2.build-gradle.kts增加firebase dependencies
build-gradle.kts(Notification app)
```kotlin
id("com.google.gms.google-services") version "4.4.2" apply false
```
build-gradle.kts(app)
```kotlin
implementation("com.google.firebase:firebase-firestore-ktx:23.0.3")
implementation("com.google.firebase:firebase-auth-ktx:21.0.3")
implementation("com.google.firebase:firebase-messaging")
implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
implementation("com.google.firebase:firebase-analytics")
```
dependencies外再增加一行
```kotlin
apply(plugin = "com.google.gms.google-services")
```
### 3.AndroidManifest.xml增加 messaging service 和 permission
```kotlin
        <service
            android:name=".MyFirebaseMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
```
```kotlin
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```
### 4.MainActivity.kt
檢查app是否已獲得發送通知的權限
```kotlin
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 及更高版本
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
```
