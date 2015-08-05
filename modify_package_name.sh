#!/bin/bash

#please input package name 
if [ $# -ne 1 ] ; then
    echo "please input package name, such as com.iscas.nw"
    exit
fi
#new package name
new_package_name=$1

origin_package_name=`grep "package=" AndroidManifest.xml | sed "s/.*package=\"\(.*\)\">/\1/g"`

new_apk_folder=`echo $new_package_name | sed 's#\.#/#g'`
origin_apk_folder=`echo $origin_package_name | sed "s#\.#/#g"`

if [ "$new_apk_folder" == "$origin_apk_folder" ];then
    exit
fi

mkdir -p src/$new_apk_folder
cp src/$origin_apk_folder/* src/$new_apk_folder -fr
rm src/$origin_apk_folder -fr

find src/$new_apk_folder -name "*.java" | xargs sed -i "s#package $origin_package_name;#package $new_package_name;#g"

# modify package name in AndroidManifest.xml
sed -i "s#package=\".*\"#package=\"$new_package_name\"#g" AndroidManifest.xml

# modify import R
files=`find . -name "*.java"`
for file in $files;do
    sed -i "s#import $origin_package_name\.R;#import $new_package_name.R;#g" $file
done

