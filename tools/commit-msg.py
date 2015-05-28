#!/usr/bin/env python

import sys

def main(argv):
	has_test = 0
	with open(argv[0], "r") as log:
		for line in log:
			line = line.strip()
			if len(line) > 0 and line[0] != '#':
				if len(line) > 5 and line.startswith("TEST="):
					has_test = 1
	if has_test == 1:
		exit(0)
	else:
		print("Commit Failed, need test it!")
		exit(1)

if __name__ == "__main__":
    main(sys.argv[1:])
