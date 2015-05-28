#!/bin/sh

SCRIPT_DIR="$( cd "$( dirname $0 )" && pwd )"
GIT_ROOT="$(git rev-parse --show-toplevel)"

cd $SCRIPT_DIR
sh build_proto.sh $GIT_ROOT/packages/phoenix/protocol $GIT_ROOT/packages/phoenix/src *.proto
sh build_proto.sh $GIT_ROOT/framework/log/protocol $GIT_ROOT/framework/log/src *.proto