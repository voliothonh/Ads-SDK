# Welcome to AdsSDK

**dependency**
```
implementation 'com.github.voliothonh:Ads-SDK:2.x.x'
```
https://github.com/voliothonh/Ads-SDK/releases

**settings.gradle**
```
dependencyResolutionManagement {  
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)  
    repositories {  
	  google()  
      mavenCentral()  
      jcenter()  
      maven { url "https://jitpack.io" }  
	  maven { url 'https://artifact.bytedance.com/repository/pangle/' }  
	  maven { url 'https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea' }  
 }}
 ```
# Lib Ver 2



## Intro-1: Khai báo file config Ads
* AdFormat : khai báo trong file configAds
````kotlin
object AdFormat {
    val Banner = "banner"
    val Native = "native"
    val Interstitial = "interstitial"
    val Reward = "reward"
    val Open = "open_app"
}
````
* Mẫu: khai báo trong file configAds

````json
{
  "listAds": [
    {
      "spaceName": "ADMOB_Interstitial_Splash",
      "adsType": "interstitial",
      "id": "ca-app-pub...",
      "isEnable": "enable"
    },
    {
      "spaceName": "ADMOB_AppOpenAds_Resume",
      "adsType": "open_app",
      "id": "ca-app-pub...",
      "isEnable": "enable"
    },
    {
      "spaceName": "ADMOB_Native_Language",
      "adsType": "native",
      "id": "ca-app-pub...",
      "isEnable": "disable"
    },

    {
      "spaceName": "ADMOB_Banner_General",
      "adsType": "banner",
      "id": "ca-app-pub=...",
      "isEnable": "disable"
    },
    {
      "spaceName": "ADMOB_Reward_ContentAI",
      "adsType": "reward",
      "id": "ca-app...",
      "isEnable": "enable"
    }
  ]
}

````
## Intro-2:
* adCallback
````kotlin
interface TAdCallback {
    fun onAdStartLoading(adUnit: String, adType: AdType) {}
    fun onAdClicked(adUnit: String, adType: AdType) {}
    fun onAdClosed(adUnit: String, adType: AdType) {}
    fun onAdDismissedFullScreenContent(adUnit: String, adType: AdType) {}
    fun onAdShowedFullScreenContent(adUnit: String, adType: AdType) {}
    fun onAdFailedToShowFullScreenContent(error : String, adUnit: String, adType: AdType) {}
    fun onAdFailedToLoad(adUnit: String, adType: AdType, error: LoadAdError) {}
    fun onAdImpression(adUnit: String, adType: AdType) {}
    fun onAdLoaded(adUnit: String, adType: AdType) {}
    fun onAdOpened(adUnit: String, adType: AdType) {}
    fun onAdSwipeGestureClicked(adUnit: String, adType: AdType) {}
    fun onPaidValueListener(bundle : Bundle) {}
    fun onSetInterFloorId(){} /*When application resume, some device init all member variable, pls call set InterFloor.setId*/
    fun onDisable(){} /*Ad disable by remote config*/
}
````

## 1.  Khởi tạo SDK trong Application class
```kotlin
/**  
 * @param application: Application 
 * @param  path: path file config ad
 * @param keyConfigAds : key get remote Config Ads 
 * @param callback callback  
 */

AdsSDK.init(this,
path = "admod_id_new.json",
keyConfigAds =  "ads_configx",
isDebug = false)
.setAdCallback(adCallback)// Set global callback for all AdType/AdUnit
.enableAppsflyer("AppsflyerID")
.setIgnoreAdResume(SplashActivity::class.java)
 .setAutoTrackingPaidValueInSdk(false)
 ```

* Enable/Disable Ad by AdType
```kotlin
AdsSDK.setEnableBanner(false)
AdsSDK.setEnableNative(false)
AdsSDK.setEnableInter(false)
AdsSDK.setEnableOpenAds(false)
AdsSDK.setEnableRewarded(false)
```

## 2. Banner

