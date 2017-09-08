LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := imebra_lib
LOCAL_CPPFLAGS += -fexceptions -pthread -I../../library/include
LOCAL_CPPFLAGS += -DIMEBRA_USE_JAVA -DIMEBRA_MEMORY_POOL_MAX_SIZE=4000000
LOCAL_LDLIBS :=  -llog

FILE_LIST := $(wildcard $(LOCAL_PATH)/../../library/src/*.cpp)
FILE_LIST += $(wildcard $(LOCAL_PATH)/../../library/implementation/*.cpp)
FILE_LIST += $(LOCAL_PATH)/src/main/cpp/java_wrapper.cxx
LOCAL_SRC_FILES := $(FILE_LIST:$(LOCAL_PATH)/%=%)

LOCAL_WHOLE_STATIC_LIBRARIES += android_support
LOCAL_CXXFLAGS += -std=c++11 -fexceptions

include $(BUILD_SHARED_LIBRARY)

$(call import-module, android/support)