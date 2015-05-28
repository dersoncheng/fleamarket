#! /bin/sh

# 获得shell文件路径
TOOL_DIR=$(cd "$(dirname "$0")"; pwd)

# 获得输入的jar文件
JAR_FILE=$1;

# 检验参数的合法性
if [ x"$JAR_FILE" == x ];then
    echo "\n\t用法："
    echo "\tjarmethod your_jar_path"
    echo "\n\t例："
    echo "\tjarmethod gson.jar\n"
    exit 1;
fi

# 这里直接使用dx命令，要求已经提前配置好了环境变量
$TOOL_DIR/lib/dx --dex --output=temp.dex $JAR_FILE > /dev/null

# 计算jar包中的方法数
METHOD_COUNT=`cat temp.dex | head -c 92 | tail -c 4 | hexdump -e '1/4 "%d\n"'`

# 显示结果
echo $METHOD_COUNT

# 删除临时文件temp.dex
rm -f temp.dex
