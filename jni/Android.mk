LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE     := malloc
LOCAL_SRC_FILES  := malloc.c
LOCAL_LDLIBS     := -llog
include $(BUILD_SHARED_LIBRARY)

