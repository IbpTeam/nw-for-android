##  nw-android
### Introduction
The project is nw for android.
It is base on content_shell of chromium (42.0.2272.76)and nodejs of nw12(0.12.0).
You can use eclipse or android studio to compile the project.
[nw.js](https://github.com/nwjs/nw.js)

the project files is collected from [chromium](git@124.16.141.145:android/runtime-chromium.git)


### use eclipse to build
Now , we use `eclipse **3.8.1**` to build the project on **mint 17 64bits（ubuntu 14.04 64bits）**，

#### 1、install eclipse

ubuntu running：
```
sudo apt-get install eclipse
```
#### 2、install ADT 23.0.0 or update version

* [installing-adt-online](http://developer.android.com/intl/zh-cn/sdk/installing/installing-adt.html)。

install-ADT-offline：

   (1) download adt v23.0.0 or updated version

   (2) open eclipse，Help-->Install New software...-->Add（choise adt path）

####  3、 set SDK

download SDK and set SDK path.
the SDK shoould include `android-21（android v5.0）` abi.
click window-->Preference-->android, to set SDK path. 
click window-->Preference-->android-->build, remove  “Skip packaging ..." option.

####  4、 import project
click File-->import-->android-->"existing android code into workspace"
 to choise this project path.

### develop app of android
All development work is in `assets dir`, you only need edit js, css, and html5 file to develop app of android.

First, you should create an index.html file in `assets dir`, that is entry program. and other files should also be putted in `assets dir`. 

Second，put all node modulse in `assets/node_modules dir.

finaly, build the project.



### use Android Studio 

1. set SDK path
click File-->Project Structure-->Android SDK Locatio, to set SDK path。

2. import project 
click File-->New-->Import Project, choise this project 

3. build apk
Build-->Generate Signed APK，
you can create new sign file to sign apk and generate signed apk.
