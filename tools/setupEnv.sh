#!/bin/sh

#CURRENT_PATH=${PWD}
#SHELL_PATH=${}/`dirname $0`
GIT_ROOT="$(git rev-parse --show-toplevel)"
CODE_STYLE_DIR=$GIT_ROOT/tools/android_code_style
cp $CODE_STYLE_DIR/code-style-checker.py $GIT_ROOT/.git/hooks/pre-commit
chmod u+x,g+x,o+x $GIT_ROOT/.git/hooks/pre-commit

cp $GIT_ROOT/tools/commit-msg.py $GIT_ROOT/.git/hooks/commit-msg
chmod 755 $GIT_ROOT/.git/hooks/commit-msg

cp $GIT_ROOT/tools/prepare-commit-msg.py $GIT_ROOT/.git/hooks/prepare-commit-msg
chmod 755 $GIT_ROOT/.git/hooks/prepare-commit-msg
