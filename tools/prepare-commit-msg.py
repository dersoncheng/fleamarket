#!/usr/bin/env python

import sys

def main(argv):
	format = """\n\nBUG=\nTEST=\n"""
	is_need_add_format = True
	with open(argv[0]) as old_log:
		for line in old_log:
			if line.startswith("BUG=") or line.startswith("TEST="):
				is_need_add_format = False
				break
		if is_need_add_format:
			old_log.seek(0)
			for line in old_log:
				format += line
	if is_need_add_format:
		new_log = open(argv[0], "w")
		new_log.write(format)
		new_log.close()

if __name__ == "__main__":
    main(sys.argv[1:])
