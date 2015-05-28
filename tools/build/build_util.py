#!/usr/bin/python
#-*-coding:utf8-*-

import subprocess
import os
import sys
import shutil
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from email.mime.image import MIMEImage

def exec_cmd(cmd, base_path, success_code = 0):
	pipe = subprocess.Popen(cmd, shell=True, cwd=base_path,stdout = subprocess.PIPE,stderr = subprocess.PIPE )
	success = True
	(out, error) = pipe.communicate()
	print 'out:', out
	if len(error) > 0:
		print 'error:', error
	return_code = pipe.wait()

	if return_code != success_code:
		print 'Error executing cmd: %s!!\nPlease check!' % cmd
		success = False

	return (success, out, error)

def get_current_commit_id(base_path):
	(sucess,current_commit_id,error) = exec_cmd('git log -1 --pretty=format:%H', base_path)
	print 'current commid id : ', current_commit_id
	return current_commit_id

def exec_git_cmd(cmd, base_path):
	git_cmd = '%s %s' % ('git', cmd)
	print 'git cmd:', git_cmd
	return exec_cmd(git_cmd, base_path)

def init_git(base_path, remote = 'origin', branch = 'master'):
	cmds = ['reset --hard', 'fetch %s' % remote, 'checkout %s' % branch, 'rebase %s/%s' % (remote, branch)]
	for cmd in cmds:
		exec_git_cmd(cmd, base_path)

def remove_if_exist(path):
	if os.path.exists(path):
		if os.path.isfile(path):
			os.remove(path)
		elif os.path.isdir(path):
			shutil.rmtree(path)

def write_file(path, content):
	f = open(path, 'w')
	f.write(content)
	f.close()

def create_directory(path):
	if not os.path.exists(path):
		os.mkdir(path)
	return path

def read_commit_id(path):
	f = open(path, 'r')
	commit_id = f.readline()
	f.close()
	return commit_id

def current_dir():
	return os.path.dirname(os.path.abspath(__file__))

def send_mail(subject, content):
	# send
	mail_to = [
				'waterdudu@wandoujia.com',
				'songfeifei@wandoujia.com',
				'testing@wandoujia.com', 
				'yhp@wandouja.com',
			]
	email_from = "build@wandoujia.com"
	email_msg = MIMEMultipart()
	email_msg['Subject'] = subject
	email_msg['From'] = email_from
	email_msg['To'] = ','.join(mail_to)
	email_msg.attach(MIMEText(content))
	try:
		email_smtp = smtplib.SMTP("smtp.gmail.com", 587)
		email_smtp.ehlo()
		email_smtp.starttls()
		email_smtp.ehlo()
		email_smtp.login(email_from, "shadouxing")
		email_smtp.sendmail(email_from, mail_to, email_msg.as_string())
		email_smtp.close()
		email_smtp.quit()
	except Exception, e:
		print str(e)

	# test
	print 'email has send to %s' % mail_to
