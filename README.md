### KunKunChat
### ——一款基于`Moonshot AI`的安卓智能聊天机器人APP
#### 使用说明
**操作**\
再使用软件之前请先配置API_KEY，配置方法如下：\
登录`Moonshot AI`官网，注册账号，在用户中心创建自己的`API_KEY`，在软件中点击`设置`图标，将`API_KEY`粘贴到输入框中，点击`确认`即可。\
https://platform.moonshot.cn/console
<img src="app/src/main/res/drawable/reset.png" width="24px"> 点击可重置对话；
<img src="app/src/main/res/drawable/other.png" width="24px"> 点击可进入应用介绍页面，长按可清理当前聊天记录。\
**注意事项**\
由于接口问题，问题复杂时响应可能会较慢，请耐心等待，超时将有提示信息

#### 技术原理
##### 关键词 `MoonshotAI`、`Okhttp`、`FastJSON`、`Markwon`、`CountDownTimer`、`Adapter`、`Bean`
使用`Okhttp`向`Moonshot AI`发送API请求,收到响应后，使用`FastJSON`解析出有效信息，将信息装载至消息列表中，并使用适配器的方式使用`Markwon`进行渲染，最后显示在`Activity`。

#### 更新日志
##### V1.4 `2024-02-29 19:52:35`
①新增用户自定义API_KEY功能

##### V1.3 `2024-02-23 16:26:05`
①延长超时判断时间，降低超时错误发生的概率\
②增加等待响应倒计时，助于提醒用户。使用`CountDownTimer`实现\
③增加提醒消息数，使KunKun更人性化以及便于找BUG\
④新增，等待时段不可点击发送消息\
⑤新增，空消息不可发送，设置提示
