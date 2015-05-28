#!/bin/sh

SCRIPT_DIR="$( cd "$( dirname $0 )" && pwd )"
GIT_ROOT="$(git rev-parse --show-toplevel)"

cd $SCRIPT_DIR
sh build_proto.sh $GIT_ROOT/proto/sonic $GIT_ROOT/packages/sonic/plugin-apk/src *.proto
