# ==========================================
# 1. 제네릭, 어노테이션, 내부 클래스 및 크래시 위치 정보
# ==========================================
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ==========================================
# 2. 카카오맵 SDK
# ==========================================
-keep class com.kakao.vectormap.** { *; }

# ==========================================
# 3. 네트워크 DTO 및 Model
# 실제 클래스의 package와 일치해야 적용됨
# ==========================================
-keep class com.braveberry.tourdataproject.**.model.** { *; }
-keep class com.braveberry.tourdataproject.**.dto.** { *; }
-keep class com.tourdataproject.**.model.** { *; }
-keep class com.tourdataproject.**.dto.** { *; }

# ==========================================
# 4. Retrofit API 인터페이스
# ==========================================
-keep interface com.braveberry.tourdataproject.**.api.** { *; }
-keep interface com.tourdataproject.**.api.** { *; }

# suspend API 및 Response의 제네릭 정보 보존
-keep,allowobfuscation,allowoptimization class kotlin.coroutines.Continuation
-keep,allowobfuscation,allowoptimization class retrofit2.Response

# ==========================================
# 5. Gson TypeToken
# ==========================================
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken { *; }
