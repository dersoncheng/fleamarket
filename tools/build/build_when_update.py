
#!/usr/bin/python
#-*-coding:utf8-*-

__author__ = "Lichuan Shang"
__email__ = "waterdudu@wandoujia.com"
__copyright__   = "Copyright 2013 Wandou Labs"

import os
from build_util import *

def is_need_to_build(branch_name, base_path):
	branch_commit_file = get_branch_commit_id_path(branch_name)
	current_commit_id = get_current_commit_id(base_path)
	print '***  current commit id *** %s\n' % current_commit_id

	if os.path.exists(branch_commit_file):
		last_commit_id    = read_commit_id(branch_commit_file)
		print '*** last commid id *** %s\n' % last_commit_id
		print '-' * 20, '\nlast : ', last_commit_id, '\ncurr : ', current_commit_id
		if last_commit_id == current_commit_id:
			return False
		else:
			return True
	else:
		print ' *** file %s not exists' % branch_commit_file
		return True

def get_branch_commit_id_path(branch_name):
	path_prefix = create_directory('commits')
	branch_name_list = branch_name.split('/')
	branch_name_list.append('last-commit-id')
	branch_commit_id_path = os.path.join(path_prefix, '_'.join(branch_name_list))
	return branch_commit_id_path

def build_p3(build_script_path, branch_name, channel, mailto):
	# build_script_path(default) : /home/android/android_build/phoenix_pack/phoenix_super_pack 
	real_build_cmd = 'bash -x backup_release.sh -b %s -c %s -m %s' % (branch_name, channel, mailto)
	build_cmds = ['cd %s' % build_script_path, 'git pull', real_build_cmd]
	out = ''
	error = ''

	for cmd in build_cmds:
		print '>' * 9, 'now executing : %s ' % cmd
		(success, cmd_out, cmd_error ) = exec_cmd(cmd, build_script_path)
		print '>' * 9, 'end executing %s' % cmd
		if cmd_out:
			out += cmd_out
		if cmd_error:
			error += cmd_error

		if not success:
			subject = '[phoenix build failed]'
			send_mail(subject, out + '\n' + error)
			# send mail
			return False
	return True

def main():
	if len(sys.argv) < 5:
		print 'args too few, expected 5 args, %d given' % len(sys.argv)
		return

	branch_name       = sys.argv[1]
	channel           = sys.argv[2]
	mailto            = sys.argv[3]
	build_script_path = sys.argv[4]

	p3_root_path = os.path.abspath('../..')
	print 'p3 root path : ', p3_root_path

	init_git(p3_root_path)

	build_need = is_need_to_build(branch_name, p3_root_path)
	if build_need:
		print '>>>.....................build........................'
		build_sucess = build_p3(build_script_path, branch_name, channel, mailto)
		if not build_sucess:
			exit(-1)
			return
		branch_commit_file = get_branch_commit_id_path(branch_name)
		current_commit_id = get_current_commit_id(p3_root_path)
		write_file(branch_commit_file, current_commit_id)
	else:
		print 'nothing to be done!'
	print 'end of build'

if __name__ == '__main__':
	main()


