import flask
from flask import Flask, request, jsonify, render_template
import json

app = flask.Flask(__name__)
app.config["DEBUG"] = True

# 全域變數
latest_emotion_index = None

@app.route('/')
def index():
    # 傳遞情緒指數給模板
    return render_template('index.html', emotion_index=latest_emotion_index)

@app.route('/notify', methods=['POST'])
def notify():
    global latest_emotion_index  # 確保可以修改全域變數
    data = request.get_json()
    if not data:
        return jsonify({"error": "Invalid data"}), 400

    # 解析來自 Cloud Function 的資料
    emotion_index = data.get('emotion_index')
    if emotion_index is not None:
        latest_emotion_index = int(emotion_index)  # 更新全域變數
        print(f"Received emotion index: {latest_emotion_index}")
        return jsonify({"message": "Notification received"}), 200
    else:
        return jsonify({"error": "Missing emotion index"}), 400

if __name__ == '__main__':
    app.run()