Hiển thị Banner.
* Nếu **bannerAd** chưa được load => Thực hiện load mới rồi fill vào ViewGroup
* Nếu **bannerAd** đã được load trước đó => Thực hiện fill vào Group
```kotlin
/**  
 * @param adContainer: ViewGroup contain this Ad  
 * @param space Space  
 * @param forceRefresh always load new ad then fill to ViewGroup  
 * @param callback callback  
 */
AdmobBanner.showAdaptive(
	adContainer: ViewGroup, 
	space: String, 
	forceRefresh: Boolean = false,
	callback: TAdCallback? = null
 )
```
```kotlin
/**  
 * @param lifecycle: Lifecycle 
 * @param adContainer: ViewGroup contain this Ad  
 * @param space Space  
 * @param showOnBottom (true - show on Bottom, false - show on Top)
 * @param forceRefresh always load new ad then fill to ViewGroup  
 * @param callback callback  
 */
AdmobBanner.showCollapsible(
    lifecycle: Lifecycle? = null,
    adContainer: ViewGroup,
    space: String,
    showOnBottom: Boolean = true,
    forceRefresh: Boolean = false,
    callback: TAdCallback? = null
 )
```

## 3. Native

Hiển thị Native.
* Nếu **nativeAd** chưa được load => Thực hiện load Native rồi fill vào ViewGroup
* Nếu **nativeAd** đã được load trước đó => Thực hiện fill vào Group
* Nếu muốn load trước space => sử dụng AdmobNative.loadOnly()

```kotlin
/**  
 * @param space Space  
 */
 AdmobNative.loadOnly(  
	space: String
)
```

```kotlin
/**  
 * @param adContainer: ViewGroup contain this Native  
 * @param space Space  
 * @param nativeContentLayoutId LayoutRes for Native  
 * @param forceRefresh always load new ad then fill to ViewGroup  
 * @param callback callback  
 */
 AdmobNative.show(  
	adContainer: ViewGroup,  
	space: String,  
	@LayoutRes nativeContentLayoutId: Int,  
	forceRefresh: Boolean = false,  
	callback: TAdCallback? = null  
)
```


## 4. Interstitials
##### 4.1 AdmobInter
* **InterAd** sẽ được lưu dạng hashMap **key[space] = interAd**
* Khoảng cách giữa 2 lần show **InterAd** mặc định là 15000ms (**15s**), có thể thay đổi thông qua **RemoteConfig** bằng biến ```interTimeDelayMs```


Thực hiện load **InterAd** khi cần thiết
```kotlin
AdmobInter.load(
	space: String, 
	callback: TAdCallback? = null
)
```

```kotlin
/**  
 * @param space  
 * @param showLoadingInter: show DialogLoading 1s before show Inter  
 * @param forceShow: try to force show InterAd, ignore interTimeDelayMs config  
 * @param loadAfterDismiss: handle new load after InterAd dismiss  
 * @param loadIfNotAvailable: try to load if InterAd not available yet  
 * @param callback: callback  
 * @param nextAction: callback for your work, always call whether the InterAd display is successful or not  
 */
 * AdmobInter.show(  
	space : String,  
	showLoadingInter: Boolean = true,  
	forceShow: Boolean = false,  
	loadAfterDismiss: Boolean = true,  
	loadIfNotAvailable: Boolean = true,  
	callback: TAdCallback? = null,  
	nextAction: () -> Unit  
)
```
##### 4.2 AdmobInterSplash
```kotlin
/**  
 * @param space  
 * @param timeout: timeout to wait ad show  
  * @param showAdCallback : callback
 * @param nextAction  
  */  
AdmobInterSplash.show(  
	space: String,
    timeout: Long,
    showAdCallback: TAdCallback,
    nextAction: () -> Unit
)
```

## 5. OpenApp
#### 5.1 AdmobOpenSplash
  ```kotlin
  /**  
 * @param space: adSpace  
 * @param timeout: timeout to wait ad show  
 * @param onAdLoaded: callback when adLoaded => for update UI or something  
 * @param nextAction: callback for your work, general handle nextActivity or nextFragment  
 */
 AdmobOpenSplash.show(  
	space: String,   
	timeout: Long,  
	onAdLoaded: () -> Unit = {},   
	nextAction: () -> Unit  
)
```
#### 5.2 AdmobOpenResume
*Chỉ cần gọi hàm này một lần trong app. (Gọi lúc nào bắt đầu load lúc đó và show ở lần resume tiếp theo).
```kotlin
AdmobOpenResume.load(space: String)
```
*Nếu muốn bỏ qua lần 1 show tiếp theo
```kotlin
AdsSDK.preventShowResumeAdNextTime()
```
## 6. Rewarded
```kotlin
/**  
 * @param activity: Show on this activity  
 * @param space
 * @param callBack  
  * @param onUserEarnedReward  
  * @param onFailureUserNotEarn  
  */  
fun show(  
	activity: AppCompatActivity,  
	space: String,  
    callBack: TAdCallback? = null,  
    onFailureUserNotEarn: () -> Unit,  
    onUserEarnedReward: () -> Unit  
)
```

