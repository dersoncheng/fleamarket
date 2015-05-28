#!/usr/bin/env python

import os
import os.path
import subprocess
import sys
import StringIO
import commands
import re

git_root = os.popen("git rev-parse --show-toplevel").readlines()[0].strip()
tool_path = git_root + "/tools/android_code_style/"
style_check_command_prefix = "java -jar " + tool_path + "checkstyle/checkstyle-5.6-all.jar -c "\
    + tool_path + "wandoujia_android_checks.xml "
skipped_words = ["Starting audit", "Audit done"]
warning_words = [":\d+:\d+: Missing a Javadoc comment", "File does not end with a newline", "must be private and have accessor methods"]
skipped_dirs = ["libs", "res", "gen", "bin"]

def is_fatal_error(outputLine):
    for warning_word in warning_words:
        if re.search(warning_word, outputLine) is not None:
            return False;
    return True;

def is_target_file(file):
    return is_in_code_dir(file) and file.endswith(".java")

def is_in_code_dir(directory):
    for skipped_dir in skipped_dirs:
        if directory.startswith(skipped_dir):
            return False
    return True

def check_file(file, is_new_file):
    lines_modified = []
    if (not is_new_file):
        lines_modified = check_lines_modified(file).split(" ")
    command = style_check_command_prefix + file
    status,output=commands.getstatusoutput(command)
    if status == 0:
        print("======== PASS =======" + "\n")
        return 0
    stringIO = StringIO.StringIO(output)
    only_warning = True
    for output_line in stringIO.readlines():
        index = output_line.find(git_root)
        if (index >= 0):
            output_line = output_line[index + len(git_root) + 1:]
        has_skipped_word = False
        for skipped_word in skipped_words:
            if output_line.find(skipped_word) >= 0:
                has_skipped_word = True
        if not has_skipped_word:
            line_number = get_line_number(output_line)
            new_or_modified = is_new_file or line_number in lines_modified
            illegal_import_ret = is_illegal_import(output_line)
            print(illegal_import_ret)
            if illegal_import_ret == -1:
                continue
            elif illegal_import_ret == 1:
                print("  FATAL: " + output_line)
                only_warning = False
                continue;
            elif is_fatal_error(output_line) and new_or_modified:
                print("  FATAL: " + output_line)
                only_warning = False
            else:
                print("  WARNING: " + output_line)
    if (only_warning):
        return 1
    else:
        return 2

def check_lines_modified(f):
    cmd = "sh " + tool_path + "/lines_modified.sh " + f;
    line = os.popen(cmd).readline();
    return line.strip();

def get_line_number(line):
    return re.search(r':(\d+):', line).group(1)

def check_commit_files():
    total_result = 0
    for status_name in os.popen("git diff --cached --name-status HEAD").readlines():
        file_name = status_name[1:].strip()
        if (status_name[0] == "D" or not is_target_file(file_name)):
            print("skip file: " + file_name + "\n\n")
        else:
            is_new_file = status_name[0] == "A"
            print("checking file: " + file_name + "\n")
            result = check_file(file_name, is_new_file)
            if (total_result < result):
                total_result = result
    return total_result

def is_illegal_import(line):
    if (line.find("Import from illegal package") < 0):
        return 0;
    if (line.find("android.util.Log") >= 0):
        return 1;
    if (line.find("sun.") >= 0):
        return 1; 
    return -1;

def main():
    result = check_commit_files()
    if (result == 0):
        print("CHECK SUCCESS")
        exit(0)
    elif (result == 1):
        print("CHECK FINISHED, WARNING FOUND")
        exit(0)
    else:
        print("CHECK FAILED")
        exit(1)

if __name__ == "__main__":
    main()
