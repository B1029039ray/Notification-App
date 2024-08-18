import firebase_admin
from firebase_admin import credentials, firestore, messaging
import requests
import os
from firebase_functions.firestore_fn import (
    on_document_written,
    Event,
    Change,
    DocumentSnapshot,
)

# 初始化 Firebase admin SDK
cred_path = os.getenv('GOOGLE_APPLICATION_CREDENTIALS', '<path/to/serviceAccountKey.json>')
cred = credentials.Certificate(cred_path)
firebase_admin.initialize_app(cred)

db = firestore.client()


# Cloud Function
def send_notifications(emotion_index):
    if emotion_index is not None and emotion_index > 80:
        #發送到 LINE chatBOT
        line_token = '<Channel access token>'
        to_user = '<user id>'  # 改為接收消息的用户 ID
        message = {
            "to": to_user,
            "messages": [
                {
                    "type": "text",
                    "text": f"情緒指數超過 80: {emotion_index}"
                }
            ]
        }

        headers = {
            'Content-Type': 'application/json',
            'Authorization': f'Bearer {line_token}'             
        }

        response = requests.post('https://api.line.me/v2/bot/message/push', json=message, headers=headers)
        if response.status_code != 200:
            print(f"Error sending to LINE: {response.text}")
        else:
            print("Successfully sent message to LINE")

        #發送 Firebase Cloud Message 到 App
        fcm_message = messaging.Message(
            data={
                'emotion_index': str(emotion_index),
            },            
            topic='emotionAlerts'
        )
        response = messaging.send(fcm_message)
        print(f'Successfully sent message: {response}')

        # 發送資料到 Flask 應用
        flask_url = '<ngrok url>/notify'
        flask_data = {'emotion_index': emotion_index}
        flask_response = requests.post(flask_url, json=flask_data)

        if flask_response.status_code != 200:
            print(f"Error sending to Flask: {flask_response.text}")
        else:
            print("Successfully sent data to Flask")

# Firestore 偵測(觸發) function
@on_document_written(document="emotions/{docId}")
def on_emotion_written(event: Event[Change[DocumentSnapshot | None]]) -> None:
    document = event.data.after.to_dict() if event.data.after is not None else None
    if document is not None:
        emotion_index = document.get('emotionIndex')
        send_notifications(emotion_index)
