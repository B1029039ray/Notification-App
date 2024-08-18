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
使用registerForActivityResult啟動權限請求的launcher
```kotlin
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("MainActivity", "Notification permission granted")
            } else {
                Log.d("MainActivity", "Notification permission denied")

            }
        }
```
構建app的UI
```kotlin
@Composable
fun MainContent(modifier: Modifier = Modifier) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = textFieldValue,
            onValueChange = { textFieldValue = it },
            label = { Text("Enter your emotion index") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            val emotionIndex = textFieldValue.text.toIntOrNull()
            if (emotionIndex != null) {
                uploadEmotionIndex(emotionIndex)
            } else {
                Log.e("MainContent", "Invalid emotion index")
            }
        }) {
            Text("Upload Emotion Index")
        }
    }
}
```
將用戶輸入的情緒指數上傳到Firestore中
```kotlin
fun uploadEmotionIndex(emotionIndex: Int) {
    val db = Firebase.firestore
    val data = hashMapOf("emotionIndex" to emotionIndex)
    db.collection("emotions")
        .add(data)
        .addOnSuccessListener { documentReference ->
            Log.d("MainContent", "DocumentSnapshot added with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.w("MainContent", "Error adding document", e)
        }
}
```
用於預覽UI的呈現
```kotlin
@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    NotificationAppTheme {
        MainContent()
    }
}
```

### 5.MyFirebaseMessagingService.kt
表示MyFirebaseMessagingService 類別繼承自 FirebaseMessagingService，用於處理來自FCM 的訊息。允許應用程式接收boardcast通知，並處理相關的訊息和令牌。
```kotlin
class MyFirebaseMessagingService : FirebaseMessagingService() {
```
訂閱主題：使用FirebaseMessaging.getInstance().subscribeToTopic("emotionAlerts") 訂閱一個名為 "emotionAlerts" 的主題，以此接收來自該主題的推播通知。
```kotlin
    override fun onCreate() {
        super.onCreate()

        // 訂閱主題
        FirebaseMessaging.getInstance().subscribeToTopic("emotionAlerts")
            .addOnCompleteListener { task ->
                val msg = if (task.isSuccessful) "Subscribed" else "Subscription failed"
                Log.d(TAG, msg)
            }
    }
```
當接收到FCM消息時，檢查有無data payload，並將emotion_index提取出透過sendNotification發送消息
```kotlin
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        // 檢查消息是否有data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            // 從data payload提取訊息
            val emotionIndex = remoteMessage.data["emotion_index"]

            // 處理data payload
            emotionIndex?.let {
                sendNotification("情绪指數警告", "情绪指數: $it")
            } ?: Log.d(TAG, "No emotion_index found in data payload")
        } else {
            Log.d(TAG, "No data payload received")
        }
    }
```
FCM分配給app新的令牌時將其保存到本地端(暫未用到)
```kotlin
override fun onNewToken(token: String) {
        super.onNewToken(token)

        // 處理新令牌
        Log.d(TAG, "New FCM registration token: $token")

        // 將令牌保存至本地端
        saveTokenToLocalStorage(token)
    }

    private fun saveTokenToLocalStorage(token: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("fcm_token", token)
        editor.apply()
    }
```
設定點擊行為:建立 Intent 和 PendingIntent：點擊通知後將開啟 MainActivity。 PendingIntent 用於包裹 Intent，允許將其傳遞給通知系統。
建立通知: 設定通知的圖示、標題、內容、聲音以及點擊行為(點擊前往app)。
建立通知通道：通知必須綁定到一個通道。創建一個名為emotionAlerts 的通知通道。重要性等級為 IMPORTANCE_HIGH，表示通知會以響鈴或振動的方式打斷用戶。
發送通知:透過 notificationManager.notify 發送通知。
```kotlin
    private fun sendNotification(title: String?, messageBody: String?) {
        // 點擊通知後打開Activity
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // 通知聲音
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // 構建通知
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // ICON
            .setContentTitle(title ?: "情绪指數警告")
            .setContentText(messageBody ?: "情绪指數超過設定值")
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 設置通知通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Emotion Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // 發送通知
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
```
### *注意`FirebaseMessaging.getInstance().subscribeToTopic("emotionAlerts")`的topic需要與Firebase的topic名稱相同!
