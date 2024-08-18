# flask
由於flask運行時的url只會在本地端，需要使用ngrok或佈署在網上來產生公開url，此次測試採用的是ngrok
### ngrok
#### 1.登入ngrok
#### 2.下載ngrok
![image](README_image/download_ngrok.png)
#### 3.將ngrok解壓縮並移至flask目錄底下
#### 4.點開ngrok.exe
#### 5.驗證你的ngrok帳號
```
ngrok config add-authtoken $YOUR_AUTHTOKEN
```
![image](README_image/ngrok_authtoken.png)
#### 6.啟動ngrok(一般的flask url是127.0.0.1:5000)
```
ngrok http http://127.0.0.1:5000
```
#### 7.取得ngrok的公開url
![image](README_image/ngrok_url.png)
