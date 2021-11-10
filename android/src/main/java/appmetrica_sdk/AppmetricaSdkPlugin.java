package appmetrica_sdk;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import android.os.Build;
import android.util.Log;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.view.FlutterView;

import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;
import com.yandex.metrica.YandexMetricaDefaultValues;
import com.yandex.metrica.ecommerce.ECommerceAmount;
import com.yandex.metrica.ecommerce.ECommerceCartItem;
import com.yandex.metrica.ecommerce.ECommerceEvent;
import com.yandex.metrica.ecommerce.ECommerceOrder;
import com.yandex.metrica.ecommerce.ECommercePrice;
import com.yandex.metrica.ecommerce.ECommerceProduct;
import com.yandex.metrica.ecommerce.ECommerceReferrer;
import com.yandex.metrica.ecommerce.ECommerceScreen;
import com.yandex.metrica.profile.Attribute;
import com.yandex.metrica.profile.StringAttribute;
import com.yandex.metrica.profile.UserProfile;
import com.yandex.metrica.profile.UserProfileUpdate;

/**
 * AppmetricaSdkPlugin
 */
public class AppmetricaSdkPlugin implements MethodCallHandler, FlutterPlugin {
    private static final String TAG = "AppmetricaSdkPlugin";
    private MethodChannel methodChannel;
    private Context context;
    private Application application;

    /**
     * Plugin registration for v1 embedder.
     */
    public static void registerWith(Registrar registrar) {
        final AppmetricaSdkPlugin instance = new AppmetricaSdkPlugin();
        instance.onAttachedToEngine(registrar.context(), registrar.messenger());
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        onAttachedToEngine(flutterPluginBinding.getApplicationContext(), flutterPluginBinding.getBinaryMessenger());
    }

