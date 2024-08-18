# Android Studio說明
### 1.取得google-services.json，放入app目錄底下

![image](README_image/google-services.png) 

### 2.build-gradle.kts增加firebase dependencies
build-gradle.kts(Notification app)
```kotlin
id("com.google.gms.google-services") version "4.4.2" apply false
```
