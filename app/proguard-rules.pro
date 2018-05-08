#
# Copyright 2015 Chaos
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/Chaos/dev/AndroidSDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# base configuration
#-mergeinterfacesaggressively
-keepattributes Signature, InnerClasses, Exceptions, SourceFile, LineNumberTable

# For jsoup
-keep class org.jsoup.nodes.Entities

# For Okio
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.**

# For retrofit
-dontwarn retrofit2.**

# For OkHttpDownloader
-dontwarn com.squareup.okhttp.*

# For OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# For BottomBar
-dontwarn com.roughike.bottombar.**

# For GreenDao
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
    public static java.lang.String TABLENAME;
}
-keep class **$Properties
-dontwarn org.greenrobot.greendao.database.**
-dontwarn rx.**

#For Wechat
-keep class com.tencent.mm.opensdk.**

#For BaseRecyclerViewAdapterHelper
-keep class * extends com.chad.library.adapter.** {
   *;
}