    private void onAttachedToEngine(Context applicationContext, BinaryMessenger binaryMessenger) {
        application = (Application) applicationContext;
        context = applicationContext;
        methodChannel = new MethodChannel(binaryMessenger, "appmetrica_sdk");
        methodChannel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        methodChannel.setMethodCallHandler(null);
        methodChannel = null;
        context = null;
        application = null;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "activate":
                handleActivate(call, result);
                break;
            case "reportEvent":
                handleReportEvent(call, result);
                break;
            case "reportUserProfileCustomString":
                handleReportUserProfileCustomString(call, result);
                break;
            case "reportUserProfileCustomNumber":
                handleReportUserProfileCustomNumber(call, result);
                break;
            case "reportUserProfileCustomBoolean":
                handleReportUserProfileCustomBoolean(call, result);
                break;
            case "reportUserProfileCustomCounter":
                handleReportUserProfileCustomCounter(call, result);
                break;
            case "reportUserProfileUserName":
                handleReportUserProfileUserName(call, result);
                break;
            case "reportUserProfileNotificationsEnabled":
                handleReportUserProfileNotificationsEnabled(call, result);
                break;
            case "setStatisticsSending":
                handleSetStatisticsSending(call, result);
                break;
            case "getLibraryVersion":
                handleGetLibraryVersion(call, result);
                break;
            case "setUserProfileID":
                handleSetUserProfileID(call, result);
                break;
            case "sendEventsBuffer":
                handleSendEventsBuffer(call, result);
                break;
            case "reportReferralUrl":
                handleReportReferralUrl(call, result);
                break;
            case "showProductCardEvent":
                handleShowProductCardEvent(call, result);
                break;
            case "showScreenEvent":
                handleShowScreenEvent(call, result);
                break;
            case "showProductDetailsEvent":
                handleShowProductDetailsEvent(call, result);
                break;
            case "addCartItemEvent":
                handleAddCartItemEvent(call, result);
            case "removeCartItemEvent":
                handleRemoveCartItemEvent(call, result);
            case "beginCheckoutEvent":
                handleBeginCheckoutEvent(call, result);
            default:
                result.notImplemented();
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private  void handleBeginCheckoutEvent (MethodCall call, Result result){
        try {
            Map<String, Object> arguments = (Map<String, Object>) call.arguments;

            final String orderID = (String) arguments.get("orderID");
            final List<List> products = (List<List>) arguments.get("products");
            final List<ECommerceCartItem> cartedItems = new ArrayList<>();

            products.forEach((List singleProduct) ->{
                ECommercePrice actualPrice = new ECommercePrice(new ECommerceAmount((Integer) singleProduct.get(2), "RUB"));
                ECommercePrice originalPrice = new ECommercePrice(new ECommerceAmount((Integer) singleProduct.get(3), "RUB"));

                ECommerceProduct product = new ECommerceProduct((String) singleProduct.get(0)).setName((String) singleProduct.get(1)).setOriginalPrice(originalPrice).setActualPrice(actualPrice);
                ECommerceCartItem addedItems = new ECommerceCartItem(product, actualPrice, 1.0);

                cartedItems.add(addedItems);
            });

            ECommerceOrder order = new ECommerceOrder(orderID, cartedItems);

            ECommerceEvent beginCheckoutEvent = ECommerceEvent.beginCheckoutEvent(order);

            YandexMetrica.reportECommerce(beginCheckoutEvent);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            result.error("Error sending addCartItemEvent", e.getMessage(), null);
        }
        result.success(null);
    }

    private  void handleAddCartItemEvent (MethodCall call, Result result) {
        try {
            Map<String, Object> arguments = (Map<String, Object>) call.arguments;

            final Integer productActualPrice = (Integer) arguments.get("actualPrice");
            final Integer productOriginalPrice = (Integer) arguments.get("productOriginalPrice");
            final String productName = (String) arguments.get("productName");
            final String productID = (String) arguments.get("productID");

            ECommercePrice actualPrice = new ECommercePrice(new ECommerceAmount(productActualPrice, "RUB"));
            ECommercePrice originalPrice = new ECommercePrice(new ECommerceAmount(productOriginalPrice, "RUB"));
            ECommerceProduct product = new ECommerceProduct(productID).setActualPrice(actualPrice).setOriginalPrice(originalPrice).setName(productName);

            ECommerceCartItem addedItems = new ECommerceCartItem(product, actualPrice, 1.0);
            ECommerceEvent addCartItemEvent = ECommerceEvent.addCartItemEvent(addedItems);

            YandexMetrica.reportECommerce(addCartItemEvent);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            result.error("Error sending addCartItemEvent", e.getMessage(), null);
        }
        result.success(null);
    }

    private  void handleRemoveCartItemEvent (MethodCall call, Result result) {
        try {
            Map<String, Object> arguments = (Map<String, Object>) call.arguments;
            final Integer productActualPrice = (Integer) arguments.get("actualPrice");
            final Integer productOriginalPrice = (Integer) arguments.get("productOriginalPrice");
            final String productName = (String) arguments.get("productName");
            final String productID = (String) arguments.get("productID");

            ECommercePrice actualPrice = new ECommercePrice(new ECommerceAmount(productActualPrice, "RUB"));
            ECommercePrice originalPrice = new ECommercePrice(new ECommerceAmount(productOriginalPrice, "RUB"));
            ECommerceProduct product = new ECommerceProduct(productID).setActualPrice(actualPrice).setOriginalPrice(originalPrice).setName(productName);

            ECommerceCartItem removedItems = new ECommerceCartItem(product, actualPrice, 1.0);
            ECommerceEvent removeCartItemEvent = ECommerceEvent.removeCartItemEvent(removedItems);

            YandexMetrica.reportECommerce(removeCartItemEvent);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            result.error("Error sending removeCartItemEvent", e.getMessage(), null);
        }
        result.success(null);
    }

    private void handleShowProductCardEvent(MethodCall call, Result result) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) call.arguments;

            final Integer productActualPrice = (Integer) arguments.get("actualPrice");
            final Integer productOriginalPrice = (Integer) arguments.get("productOriginalPrice");
            final String screenWhereFromOpen = (String) arguments.get("screenWhereFromOpen");
            final String productName = (String) arguments.get("productName");
            final String productID = (String) arguments.get("productID");

            ECommerceScreen screen = new ECommerceScreen().setName(screenWhereFromOpen);

            ECommercePrice actualPrice = new ECommercePrice(new ECommerceAmount(productActualPrice, "RUB"));
            ECommercePrice originalPrice = new ECommercePrice(new ECommerceAmount(productOriginalPrice, "RUB"));

            ECommerceProduct product = new ECommerceProduct(productID).setActualPrice(actualPrice).setOriginalPrice(originalPrice).setName(productName);
            ECommerceEvent showProductCardEvent = ECommerceEvent.showProductCardEvent(product, screen);

