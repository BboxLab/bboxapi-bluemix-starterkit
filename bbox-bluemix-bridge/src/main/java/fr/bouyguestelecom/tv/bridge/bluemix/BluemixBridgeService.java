/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2015 InnovationLab BboxLab
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bouyguestelecom.tv.bridge.bluemix;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONObject;

import fr.bouyguestelecom.tv.bridge.IBboxBridge;
import fr.bouyguestelecom.tv.bridge.bluemix.utils.Constants;
import fr.bouyguestelecom.tv.bridge.bluemix.utils.MqttHandler;
import fr.bouyguestelecom.tv.bridge.bluemix.utils.TopicFactory;
import fr.bouyguestelecom.tv.openapi.secondscreen.application.ApplicationsManager;
import fr.bouyguestelecom.tv.openapi.secondscreen.authenticate.IAuthCallback;
import fr.bouyguestelecom.tv.openapi.secondscreen.bbox.Bbox;
import fr.bouyguestelecom.tv.openapi.secondscreen.notification.NotificationManager;
import fr.bouyguestelecom.tv.openapi.secondscreen.notification.NotificationType;
import fr.bouyguestelecom.tv.openapi.secondscreen.notification.WebSocket;

/**
 * @author Bertrand Martel
 */
public class BluemixBridgeService extends IntentService {

    private static final String TAG = BluemixBridgeService.class.getSimpleName();

    private final static String SERVICE_THREAD_NAME = "BluemixBridgeService";

    protected IoTStarterApplication app;

    public IAuthCallback authenticationCallback;

    private IBboxBridge.Stub bboxIotService = new IBboxBridge.Stub() {
    };

    public BluemixBridgeService() {
        super(SERVICE_THREAD_NAME);
    }

