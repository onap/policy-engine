/*-
 * ============LICENSE_START=======================================================
 * PolicyEngineAPI
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.std;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.onap.policy.api.NotificationHandler;
import org.onap.policy.api.NotificationScheme;
import org.onap.policy.api.NotificationType;
import org.onap.policy.api.PDPNotification;
import org.onap.policy.common.logging.flexlogger.FlexLogger;
import org.onap.policy.common.logging.flexlogger.Logger;
import org.onap.policy.xacml.api.XACMLErrorConstants;

public class AutoClientEnd extends WebSocketClient {
    private static volatile StdPDPNotification notification = null;
    private static volatile StdPDPNotification oldNotification = null;
    private static volatile AutoClientEnd client = null;
    private static volatile NotificationScheme scheme = null;
    private static volatile NotificationHandler handler = null;
    private static volatile String url = null;
    private static volatile boolean status = false;
    private static volatile boolean stop = false;
    private static volatile boolean error = false;
    private static volatile boolean restartNeeded = false;
    private static volatile ScheduledExecutorService restartExecutorService = null;
    private static final Logger logger = FlexLogger.getLogger(AutoClientEnd.class.getName());

    private AutoClientEnd(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onMessage(String msg) {
        logger.info("Received Auto Notification from : " + getURI() + ", Notification: " + msg);
        try {
            AutoClientEnd.notification = NotificationUnMarshal.notificationJSON(msg);
        } catch (Exception e) {
            logger.error("PE500 " + e);
        }
        try {
            NotificationStore.recordNotification(notification);
        } catch (Exception e) {
            logger.error(e);
        }
        if (AutoClientEnd.oldNotification != AutoClientEnd.notification) {
            AutoClientEnd.oldNotification = AutoClientEnd.notification;
            callHandler();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.info("AutoClientEnd disconnected from: " + getURI() + "; Code: " + code + ", reason :  " + reason);
        AutoClientEnd.restartNeeded = true;
    }

    @Override
    public void onError(Exception ex) {
        logger.error("XACMLErrorConstants.ERROR_PROCESS_FLOW + Error connecting to: " + getURI()
                + ", Exception occured ...\n" + ex);
        AutoClientEnd.restartNeeded = true;
    }

    @Override
    public void onOpen(ServerHandshake arg0) {
        restartNeeded = false;
        logger.info("Auto Notification Session Started... " + getURI());
    }

    private static void restart() {
        try {
            if (client != null && restartNeeded && !stop) {
                logger.info("Auto Notification Session Restarting ... " + getUrl());
                client.reconnect();
            }
        } catch (Exception e) {
            logger.info("Auto Notification Session Error Started... " + getUrl());
        }
    }

    /**
     * Sets the auto.
     *
     * @param scheme the scheme
     * @param handler the handler
     */
    public static void setAuto(NotificationScheme scheme, NotificationHandler handler) {
        logger.info("Auto Notification setAuto, scheme: " + scheme);
        AutoClientEnd.scheme = scheme;
        AutoClientEnd.handler = handler;
    }

    public static void setScheme(NotificationScheme scheme) {
        AutoClientEnd.scheme = scheme;
    }

    public static boolean getStatus() {
        return AutoClientEnd.status;
    }

    public static String getUrl() {
        return AutoClientEnd.url;
    }

    /**
     * Start.
     *
     * @param url the url
     */
    public static void start(String url) {
        AutoClientEnd.url = url;

        if (scheme == null || handler == null || !(scheme.equals(NotificationScheme.AUTO_ALL_NOTIFICATIONS)
                || scheme.equals(NotificationScheme.AUTO_NOTIFICATIONS)) || AutoClientEnd.client != null) {
            return;
        }

        if (url.contains("https")) {
            url = url.replaceAll("https", "wss");
        } else {
            url = url.replaceAll("http", "ws");
        }

        // Stop and Start needs to be done.
        try {
            logger.info("Starting Auto Notification with the PDP server : " + url);
            client = new AutoClientEnd(new URI(url + "notifications"));
            client.setConnectionLostTimeout(0);
            client.connect();
            status = true;
            restartExecutorService = Executors.newSingleThreadScheduledExecutor();
            Runnable task = AutoClientEnd::restart;
            restartExecutorService.scheduleAtFixedRate(task, 60, 60, TimeUnit.SECONDS);

            if (error) {
                // will not trigger. leave it in to later add checks
                // The URL's will be in Sync according to design Spec.
                ManualClientEnd.start(AutoClientEnd.url);
                StdPDPNotification notification = NotificationStore.getDeltaNotification(
                        (StdPDPNotification) ManualClientEnd.result(NotificationScheme.MANUAL_ALL_NOTIFICATIONS));
                if (notification.getNotificationType() != null && oldNotification != notification) {
                    oldNotification = notification;
                    AutoClientEnd.notification = notification;
                    callHandler();
                }
                error = false;
            }
        } catch (Exception e) {
            logger.error(XACMLErrorConstants.ERROR_SYSTEM_ERROR + e);
            status = false;
            changeUrl();
        }
    }

    private static void changeUrl() {
        // Change the PDP if it is not Up.
        stop();
        StdPolicyEngine.rotatePDPList();
        start(StdPolicyEngine.getPDPURL());
    }

    /**
     * Stop the websocket connection.
     */
    public static void stop() {
        if (client == null) {
            return;
        }
        logger.info("\n Closing Auto Notification WebSocket Connection.. ");
        stop = true;
        // first stop the restart service
        try {
            restartExecutorService.shutdown();
        } catch (Exception e1) {
            logger.info("\n AutoClientEnd: Error stoppping the restart Scheduler ");
        }

        // close the connection
        try {
            client.closeBlocking();
        } catch (Exception e) {
            logger.error("\n ERROR Closing Auto Notification WebSocket Connection.. ");
        }

        logger.info("\n Closed the Auto Notification WebSocket Connection.. ");
        client = null;
        status = false;
        stop = false;
        restartNeeded = false;
    }

    private static void callHandler() {
        logger.info("AutoClientEnd: In callHandler");
        if (handler == null || scheme == null) {
            return;
        }
        if (scheme.equals(NotificationScheme.AUTO_ALL_NOTIFICATIONS)) {
            boolean removed = false;
            boolean updated = false;
            if (notification.getRemovedPolicies() != null && !notification.getRemovedPolicies().isEmpty()) {
                removed = true;
                notification.setNotificationType(NotificationType.REMOVE);
            }
            if (notification.getLoadedPolicies() != null && !notification.getLoadedPolicies().isEmpty()) {
                updated = true;
                notification.setNotificationType(NotificationType.UPDATE);
            }
            if (removed && updated) {
                notification.setNotificationType(NotificationType.BOTH);
            }
            try {
                handler.notificationReceived(notification);
            } catch (Exception e) {
                logger.error("Error in Clients Handler Object : ", e);
            }
        } else if (scheme.equals(NotificationScheme.AUTO_NOTIFICATIONS)) {
            PDPNotification newNotification = MatchStore.checkMatch(notification);
            if (newNotification.getNotificationType() != null) {
                try {
                    handler.notificationReceived(newNotification);
                } catch (Exception e) {
                    logger.error("Error in Clients Handler Object : ", e);
                }
            }
        }
    }
}