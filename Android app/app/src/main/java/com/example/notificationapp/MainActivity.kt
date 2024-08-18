package com.example.notificationapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.notificationapp.ui.theme.NotificationAppTheme
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.ui.Alignment
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {
    companion object {
        private const val REQUEST_CODE_POST_NOTIFICATIONS = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 呼叫通知權限function
        checkNotificationPermission()

        setContent {
            NotificationAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }

    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 及更高版本
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("MainActivity", "Notification permission granted")
            } else {
                Log.d("MainActivity", "Notification permission denied")

            }
        }
}

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

@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    NotificationAppTheme {
        MainContent()
    }
}
