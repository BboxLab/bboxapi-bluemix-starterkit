/*******************************************************************************
 * Copyright (c) 2014 IBM Corp.
 * <p/>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * <p/>
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * <p/>
 * Contributors:
 * Mike Robertson - initial contribution
 *******************************************************************************/
package fr.bouyguestelecom.tv.bridge.bluemix;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.bouyguestelecom.tv.bridge.bluemix.utils.Constants;
import fr.bouyguestelecom.tv.bridge.bluemix.utils.IoTProfile;

/**
 * Main class for the IoT Starter application. Stores values for
 * important device and application information.
 */
public class IoTStarterApplication extends Application {
    private final static String TAG = IoTStarterApplication.class.getName();

    // Values needed for connecting to IoT
    private String organization;
    private String deviceId;
    private String authToken;
    private Constants.ConnectionType connectionType;

    private SharedPreferences settings;

    // Application state variables
    private boolean connected = false;

    private IoTProfile profile;
    private List<IoTProfile> profiles = new ArrayList<IoTProfile>();
    private ArrayList<String> profileNames = new ArrayList<String>();

    /**
     * Called when the application is created. Initializes the application.
     */
    @Override
    public void onCreate() {
        Log.d(TAG, ".onCreate() entered");
        super.onCreate();

        settings = getSharedPreferences(Constants.SETTINGS, 0);

        loadProfiles();
    }

    /**
     * Called when old application stored settings values are found.
     * Converts old stored settings into new profile setting.
     */
    private void createNewDefaultProfile() {
        Log.d(TAG, "organization not null. compat profile setup");
        // If old stored property settings exist, use them to create a new default profile.
        String organization = settings.getString(Constants.ORGANIZATION, null);
        String deviceId = settings.getString(Constants.DEVICE_ID, null);
        String authToken = settings.getString(Constants.AUTH_TOKEN, null);
        IoTProfile newProfile = new IoTProfile("default", organization, deviceId, authToken);
        this.profiles.add(newProfile);
        this.profileNames.add("default");

        // Put the new profile into the store settings and remove the old stored properties.
        Set<String> defaultProfile = newProfile.convertToSet();

        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet(newProfile.getProfileName(), defaultProfile);
        editor.remove(Constants.ORGANIZATION);
        editor.remove(Constants.DEVICE_ID);
        editor.remove(Constants.AUTH_TOKEN);
        editor.commit();

        this.setProfile(newProfile);
        this.setOrganization(newProfile.getOrganization());
        this.setDeviceId(newProfile.getDeviceID());
        this.setAuthToken(newProfile.getAuthorizationToken());

        return;
    }

    /**
     * Load existing profiles from application stored settings.
     */
    private void loadProfiles() {
        // Compatability
        if (settings.getString(Constants.ORGANIZATION, null) != null) {
            createNewDefaultProfile();
            return;
        }

        String profileName;
        if ((profileName = settings.getString("iot:selectedprofile", null)) == null) {
            profileName = "";
        }

        Map<String, ?> profileList = settings.getAll();
        if (profileList != null) {
            for (String key : profileList.keySet()) {
                if (key.equals("iot:selectedprofile")) {
                    continue;
                }
                Set<String> profile;// = new HashSet<String>();
                try {
                    // If the stored property is a Set<String> type, parse the profile and add it to the list of
                    // profiles.
                    if ((profile = settings.getStringSet(key, null)) != null) {
                        Log.d(TAG, "profile name: " + key);
                        IoTProfile newProfile = new IoTProfile(profile);
                        this.profiles.add(newProfile);
                        this.profileNames.add(newProfile.getProfileName());

                        if (newProfile.getProfileName().equals(profileName)) {
                            this.setProfile(newProfile);
                            this.setOrganization(newProfile.getOrganization());
                            this.setDeviceId(newProfile.getDeviceID());
                            this.setAuthToken(newProfile.getAuthorizationToken());
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }
    }

    /**
     * Overwrite an existing profile in the stored application settings.
     *
     * @param newProfile The profile to save.
     */
    public void overwriteProfile(IoTProfile newProfile) {
        // Put the new profile into the store settings and remove the old stored properties.
        Set<String> profileSet = newProfile.convertToSet();

        SharedPreferences.Editor editor = settings.edit();
        editor.remove(newProfile.getProfileName());
        editor.putStringSet(newProfile.getProfileName(), profileSet);
        editor.commit();

        for (IoTProfile existingProfile : profiles) {
            if (existingProfile.getProfileName().equals(newProfile.getProfileName())) {
                profiles.remove(existingProfile);
                break;
            }
        }
        profiles.add(newProfile);
    }

    /**
     * Save the profile to the application stored settings.
     *
     * @param profile The profile to save.
     */
    public void saveProfile(IoTProfile profile) {
        // Put the new profile into the store settings and remove the old stored properties.
        Set<String> profileSet = profile.convertToSet();

        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet(profile.getProfileName(), profileSet);
        editor.commit();
        this.profiles.add(profile);
        this.profileNames.add(profile.getProfileName());
    }

    /**
     * Remove all saved profile information.
     */
    public void clearProfiles() {
        this.profiles.clear();
        this.profileNames.clear();

        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void setConnectionType(Constants.ConnectionType type) {
        this.connectionType = type;
    }

    public Constants.ConnectionType getConnectionType() {
        return this.connectionType;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public IoTProfile getProfile() {
        return profile;
    }

    public void setProfile(IoTProfile profile) {
        this.profile = profile;
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("iot:selectedprofile", profile.getProfileName());
        editor.commit();
    }

    public List<IoTProfile> getProfiles() {
        return profiles;
    }

    public ArrayList<String> getProfileNames() {
        return profileNames;
    }
}
