nw-android
=======
android平台下的nw，目前使用的content_shell_apk和nodejs。提供使用eclipse（以及android studio）编译android平台下的nw的功能。

### 使用eclipse编译android应用
目前使用的eclipse版本为**3.8.1**，编译eclipse的系统为**mint 17 64位（ubuntu 14.4 64位）**，

1、安装eclipse
--------
ubuntu 下安装eclipse命令：
```
sudo apt-get install eclipse
```
2、更新ADT为`23.0.0`以上版本
-------
在线安装ADT参考：* [installing-adt](http://developer.android.com/intl/zh-cn/sdk/installing/installing-adt.html)。

离线安装ADT：

   (1) 下载ADT离线安装包（`23.0.0`以上版本）

   (2) 在eclipse中，Help-->Install New software...-->Add（选择安装包进行安装）

3、 配置SDK
------
在安装中选SDK所在目录,下载的SDK要支持`android-21（android 5.0版本）`
点击window-->Preference-->android-->build, 在build sets中去掉选项 “Skip packaging ...（省略）”。

4、 导入项目
------
点击File-->import-->android-->"existing android code into workspace"
选择本项目目录。

### 应用开发
所有的开发工作都是在assets目录下进行的。只需要使用js，css，html5等网页开发技术，就可以实现android应用的开发。

首先，建立index.html作为应用的入口程序（在assert目录下）其他所有的web文件也都放在assets目录下即可。

其次，将依赖的node模块都放在assets/node_modules目录下。

最后，重新打包生成apk文件。


### 使用Android Studio编译android应用
1、安装Android Studio
--------


2、 配置SDK
------
在安装中选SDK所在目录,下载的SDK要支持`android-21（android 5.0版本）`
点击File-->Project Structure-->Android SDK Location，输入sdk路径。

3、 导入项目
------
点集File-->New-->Import Project
选择本项目目录。

4、生成apk
Build-->Generate Signed APK，签名可以使用自己生成的签名，也可以使用项目目录下的签名ibp.key，密码与文件名相同。
