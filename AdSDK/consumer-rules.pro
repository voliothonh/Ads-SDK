-keep class com.admob.** { *; }
-dontobfuscate

-keep class com.google.android.gms.common.ConnectionResult {
    int SUCCESS;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
    java.lang.String getId();
    boolean isLimitAdTrackingEnabled();
}
-keep public class com.android.installreferrer.** { *; }

-keep class com.appsflyer.** { *; }
-keep public class com.android.installreferrer.** { *; }

-dontwarn com.appsflyer.**
-keep public class com.google.firebase.messaging.FirebaseMessagingService {
    public *;
}

-keep class com.appsflyer.** { *; }
-keep class kotlin.jvm.internal.Intrinsics{ *; }
-keep class kotlin.collections.**{ *; }
