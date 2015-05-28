#!/bin/sh

help()
{
    echo "用法："
    echo "\tbuild_proto.sh proto_file_dir target_source_folder [files]"
    printf "\t%-20s %s\n" "proto_file_dir" ".proto 文件所在文件夹"
    printf "\t%-20s %s\n" "target_source_folder" "目标 source folder，比如 src"
    printf "\t%-20s %s\n" "files" "需要选取的文件，默认为全部 .proto 后缀文件"
    echo "例："
    echo "\tbuild_proto.sh packages/phoenix/protocol/ packages/phoenix/src/"
}

SCRIPT_FILE_NAME=$0
SCRIPT_DIR_NAME="$( cd "$( dirname $SCRIPT_FILE_NAME )" && pwd )"

#handle options
while [ -n $1 ] ; do
	case $1 in
		-h|--help)
			help
			exit 1
			;;
		*)
			break
			;;
	esac
done

PROTO_FILE_DIR="$( cd $1 && pwd )"
TARGET_SOURCE_FOLDER="$( cd $2 && pwd )"
shift 2

while [ $# -gt 0 ]; do
	PROTO_FILES="$PROTO_FILES $1"
	shift 1
done

if [ -z $PROTO_FILES ] ; then
	PROTO_FILES=*.proto
fi

WIRE_JAR_PATH=$SCRIPT_DIR_NAME/wire-compiler-1.3.3-jar-with-dependencies.jar

cd $PROTO_FILE_DIR
java -jar $WIRE_JAR_PATH --proto_path=. --java_out=$TARGET_SOURCE_FOLDER $PROTO_FILES

