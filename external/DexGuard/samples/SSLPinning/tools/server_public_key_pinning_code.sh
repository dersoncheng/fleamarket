#!/bin/sh
#
# Tool to print out code to pin public keys in SSL.

# Account for possibly missing/basic readlink.
# POSIX conformant (dash/ksh/zsh/bash).
TOOL=`readlink -f "$0" 2>/dev/null`
if test "$TOOL" = ''
then
  TOOL=`readlink "$0" 2>/dev/null`
  if test "$TOOL" = ''
  then
    TOOL="$0"
  fi
fi

TOOL_HOME=`dirname "$TOOL"`

java -cp "$TOOL_HOME/dexguard_tools.jar" dexguard.tools.ServerPublicKeyPinningTool "$@"
