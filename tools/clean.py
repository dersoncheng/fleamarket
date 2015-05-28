#!/usr/bin/env python
# -*- coding: utf-8 -*-

from xml.dom.minidom import parse
from operator import itemgetter
from itertools import groupby
import sys
import shutil
import re
import os
import subprocess
import traceback

RES_VALUE_FILES = ['color.xml', 'colors.xml', 'dimen.xml', 'dimens.xml', 'string.xml', 'strings.xml']
INVALID_CH_ARRAY = ['"', '\'','&', '<', '>']

reload(sys)
sys.setdefaultencoding('utf8')


def resource_keys(t, v):
  return [src_resource_key(t, v), res_resource_key(t, v)]

def src_resource_key(t, v):
  return u'%s.%s' % (t, v)

def res_resource_key(t, v):
  return u'@%s/%s' % (t, v)

def wrap_quote(s):
  return u'"%s"' % (s)

def file_to_str(f):
  fp = open(f, 'r')
  data = fp.read()
  fp.close()
  return data.decode('utf-8')

def check_value(v):
  # can not handle string with tag and "
  return v != '' and all(v.find(ch) < 0 for ch in INVALID_CH_ARRAY)
#  return v != '' and v.find('"') < 0 

def handle_files(files, func):
  for f in files:
    print f
    file_str = file_to_str(f)
    file_str = func(file_str)
    fp = open(f, 'w')
    fp.write(file_str.encode('utf-8'))
    fp.close()

class Cleaner(object):
  path = None
  dimens = None
  colors = None
  strings = None

  def __init__(self, path):
    self.path = path
    res_value_files = self.res_value_files()
    self.dimens = self.read_res_value(res_value_files, 'dimen')
    self.colors = self.read_res_value(res_value_files, 'color')
    self.strings = self.read_res_value(res_value_files, 'string')

  def read_res_value(self, res_files, t):
    res_dict = dict()
    for rf in res_files:
      dom = parse(rf)
      for res in dom.getElementsByTagName('resources'):
        for item in res.getElementsByTagName(t):
          name = item.getAttribute('name')
          value = res_dict.get(name, [])
          new_value = ''.join(t.nodeValue for t in item.childNodes if t.nodeType == t.TEXT_NODE)
          value.append((rf, new_value))
          res_dict[name] = value

    return res_dict

  def normalize_dp(self):
    todo_files = self.res_value_files()
    def replace(file_str):
      return file_str.replace('dip</dimen>', 'dp</dimen>')

    handle_files(todo_files, replace)


  def replace(self):
    # replace res files
    todo_files = self.res_files()
    self.inner_replace(todo_files, 'dimen', self.dimens)
    self.inner_replace(todo_files, 'string', self.strings)
    # android:drawable="@color/aa" can't be replaced
    self.inner_replace(todo_files, 'color', self.colors, False)

    
  def inner_replace(self, todo_files, t, res_dict, kv=True):
    kk_dict = self.find_same_value(todo_files, res_dict)
    def replace(file_str):
      for k, v in kk_dict.items(): 
        file_str = file_str.replace(wrap_quote(res_resource_key(t, k)), wrap_quote(res_resource_key(t, v))) 
      if kv:
        for k, v in self.res_candidate_list(res_dict):
          file_str = file_str.replace(wrap_quote(res_resource_key(t, k)), wrap_quote(v)) 
      return file_str
    handle_files(todo_files, replace)

  def find_same_value(self, todo_files, res_dict):
    res = sorted(self.res_candidate_list(res_dict), key=itemgetter(1))
    kk_dict = dict()
    for key, items in groupby(res, itemgetter(1)):
      k_list = [item[0] for item in items]
      kk_dict = dict(kk_dict, **dict(zip(k_list, [k_list[0]] * len(k_list))))
    return kk_dict

  def clean(self):
    check_files = self.res_files() + self.src_files() + self.res_config_files()
    data = ''
    for f in check_files:
      data += file_to_str(f)
    data = re.sub('[\s]*', '', data)
    self.inner_clean(data, 'dimen', self.dimens)
    self.inner_clean(data, 'color', self.colors)
    self.inner_clean(data, 'string', self.strings)

  def inner_clean(self, data, t, res_dict):
#    unused_list = [(k, v) for k, v in res_dict.iteritems() if not self.is_resource_used(data, t, k)]
    unused_list = dict()
    for name, items in res_dict.iteritems():
      if not self.is_resource_used(data, t, name):
        for (f, value) in items:
          name_list = unused_list.get(f, [])
          name_list.append(name)
          unused_list[f] = name_list

    for f, name_list in unused_list.iteritems():
      data = file_to_str(f)
      print f, len(name_list), 'unused'
      for name in name_list:
        index = data.find(wrap_quote(name))
        if index >= 0:
          start = data.rfind('<%s' % (t), 0, index)
          end = data.find('</%s>' % (t), index) + len('</%s>' % (t))
          data = data[:start] + data[end:]
        # pattern = '<%s[^/]*\"%s\"[^/]*</%s>' % (t, name, t);
        # data = re.sub(pattern, '', data)
      fp = open(f, 'w')
      fp.write(data.encode('utf-8'))
      fp.close()

  def is_resource_used(self, data, t, v):
    keys = resource_keys(t, v)
    return any([key in data for key in keys])

  def res_candidate_list(self, res_dict):
    return [(k, v[0][1]) for k, v in res_dict.iteritems() if len(v) == 1 and check_value(v[0][1])]

  def src_files(self):
    all_files = []
    todo_dirs = [d for d in os.listdir(self.path) if d.lower().startswith('src')]
    for todo_dir in todo_dirs:
      for root, dirs, files in os.walk(os.path.join(self.path, todo_dir), True):
        all_files += [os.path.join(root, f) for f in files if f.lower().endswith('.java')]
    return all_files

  def res_files(self):
    all_files = []
    todo_dirs = [d for d in os.listdir(self.path) if d.lower().startswith('res')]
    for todo_dir in todo_dirs:
      for root, dirs, files in os.walk(os.path.join(self.path, todo_dir), True):
        all_files += [os.path.join(root, f) for f in files if f.lower().endswith('.xml')]
    return all_files
  
  def res_value_files(self):
    all_files = []
    todo_dirs = [d for d in os.listdir(self.path) if d.lower().startswith('res')]
    for todo_dir in todo_dirs:
      for root, dirs, files in os.walk(os.path.join(self.path, todo_dir), True):
        all_files += [os.path.join(root, f) for f in files if f.lower() in RES_VALUE_FILES]
    return all_files

  def res_config_files(self):
    return [os.path.join(self.path, f) for f in os.listdir(self.path) if f.lower().endswith('.xml')]

if __name__ == '__main__':
  if len(sys.argv) < 3:
    print 'usage python clean.py replace/clean/nor_res path'
    exit()
  if sys.argv[1] == 'clean':
    cleaner = Cleaner(sys.argv[2])
    cleaner.clean()
  elif sys.argv[1] == 'replace':
    cleaner = Cleaner(sys.argv[2])
    cleaner.replace()
  elif sys.argv[1] == 'nor_res':
    cleaner = Cleaner(sys.argv[2])
    cleaner.normalize_dp()
  elif sys.argv[1] == 'r':
    try:
      cleaner = Cleaner(sys.argv[2])
      cleaner.replace()
      cleaner.clean()
    except:
      traceback.print_exc()
      sys.exit(1)

  sys.exit(0)


