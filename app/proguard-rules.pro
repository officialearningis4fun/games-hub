# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line numbers and source file attributes for stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve reflection attributes (annotations, signatures, inner classes)
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

-keep,allowoptimization,allowobfuscation class org.apache.logging.log4j.** { *; }

-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
-dontwarn jakarta.servlet.ServletContainerInitializer

-dontwarn io.ktor.client.features.HttpTimeout$Feature
-dontwarn io.ktor.client.features.HttpTimeout$HttpTimeoutCapabilityConfiguration
-dontwarn io.ktor.client.features.HttpTimeout
-dontwarn io.ktor.client.features.HttpTimeoutKt
-dontwarn io.ktor.client.features.websocket.WebSocketCapability
-dontwarn io.ktor.client.features.websocket.WebSocketExtensionsCapability
-dontwarn io.ktor.network.sockets.ConnectTimeoutException
-dontwarn io.ktor.network.sockets.SocketTimeoutException
-dontwarn io.ktor.network.sockets.TimeoutExceptionsCommonKt
-dontwarn io.ktor.util.Lock
-dontwarn io.ktor.utils.io.NativeUtilsJvmKt
-dontwarn javax.servlet.ServletContainerInitializer
-dontwarn io.ktor.client.plugins.HttpTimeout$HttpTimeoutCapabilityConfiguration
-dontwarn io.ktor.client.plugins.HttpTimeout$Plugin
-dontwarn io.ktor.client.plugins.HttpTimeout
-dontwarn io.ktor.client.plugins.contentnegotiation.ContentNegotiation$Config
-dontwarn io.ktor.client.plugins.contentnegotiation.ContentNegotiation$Plugin
-dontwarn io.ktor.client.plugins.contentnegotiation.ContentNegotiation

# Keep all classes annotated with @androidx.annotation.Keep and their members
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Keep kotlinx.serialization classes, companion objects, serializers, and properties
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class * {
    *** Companion;
}
-keepclassmembers class *$serializer {
    *** INSTANCE;
}

# Keep Firebase Realtime Database & Firestore property annotations
-keepclassmembers class * {
    @com.google.firebase.database.PropertyName <fields>;
    @com.google.firebase.database.PropertyName <methods>;
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}

# Keep Firebase and Serialization DTO & Model classes (including constructor parameter names & fields)
-keep class com.learningis4fun.gameshub.api.model.** { *; }
-keepclassmembers class com.learningis4fun.gameshub.api.model.** { *; }

-keep class com.learningis4fun.gameshub.commons.domain.model.** { *; }
-keepclassmembers class com.learningis4fun.gameshub.commons.domain.model.** { *; }

-keep class com.learningis4fun.gameshub.data.data_source.remote.dto.** { *; }
-keepclassmembers class com.learningis4fun.gameshub.data.data_source.remote.dto.** { *; }

-keep class com.learningis4fun.gameshub.domain.model.** { *; }
-keepclassmembers class com.learningis4fun.gameshub.domain.model.** { *; }

-keep class com.learningis4fun.gameshub.commons.ui.routes.** { *; }
-keepclassmembers class com.learningis4fun.gameshub.commons.ui.routes.** { *; }

-keep class com.learningis4fun.gameshub.app.nav.** { *; }
-keepclassmembers class com.learningis4fun.gameshub.app.nav.** { *; }

# Preserve Enum values for state serialization
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
