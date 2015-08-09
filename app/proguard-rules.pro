  -keep class com.appodeal.** { *; }
  -keep class com.applovin.** { *; }
  -keep class com.mopub.** { *; }
  -keep class org.nexage.** { *; }
  -keep class com.chartboost.** { *; }
  -dontwarn com.chartboost.**
  -keep class com.amazon.** { *; }
  -keep class com.google.android.gms.ads.** { *; }
  -keep class com.google.android.gms.common.GooglePlayServicesUtil { *; }
  -keep class ru.mail.android.mytarget.** { *; }
  -keep class com.unity3d.ads.** { *; }
  -keep class com.applifier.** { *; }

  -keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
  }
  -keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
  }
  -keepnames @com.google.android.gms.common.annotation.KeepName class *
  -keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
  }
  -keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
  }
