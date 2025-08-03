-dontobfuscate
-keepattributes SourceFile, LineNumberTable
-allowaccessmodification

-keep class com.pinterest.ktlint.Main {
  public static void main(java.lang.String[]);
}

# Keep kotlin metadata so that the Kotlin compiler knows about top level functions
-keep class kotlin.Metadata { *; }

# Keep Unit as it's in the signature of public methods:
-keep class kotlin.Unit { *; }

# called reflectively by kotlin compiler internals

# called by reflection in ktlint code
-keep class com.pinterest.ktlint.rule.engine.core.api.LoggerFactory { <init>(); }
-keep class * implements org.jetbrains.kotlin.kdoc.psi.api.KDocElement { *; }
-keep class * implements org.jetbrains.kotlin.psi.KtElement { *; }

# ignore unknown references
-dontwarn org.jetbrains.kotlin.com.intellij.util.diff.Diff$Change
-dontwarn org.jetbrains.kotlin.com.intellij.util.diff.Diff

# Ignore annotations
-dontwarn org.jetbrains.annotations.*
-dontwarn org.jetbrains.kotlin.com.google.errorprone.annotations.**
-dontwarn org.jetbrains.kotlin.com.google.j2objc.annotations.**
-dontwarn javax.servlet.*
-dontwarn org.mozilla.universalchardet.*
-dontwarn org.checkerframework.checker.nullness.**
-dontwarn kotlin.annotations.**
