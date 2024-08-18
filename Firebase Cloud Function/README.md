# Firebase Cloud Function
### 佈署Cloud Function(進入cmd命令提示字元)
#### 1.安裝node.js和npm
#### 2.安裝Firebase CLI
```
npm install -g firebase-tools
```
#### 3.login
```
firebase login
```
#### 4.init
```
firebase init
```
#### 5.選擇功能(選擇Functions)
![image](README_image/choose_functions.png)
#### 6.選擇要用哪個project
![image](README_image/choose_project.png)
#### 7.語言選擇python
![image](README_image/choose_language.png)
#### 8.按yes直到結束(裝完後此時就可以開始寫要佈署上functions的code)
#### 寫完要佈署時，先進入該資料夾(預設資料夾名functions)
```
cd functions
```
#### 佈署
```
firebase deploy
```
# LINE
#### 1.登入LINE Developers
#### 2.創建一個頻道
![image](README_image/channels.png)
#### 3.前往Basic settings並滑到底部取得user id
![image](README_image/basic_settings.png)
![image](README_image/user_id.png)
