package com.salesforceapp;

import android.app.Application;
import android.content.Context;
import com.facebook.react.PackageList;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.soloader.SoLoader;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import com.salesforce.marketingcloud.MCLogListener;
import com.salesforce.marketingcloud.MarketingCloudConfig;
import com.salesforce.marketingcloud.MarketingCloudSdk;
import com.salesforce.marketingcloud.messages.inbox.InboxMessageManager;
import com.salesforce.marketingcloud.notifications.NotificationCustomizationOptions;
import com.salesforce.marketingcloud.notifications.NotificationManager;
import com.salesforce.marketingcloud.notifications.NotificationMessage;



public class MainApplication extends Application implements ReactApplication {

  private final ReactNativeHost mReactNativeHost =
      new ReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
          return BuildConfig.DEBUG;
        }

        @Override
        protected List<ReactPackage> getPackages() {
          @SuppressWarnings("UnnecessaryLocalVariable")
          List<ReactPackage> packages = new PackageList(this).getPackages();
          // Packages that cannot be autolinked yet can be added manually here, for example:
          // packages.add(new MyReactNativePackage());
          return packages;
        }

        @Override
        protected String getJSMainModuleName() {
          return "index";
        }
      };

  @Override
  public ReactNativeHost getReactNativeHost() {
    return mReactNativeHost;
  }

  @Override
  public void onCreate() {
    super.onCreate();


      // MC With SIT Configuration
      MarketingCloudSdk.setLogLevel(MCLogListener.VERBOSE);
      MarketingCloudSdk.setLogListener(new MCLogListener.AndroidLogListener());
      MarketingCloudSdk.init(this, MarketingCloudConfig.builder() //
              .setApplicationId(BuildConfig.MC_APPLICATION_ID) // SIT App ID from MC
              .setAccessToken(BuildConfig.MC_ACCESS_TOKEN) // SIT Access Token from MC
              .setSenderId(BuildConfig.SENDER_ID) // Firebase ID
              .setDelayRegistrationUntilContactKeyIsSet(true)
              .setMarketingCloudServerUrl(BuildConfig.MC_CLOUD_SERVER_URL)// MC Endpoint
              .setInboxEnabled(true)
              // .setGeofencingEnabled(true)
              // ENABLE MARKETING CLOUD FEATURES
              .setNotificationCustomizationOptions(NotificationCustomizationOptions.create(R.drawable.logo))
              .build(this), initializationStatus -> Log.d("Initialization Status", initializationStatus.toString()));

      MarketingCloudSdk.requestSdk(marketingCloudSdk -> {
          marketingCloudSdk.getInboxMessageManager().refreshInbox(b -> {
              Log.v("Inbox Refresh: ", "" + b);
          });
      });
      MarketingCloudSdk.requestSdk(marketingCloudSdk -> {

          marketingCloudSdk.getNotificationManager().setShouldShowNotificationListener(new NotificationManager.ShouldShowNotificationListener() {
              @Override
              public boolean shouldShowNotification(@NonNull NotificationMessage notificationMessage) {
                  Log.d("Should notification", notificationMessage.toString());
                  if (notificationMessage.customKeys().get("NotifyType") != null && notificationMessage.customKeys().get("NotifyType").equals("STC")) {
                      Bundle bundle = new Bundle();
                      try {
                          JSONObject data = new JSONObject();
                          data.put("NotifyType", "STC");
                          bundle.putString("data", data.toString());
                      } catch (JSONException e) {
                          e.printStackTrace();
                      }


                      bundle.putString("id", new Random(System.currentTimeMillis()).nextInt() + "");
                      bundle.putBoolean("ignoreInForeground", false);
                      bundle.putString("title", notificationMessage.title());
                      bundle.putString("message", notificationMessage.alert());
                      bundle.putString("smallIcon", "logo");


                      new RNPushNotificationHelper((Application) getApplicationContext()).sendToNotificationCentre(bundle);
                      return false;
                  } else {
                      return true;
                  }
              }
          });

          // Log.d("SDK State", marketingCloudSdk.getSdkState().toString());
      });


    SoLoader.init(this, /* native exopackage */ false);
    initializeFlipper(this, getReactNativeHost().getReactInstanceManager());
  }

  /**
   * Loads Flipper in React Native templates. Call this in the onCreate method with something like
   * initializeFlipper(this, getReactNativeHost().getReactInstanceManager());
   *
   * @param context
   * @param reactInstanceManager
   */
  private static void initializeFlipper(
      Context context, ReactInstanceManager reactInstanceManager) {
    if (BuildConfig.DEBUG) {
      try {
        /*
         We use reflection here to pick up the class that initializes Flipper,
        since Flipper library is not available in release mode
        */
        Class<?> aClass = Class.forName("com.salesforceapp.ReactNativeFlipper");
        aClass
            .getMethod("initializeFlipper", Context.class, ReactInstanceManager.class)
            .invoke(null, context, reactInstanceManager);
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
  }
}
