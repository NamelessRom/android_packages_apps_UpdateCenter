LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_AAPT_INCLUDE_ALL_RESOURCES := true

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v13 changeloglib gsonCustom ionCustom

LOCAL_SRC_FILES := \
    $(call all-java-files-under, java)

changelog_dir := ../../../../../external/changeloglib/res
res_dirs := res $(changelog_dir)
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs))

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_PACKAGE_NAME := UpdateCenter
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages com.android.changelibs

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
