LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_CPP_EXTENSION := .cpp
LOCAL_LDLIBS     += -llog
LOCAL_CFLAGS	 := -g -fpermissive
#纯粹为了编译的时候不显示警告，节操崩坏
LOCAL_CXXFLAGS 	 := -Wno-psabi

LOCAL_MODULE     := native_utils
LOCAL_SRC_FILES  := NativeUtils.cpp

include $(BUILD_SHARED_LIBRARY)
