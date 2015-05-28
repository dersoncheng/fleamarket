#! /bin/sh

# 获得shell文件路径
TOOL_DIR=$(cd "$(dirname "$0")"; pwd)

# 获得完整apk路径
APK_PATH=$1

# 检验参数的合法性
if [ x"$APK_PATH" == x ];then
    echo "\n\t用法："
    echo "\tapkmethod your_apk_path "
    echo "\n\t例："
    echo "\tapkmethod phoenix.apk\n"
    exit 1;
fi

# 创建一个临时目录，来解压这个apk文件
rm -rf apk_temp
mkdir apk_temp
cp $APK_PATH apk_temp/
cd apk_temp

# 获得apk的名称
APK_NAME="$(basename *.apk)"

# 重命名为zip
mv $APK_NAME $APK_NAME.zip

# 解压apk，得到classes.dex包
unzip -x $APK_NAME.zip > /dev/null

# 计算dex中的method数量
METHOD_COUNT=`cat classes.dex | head -c 92 | tail -c 4 | hexdump -e '1/4 "%d\n"'`

# 输出method数量
echo $METHOD_COUNT

# 删除无用目录
cd .. && rm -rf apk_temp
