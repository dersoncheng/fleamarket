# -*- coding: cp936 -*-
import os
import time
import random
# these import is for display in graph
# import numpy as np
# import matplotlib
# import matplotlib.pyplot as plt
# import pylab as pl
# import matplotlib.patches

# usage:
# 1.open Profile GPU rendering within Developer option in 4.1~4.2 or set Profile GPU rendering to in adb shell
# 2.run this code
# 3.this code will auto fling current page up or down, until get enough fps data

MAX_FRAME_COUNT = 5000
DRAW_LINE_STEP = 0.1

total_value = 0.0
max_value = 0.0
total_time = 0
time_between_10_16 = 0
time_larger_16 = 0
draw_line_start = 0

top_activity_pid = 0

mem_size_before_swipe = 0

def output_gfxinfo():
    gfxinfo_file = os.popen("adb shell dumpsys gfxinfo " + str(top_activity_pid))
    gfxinfo_str = gfxinfo_file.read()
    # print "gfxinfo_str is: ", gfxinfo_str
    
    execute_index = gfxinfo_str.index("Execute")
    view_hierarchy_index = gfxinfo_str.index("View hierarchy", execute_index, len(gfxinfo_str))
    mun_raw_str = gfxinfo_str[execute_index + len("Execute"): view_hierarchy_index]

    rows_of_num = mun_raw_str.split('\r\n');

    for line in rows_of_num:
        if len(line) > 0:
            each_frame_nums = line.split()

            frame_cost_time = 0.0
            # for num in each_frame_nums:

            global draw_line_start
            draw_line_y_start = 0
    
            for i in range(0, len(each_frame_nums)):
                num = each_frame_nums[i]
                
                if len(num) >= 0:
                    try:
                        float_num = float(num)
                        frame_cost_time += float_num
                        #print "frame coast", frame_cost_time
                        
                        global total_value
                        global total_time
                        global max_value
                        
                        if max_value < frame_cost_time:
                            max_value = frame_cost_time
                        total_value += float_num
                        total_time += 1

                        global time_between_10_16
                        global time_larger_16
                        if frame_cost_time > 16:
                            time_larger_16 += 1
                        elif frame_cost_time > 10:
                            time_between_10_16 += 1

                        drawBar(i, float_num, draw_line_start, DRAW_LINE_STEP, draw_line_y_start)
                        draw_line_y_start += float_num
                    except:
                        pass

            if draw_line_y_start > 0:
                draw_line_start += DRAW_LINE_STEP

def drawBar(i, float_num, draw_line_start, draw_line_step, draw_line_y_start):                   
    if(i == 1):
        #print "frame yellow coast ", float_num, " draw_line_y_start ", draw_line_y_start
        plt.bar(draw_line_start, float_num, DRAW_LINE_STEP, draw_line_y_start, color = 'blue', linewidth = 0)
    elif (i == 2):
        #print "frame red coast ", float_num, " draw_line_y_start ", draw_line_y_start
        plt.bar(draw_line_start, float_num, DRAW_LINE_STEP, draw_line_y_start, color = 'red', linewidth =0)
    else:
        #print "frame blue coast ", float_num, " draw_line_y_start ", draw_line_y_start
        plt.bar(draw_line_start, float_num, DRAW_LINE_STEP, draw_line_y_start, color = 'yellow', linewidth =0)
    
            
def flingToLeft():
    os.popen("adb shell input swipe 100 800 600 800")
    # os.popen("adb shell input touchscreen swipe 100 800 600 800 60")

def flingToRight():
    os.popen("adb shell input swipe 600 800 100 800")
    # os.popen("adb shell input touchscreen swipe 600 800 100 800 60")

def flingToTop():
    os.popen("adb shell input swipe 100 800 100 100")
    # os.popen("adb shell input touchscreen swipe 100 800 600 800 60")

def flingToBottom():
    os.popen("adb shell input swipe 100 500 100 1000")
    # os.popen("adb shell input touchscreen swipe 600 800 100 800 60")

def findTopPid():
    top_activity_ret = os.popen("adb shell dumpsys activity top")
    top_activity_ret_str = top_activity_ret.read()

    pid_start_index = top_activity_ret_str.index("pid=") + 4
    pid_str = top_activity_ret_str[pid_start_index : pid_start_index+ 5]
    global top_activity_pid
    top_activity_pid = int(pid_str)
    print "pid is: ", pid_str

def recodeMemInfo():
    global mem_size_before_swipe
    mem_size_before_swipe = getMemInfo()

def printMemInfoChange():
    before = int(mem_size_before_swipe)
    after = int(getMemInfo())
    print "mem size before fling:", str(before), " mem size after fling: ", str(after), " mem change: ", str(after - before)

def getMemInfo():
    mem_info_ret = os.popen("adb shell dumpsys meminfo " + str(top_activity_pid))
    mem_info_ret_str = mem_info_ret.read()
    totle_index = mem_info_ret_str.index("TOTAL") + 5
    # print "mem_info_ret_str is: ", mem_info_ret_str
    for i in range(totle_index, len(mem_info_ret_str)):
        if mem_info_ret_str[i].isdigit():
            totle_index = i
            break;

    total_index_end = 0
    for i in range(totle_index, len(mem_info_ret_str)):
        if not mem_info_ret_str[i].isdigit():
            total_index_end = i
            break;
        
    size = mem_info_ret_str[totle_index : total_index_end]
    # print "mem_size_before_swipe is: ", mem_size_before_swipe
    return size;

def main():
    findTopPid()
    recodeMemInfo()
    
    while total_time < MAX_FRAME_COUNT:
        if random.random() > 0.5:
            # flingToLeft()
            flingToTop()
        else:
            #flingToRight()
            flingToBottom()
        #try:
        output_gfxinfo()	
        time.sleep(1)
        print "total_time ", total_time

    print "average: ", total_value/total_time
    print "max draw coast: ", max_value
    print "draw coast larger than 16ms: ", time_larger_16
    print "draw coast between 10~16ms: ", time_between_10_16
    printMemInfoChange()
     
main()
#flingToLeft()
#flingToRight()
#pl.show()