            YandexMetrica.reportECommerce(showProductCardEvent);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            result.error("Error sending showProductCardEvent", e.getMessage(), null);
        }
        result.success(null);
    }

    private void handleShowScreenEvent(MethodCall call, Result result) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) call.arguments;
            final String screenWhereFromOpen = (String) arguments.get("screenWhereFromOpen");
            ECommerceScreen screen = new ECommerceScreen().setName(screenWhereFromOpen);
            ECommerceEvent showScreenEvent = ECommerceEvent.showScreenEvent(screen);

            YandexMetrica.reportECommerce(showScreenEvent);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            result.error("Error sending showScreenEvent", e.getMessage(), null);
        }
        result.success(null);
    }

    private  void handleShowProductDetailsEvent(MethodCall call, Result result) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) call.arguments;

            final Integer productActualPrice = (Integer) arguments.get("actualPrice");
            final Integer productOriginalPrice = (Integer) arguments.get("productOriginalPrice");
            final String productName = (String) arguments.get("productName");
            final String productID = (String) arguments.get("productID");

            ECommercePrice actualPrice = new ECommercePrice(new ECommerceAmount(productActualPrice, "RUB"));
            ECommercePrice originalPrice = new ECommercePrice(new ECommerceAmount(productOriginalPrice, "RUB"));

            ECommerceReferrer referrer = new ECommerceReferrer();
            ECommerceProduct product = new ECommerceProduct(productID).setActualPrice(actualPrice).setOriginalPrice(originalPrice).setName(productName);

            ECommerceEvent showProductDetailsEvent = ECommerceEvent.showProductDetailsEvent(product, referrer);
            YandexMetrica.reportECommerce(showProductDetailsEvent);
        } catch (Exception e){
            Log.e(TAG, e.getMessage(), e);
            result.error("Error sending showProductDetailsEvent", e.getMessage(), null);
        }
    }

    private void handleActivate(MethodCall call, Result result) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) call.arguments;
            // Get activation parameters.
            final String apiKey = (String) arguments.get("apiKey");
            final int sessionTimeout = (int) arguments.get("sessionTimeout");
            final boolean locationTracking = (boolean) arguments.get("locationTracking");
            final boolean statisticsSending = (boolean) arguments.get("statisticsSending");
            final boolean crashReporting = (boolean) arguments.get("crashReporting");
            final int maxReportsInDatabaseCount = (int) arguments.get("maxReportsInDatabaseCount");
            // Creating an extended library configuration.
            YandexMetricaConfig config = YandexMetricaConfig.newConfigBuilder(apiKey)
                    .withLogs()
                    .withSessionTimeout(sessionTimeout)
                    .withLocationTracking(locationTracking)
                    .withStatisticsSending(statisticsSending)
                    .withCrashReporting(crashReporting)
                    .withMaxReportsInDatabaseCount(maxReportsInDatabaseCount)
                    .build();
            // Initializing the AppMetrica SDK.
            YandexMetrica.activate(context, config);
            // Automatic tracking of user activity.
            YandexMetrica.enableActivityAutoTracking(application);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            result.error("Error performing activation", e.getMessage(), null);
        }
        result.success(null);
    }

    private void handleReportEvent(MethodCall call, Result result) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) call.arguments;
            final String eventName = (String) arguments.get("name");
            @SuppressWarnings("unchecked")
            Map<String, Object> attributes = (Map<String, Object>) arguments.get("attributes");
            if (attributes == null) {
                YandexMetrica.reportEvent(eventName);
            } else {
                YandexMetrica.reportEvent(eventName, attributes);
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            result.error("Error reporing event", e.getMessage(), null);
        }

        result.success(null);
    }

    private void handleReportUserProfileCustomString(MethodCall call, Result result) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) call.arguments;
            final String key = (String) arguments.get("key");
            final String value = (String) arguments.get("value");
            UserProfile.Builder profileBuilder = UserProfile.newBuilder();
            if (value != null) {
                profileBuilder.apply(Attribute.customString(key).withValue(value));
            } else {
                profileBuilder.apply(Attribute.customString(key).withValueReset());
            }
            YandexMetrica.reportUserProfile(profileBuilder.build());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            result.error("Error reporing user profile custom string", e.getMessage(), null);
        }

        result.success(null);
    }

    private void handleReportUserProfileCustomNumber(MethodCall call, Result result) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) call.arguments;
            final String key = (String) arguments.get("key");
            UserProfile.Builder profileBuilder = UserProfile.newBuilder();
            if (arguments.get("value") != null) {
                final double value = (double) arguments.get("value");
                profileBuilder.apply(Attribute.customNumber(key).withValue(value));
            } else {
                profileBuilder.apply(Attribute.customNumber(key).withValueReset());
            }
            YandexMetrica.reportUserProfile(profileBuilder.build());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            result.error("Error reporing user profile custom number", e.getMessage(), null);
        }

        result.success(null);
    }

    private void handleReportUserProfileCustomBoolean(MethodCall call, Result result) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) call.arguments;
            final String key = (String) arguments.get("key");
            UserProfile.Builder profileBuilder = UserProfile.newBuilder();
            if (arguments.get("value") != null) {
                final boolean value = (boolean) arguments.get("value");
                profileBuilder.apply(Attribute.customBoolean(key).withValue(value));
            } else {
                profileBuilder.apply(Attribute.customBoolean(key).withValueReset());
            }
            YandexMetrica.reportUserProfile(profileBuilder.build());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            result.error("Error reporing user profile custom boolean", e.getMessage(), null);
        }

        result.success(null);
    }

    private void handleReportUserProfileCustomCounter(MethodCall call, Result result) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) call.arguments;
            final String key = (String) arguments.get("key");
            final double delta = (double) arguments.get("delta");
            UserProfile.Builder profileBuilder = UserProfile.newBuilder();
            profileBuilder.apply(Attribute.customCounter(key).withDelta(delta));
            YandexMetrica.reportUserProfile(profileBuilder.build());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            result.error("Error reporing user profile custom counter", e.getMessage(), null);
        }

        result.success(null);
    }

    private void handleReportUserProfileUserName(MethodCall call, Result result) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) call.arguments;
            UserProfile.Builder profileBuilder = UserProfile.newBuilder();
            if (arguments.get("userName") != null) {
                final String userName = (String) arguments.get("userName");
                profileBuilder.apply(Attribute.name().withValue(userName));
            } else {
                profileBuilder.apply(Attribute.name().withValueReset());
            }
            YandexMetrica.reportUserProfile(profileBuilder.build());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            result.error("Error reporing user profile user name", e.getMessage(), null);
        }

        result.success(null);
    }

    private void handleReportUserProfileNotificationsEnabled(MethodCall call, Result result) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) call.arguments;
            UserProfile.Builder profileBuilder = UserProfile.newBuilder();
            if (arguments.get("notificationsEnabled") != null) {
                final boolean notificationsEnabled = (boolean) arguments.get("notificationsEnabled");
                profileBuilder.apply(Attribute.notificationsEnabled().withValue(notificationsEnabled));
            } else {
                profileBuilder.apply(Attribute.notificationsEnabled().withValueReset());
            }
            YandexMetrica.reportUserProfile(profileBuilder.build());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            result.error("Error reporing user profile user name", e.getMessage(), null);
        }

        result.success(null);
    }

    private void handleSetStatisticsSending(MethodCall call, Result result) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) call.arguments;
            final boolean statisticsSending = (boolean) arguments.get("statisticsSending");
            YandexMetrica.setStatisticsSending(context, statisticsSending);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            result.error("Error enable sending statistics", e.getMessage(), null);
        }

        result.success(null);
    }

    private void handleGetLibraryVersion(MethodCall call, Result result) {
        try {
            result.success(YandexMetrica.getLibraryVersion());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            result.error("Error enable sending statistics", e.getMessage(), null);
        }
    }

    private void handleSetUserProfileID(MethodCall call, Result result) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) call.arguments;
            final String userProfileID = (String) arguments.get("userProfileID");
            YandexMetrica.setUserProfileID(userProfileID);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            result.error("Error sets the ID of the user profile", e.getMessage(), null);
        }

        result.success(null);
    }

    private void handleSendEventsBuffer(MethodCall call, Result result) {
        try {
            YandexMetrica.sendEventsBuffer();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            result.error("Error sending stored events from the buffer", e.getMessage(), null);

        }

        result.success(null);
    }

    private void handleReportReferralUrl(MethodCall call, Result result) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) call.arguments;
            final String referral = (String) arguments.get("referral");
            YandexMetrica.reportReferralUrl(referral);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            result.error("Error sets the ID of the user profile", e.getMessage(), null);
        }

        result.success(null);
    }
}
