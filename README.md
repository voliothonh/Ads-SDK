# Welcome to AdsSDK

**Imprort Depencency**
```
implementation 'com.github.voliothonh:Ads-SDK:1.0.0'
```


## 1.  Khởi tạo SDK trong Application class
```kotlin
AdsSDK.init(this)
    .setAdCallback(adCallback) // Set global callback for all AdType/AdUnit
    .setIgnoreAdResume(SplashActivity::class.java) // Ingore show AdResume in these classes (All fragments and Activities is Accepted)
 ```

 <br/>


## 2. Banner

Hiển thị Banner.
* Nếu **bannerAd** chưa được load => Thực hiện load mới rồi fill vào ViewGroup
* Nếu **bannerAd** đã được load trước đó => Thực hiện fill vào Group
* ```adUnitId``` là key để lưu lại instance của **bannerAd**


```kotlin
/**
 * @param adContainer: ViewGroup contain this Ad
 * @param adUnitId AdId
 * @param forceRefresh always load new ad then fill to ViewGroup
 * @param callback callback
 */
AdmobBanner.show300x250(
    adContainer: ViewGroup,
    adUnitId: String,
    forceRefresh: Boolean = false,
callback: TAdCallback? = null
)
```

```kotlin
/**
 * @param adContainer: ViewGroup contain this Ad
 * @param adUnitId AdId
 * @param forceRefresh always load new ad then fill to ViewGroup
 * @param callback callback
 */
AdmobBanner.showAdaptive(
    adContainer: ViewGroup,
    adUnitId: String,
    forceRefresh: Boolean = false,
callback: TAdCallback? = null
)
```

```kotlin
/**
 * Each position show be Unique AdUnitID
 * @param adContainer: ViewGroup contain this Ad
 * @param adUnitId AdId
 * @param showOnBottom: Show on Top or Bottom
 * @param forceRefresh always load new ad then fill to ViewGroup
 * @param callback callback
 */
AdmobBanner.showCollapsible(
    adContainer: ViewGroup,
    adUnitId: String,
    showOnBottom: Boolean = true,
forceRefresh: Boolean = false,
callback: TAdCallback? = null
)
```

 <br/>

## 3. Native

Hiển thị Native.
* Nếu **nativeAd** chưa được load => Thực hiện load Native rồi fill vào ViewGroup
* Nếu **nativeAd** đã được load trước đó => Thực hiện fill vào Group
* ```adUnitId``` là key để lưu lại instance của NativeAd
* IdView của NativeLayout tham khảo trong: https://github.com/voliothonh/Ads-SDK/blob/master/AdSDK/src/main/res/layout/layout_native_demo.xml

```kotlin
/**
 * @param adContainer: ViewGroup contain this Native
 * @param adUnitId AdId
 * @param nativeContentLayoutId LayoutRes for Native
 * @param forceRefresh always load new ad then fill to ViewGroup
 * @param callback callback
 */
AdmobNative.show(
    adContainer: ViewGroup,
    adUnitId: String,
    @LayoutRes nativeContentLayoutId: Int,
    forceRefresh: Boolean = false,
callback: TAdCallback? = null
)
```

 <br/>

## 4. Interstitials
##### 4.1 AdmobInter
* **InterAd** sẽ được lưu dạng hashMap **key[adUnitId] = interAd**
* Khoảng cách giữa 2 lần show **InterAd** mặc định là 15000ms (**15s**), có thể thay đổi thông qua **RemoteConfig** bằng biến ```interTimeDelayMs```


Thực hiện load **InterAd** khi cần thiết
Không nên load ở Application, thông thường load ở MainActivity
```kotlin
AdmobInter.load(
    adUnitId: String,
    callback: TAdCallback? = null
)
```

```kotlin
/**
 * @param adUnitId
 * @param showLoadingInter: show DialogLoading 1s before show Inter
 * @param forceShow: try to force show InterAd, ignore interTimeDelayMs config
 * @param loadAfterDismiss: handle new load after InterAd dismiss
 * @param loadIfNotAvailable: try to load if InterAd not available yet
 * @param callback: callback
 * @param nextAction: callback for your work, always call whether the InterAd display is successful or not
 */
* AdmobInter.show(
    adUnitId: String,
    showLoadingInter: Boolean = true,
forceShow: Boolean = false,
loadAfterDismiss: Boolean = true,
loadIfNotAvailable: Boolean = true,
callback: TAdCallback? = null,
nextAction: () -> Unit
)
```

```kotlin
/**
 * Config when show InterAd.
 * Usually we will take the next action after Inter dismiss
 * But InterAd's onDismissFullContent is suffering from a callback bug that is several hundred milliseconds late. * Therefore, the screen change delay is not happening as expected.
 * This function will fix that error.
 * We will handle nextAction while InterAd starting showing
 * @param handleNextActionDuringInterShow: if set {true} => fix bug delay onDismiss of Inter
 * @param delayTimeToActionAfterShowInter: time to delay start from showInter. Recommend 0 if startActivity , 300 with navigateFragment
 */
AdmobInter.setNextWhileInterShowing(
    handleNextActionDuringInterShow: Boolean,
    delayTimeToActionAfterShowInter: Int = 300
)
```
##### 4.2 AdmobInterResume
* Sử dụng **InterResume** khi user quay trở lại app thay cho **OpenResumeApp**
* Khi quay lại sẽ show một màn hình thông báo WelcomeBack chứa button OK. Ấn button sẽ show **InterAd**
* Chỉ cần gọi hàm này một lần trong app. (Gọi lúc nào bắt đầu load lúc đó và show ở lần resume tiếp theo)


```kotlin
AdmobInterResume.load(id: String)
```
##### 4.3 AdmobInterSplash
```kotlin
/**
 * @param adUnitId: adUnit
 * @param timeout: timeout to wait ad show
 * @param nextAction
 */
AdmobInterSplash.show(
    adUnitId: String,
    timeout: Long,
    nextAction: () -> Unit
)
```


 <br/>

## 5. OpenApp
#### 5.1 AdmobOpenSplash
  ```kotlin
  /**
 * @param adUnitId: adUnitId
 * @param timeout: timeout to wait ad show
 * @param onAdLoaded: callback when adLoaded => for update UI or something
 * @param nextAction: callback for your work, general handle nextActivity or nextFragment
 */
AdmobOpenSplash.show(
    adUnitId: String,
    timeout: Long,
    onAdLoaded: () -> Unit = {},
nextAction: () -> Unit
)
```
#### 5.2 AdmobOpenResume
Chỉ cần gọi hàm này một lần trong app. (Gọi lúc nào bắt đầu load lúc đó và show ở lần resume tiếp theo)
```kotlin
AdmobOpenResume.load(id: String)
```


 <br/>

## 6. Rewarded
```kotlin
/**
 * @param activity: Show on this activity
 * @param adUnitId: adUnitId
 * @param callBack
 * @param onUserEarnedReward
 * @param onFailureUserNotEarn
 */
fun show(
    activity: AppCompatActivity,
    adUnitId: String,
    callBack: TAdCallback? = null,
    onFailureUserNotEarn: () -> Unit,
    onUserEarnedReward: () -> Unit
)
```