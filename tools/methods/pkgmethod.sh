#!/bin/bash
function help()
{
    echo "用法："
    echo "\tpkgmethod [-d depth] apk_file [\"package_list\"]"
    printf "\t%-20s %s\n" "-d depth" "设定检索包的深度，默认为3级"
    printf "\t%-20s %s\n" "apk_file" "需要计算方法数的 apk"
    printf "\t%-20s %s\n" "package_list" "指定检索的包名，空格分割，可以为空，代表检索所有包"
    echo "例："
    echo "\tpkgmethod -d 1 phoenix.apk \"com.wandoujia.p4 com.wandoujia.p4.xibaibai\""
}

function get_pkg_depth()
{
    pkg_name=$1
    IFS='.' read -a array <<< "$pkg_name"
    echo "${#array[@]}"
}

function calc_classes_method_count()
{
    target_pkg_path=$1
    target_pkg_name=${target_pkg_path:2}
    target_pkg_name="${target_pkg_name////.}"
    if [ -z $target_pkg_name ] ;then
        target_pkg_name="all"
    fi
    if [ -d $target_pkg_path ] ;then
        # 编译得到以包名命名的dex文件，如：com.baidu.tieba.pb.dex
        java -jar $SMALI_JAR $target_pkg_path/ -o $target_pkg_name.dex
        # 从dex文件中统计方法数
        method_num=`$DEXDUMP_PATH -f $target_pkg_name.dex | grep method_ids_size`
        # 为了得到纯数字部分，把不相干的东西删掉
        method_num=${method_num//method_ids_size/}
        method_num=${method_num//:/}
        method_num=`echo $method_num | sed -e 's/\(^ *\)//' -e 's/\( *$\)//'`
        pkg_depth=`get_pkg_depth $target_pkg_name`
        pkg_length=$[ 10 * $pkg_depth ]
        pkg_length=$[ 50 + $pkg_length ]
        printf "%-${pkg_length}s: %-5d\n" $target_pkg_name $method_num
    fi
}

DEPTH=3

while [ -n $1 ] ;do
    case $1 in
        -h|--help)
            help
            exit 1
            ;;
        -d)
            DEPTH=$2
            shift 2
            ;;
        *)
            break
            ;;
    esac
done

# 得到命令行的所有参数
APK_FILE_PATH=$1
PKG_LIST=$2

# 检验参数的合法性
if [ -z $APK_FILE_PATH ] ;then
    help
    exit 1
fi

if [ -z $PKG_LIST ] ;then
    PKG_LIST="all"
fi

echo "包名："$PKG_LIST

TOOL_DIR=`pwd`

# 以此得到两个jar文件的完整路径
BAKSMALI_JAR=$TOOL_DIR/lib/baksmali-2.0.3.jar
SMALI_JAR=$TOOL_DIR/lib/smali-2.0.3.jar

DEXDUMP_PATH=$TOOL_DIR/lib/dexdump

# 下面要做的事情是：在当前目录下创建临时文件夹，将目标apk文件拷贝进来并解压
# 创建一个临时目录，来解压这个apk文件
TEMP_DIR=$TOOL_DIR/temp
rm -rf $TEMP_DIR
mkdir $TEMP_DIR
cp $APK_FILE_PATH $TEMP_DIR/
echo "创建临时目录成功..."

# 进入 temp 目录
cd $TEMP_DIR
# 获得apk的名称
APK_NAME="$(basename *.apk)"

# 重命名为zip
mv $APK_NAME apk.zip

# 解压apk，得到classes.dex包
unzip -x apk.zip > /dev/null
echo "解压apk文件并提取dex文件成功..."

# 在当前目录下，就可以得到classes.dex文件了
# 接下来要做的事情就是：
# 1、使用baksmali将classes.dex中的class导出（smali文件）
CLASSES_DIR=$TEMP_DIR/classes_dir
java -jar $BAKSMALI_JAR -o $CLASSES_DIR/ classes.dex
echo "反编译dex文件成功..."
echo "开始进行package下方法数量的统计...\n"

# 2、用smali对各个package进行转换：smali to dex
cd $CLASSES_DIR
if [ $PKG_LIST = "all" ] ;then
    for pkg_item in `tree -dfi -L $DEPTH` ;do
        calc_classes_method_count $pkg_item
    done
else
    for pkg_item in $PKG_LIST ;do
        pkg_depth=`get_pkg_depth $pkg_item`
        total_depth=$[ $pkg_depth + $DEPTH ]
        pkg_item=${pkg_item//.//}
        for sub_pkg_item in `tree -dfi -L $total_depth | grep $pkg_item` ;do
            calc_classes_method_count $sub_pkg_item
        done
    done
fi

# 删除临时目录，结束
rm -rf $TEMP_DIR
echo "\n删除临时目录成功，方法数量统计结束！"
