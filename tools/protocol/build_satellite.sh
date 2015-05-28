#!/bin/sh

SCRIPT_DIR="$( cd "$( dirname $0 )" && pwd )"
GIT_ROOT="$(git rev-parse --show-toplevel)"

cd $SCRIPT_DIR
sh build_proto.sh $GIT_ROOT/proto/satellite_sdk $GIT_ROOT/framework/satellite_sdk/src *.proto