    @SuppressLint("NewApi")
    @Override
    public void onCreate() {
        super.onCreate();

        app = (IoTStarterApplication) getApplicationContext();

        Log.i(TAG, "BluemixBridgeService started");

        MqttHandler mqttHandle = MqttHandler.getInstance(this);

        app.setDeviceId(BuildConfig.BLUEMIX_IOT_DEVICEID);
        app.setOrganization(BuildConfig.BLUEMIX_IOT_ORG);
        app.setAuthToken(BuildConfig.BLUEMIX_IOT_AUTH_TOKEN);

        if (checkCanConnect()) {
            mqttHandle.connect();
        } else {
            Log.i(TAG, "cant connect to MQTT");
        }

        authenticationCallback = new IAuthCallback() {
            @Override
            public void onAuthResult(int code, String msg) {

                Log.d(TAG, "onAuthResult msg=" + msg + " code=" + code);

                if (code > 299 || code < 200) {
                    return;
                }

                try {

                    final Bbox bbox = BboxHolder.getInstance().getBbox();

                    if (bbox != null) {
                        bbox.getApplicationsManager().getMyAppId("Remote_Controller",
                                new ApplicationsManager.CallbackAppId() {
                                    @Override
                                    public void onResult(int statusCode, String appId) {

                                        final NotificationManager notification = WebSocket.getInstance(appId, bbox);

                                        notification.subscribe(NotificationType.IOT, new NotificationManager.CallbackSubscribed() {
                                            @Override
                                            public void onResult(int i) {

                                            }
                                        });

                                        // Before the NotificationManager start listening we are going to subscribe to Message.
                                        notification.subscribe(NotificationType.MESSAGE,

                                                // We provide a callback, because we want to start listening to notifications after we subscribe to Message
                                                new NotificationManager.CallbackSubscribed() {
                                                    @Override
                                                    public void onResult(int statusCode) {

                                                        Log.d(TAG, "status subscribe:" + statusCode);

                                                        notification.subscribe(NotificationType.APPLICATION, null);
                                                        notification.subscribe(NotificationType.MEDIA, null);
                                                        notification.subscribe(NotificationType.IOT, null);
                                                        notification.subscribe(NotificationType.USER_INPUT, null);

                                                        notification.addApplicationListener(new NotificationManager.Listener() {
                                                            @Override
                                                            public void onNotification(JSONObject event) {

                                                                Log.d(TAG, "publish APPLICATION event : " + event);
                                                                MqttHandler mqttHandler = MqttHandler.getInstance(BluemixBridgeService.this);
                                                                mqttHandler.publish(TopicFactory.getEventTopic(NotificationType.APPLICATION.toString()), event.toString(), false, 0);

                                                            }
                                                        });

                                                        notification.addMediaListener(new NotificationManager.Listener() {
                                                            @Override
                                                            public void onNotification(JSONObject event) {

                                                                Log.d(TAG, "publish MEDIA event : " + event);
                                                                MqttHandler mqttHandler = MqttHandler.getInstance(BluemixBridgeService.this);
                                                                mqttHandler.publish(TopicFactory.getEventTopic(NotificationType.MEDIA.toString()), event.toString(), false, 0);

                                                            }
                                                        });

                                                        notification.addMessageListener(new NotificationManager.Listener() {
                                                            @Override
                                                            public void onNotification(JSONObject event) {

                                                                Log.d(TAG, "publish MESSAGE event : " + event);
                                                                MqttHandler mqttHandler = MqttHandler.getInstance(BluemixBridgeService.this);
                                                                mqttHandler.publish(TopicFactory.getEventTopic(NotificationType.MESSAGE.toString()), event.toString(), false, 0);

                                                            }
                                                        });

                                                        notification.addUserInputListener(new NotificationManager.Listener() {
                                                            @Override
                                                            public void onNotification(JSONObject event) {

                                                                Log.d(TAG, "publish USER_INPUT event : " + event);
                                                                MqttHandler mqttHandler = MqttHandler.getInstance(BluemixBridgeService.this);
                                                                mqttHandler.publish(TopicFactory.getEventTopic(NotificationType.USER_INPUT.toString()), event.toString(), false, 0);

                                                            }
                                                        });

                                                        notification.addIotListener(new NotificationManager.Listener() {
                                                            @Override
                                                            public void onNotification(JSONObject event) {

                                                                Log.d(TAG, "publish IOT event : " + event);
                                                                MqttHandler mqttHandler = MqttHandler.getInstance(BluemixBridgeService.this);
                                                                mqttHandler.publish(TopicFactory.getEventTopic(NotificationType.IOT.toString()), event.toString(), false, 0);

                                                            }
                                                        });

                                                        // Once we have set our listeners, we can start listening for notifications.
                                                        notification.listen(new NotificationManager.CallbackConnected() {
                                                            @Override
                                                            public void onConnect() {
                                                                Log.i(TAG, "WebSockets connected");
                                                            }
                                                        });
                                                    }
                                                });
                                    }
                                });
                    }
                } catch (BboxNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };

        BboxHolder.getInstance().bboxSearch(BluemixBridgeService.this, authenticationCallback);

    }

    /**
     * Check whether the required properties are set for the app to connect to IoT.
     *
     * @return True if properties are set, false otherwise.
     */

    private boolean checkCanConnect() {
        if (app.getOrganization().equals(Constants.QUICKSTART)) {
            app.setConnectionType(Constants.ConnectionType.QUICKSTART);
            if (app.getDeviceId() == null || app.getDeviceId().equals("")) {
                return false;
            }
        } else if (app.getOrganization().equals(Constants.M2M)) {
            app.setConnectionType(Constants.ConnectionType.M2M);
            if (app.getDeviceId() == null || app.getDeviceId().equals("")) {
                return false;
            }
        } else {
            app.setConnectionType(Constants.ConnectionType.IOTF);
            if (app.getOrganization() == null || app.getOrganization().equals("") ||
                    app.getDeviceId() == null || app.getDeviceId().equals("") ||
                    app.getAuthToken() == null || app.getAuthToken().equals("")) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "service BluemixBridgeService destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return bboxIotService;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * Called when a client activity is unbinding from Service
     *
     * @param intent service intent
     * @return
     */
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
