package com.cajetan.youtubeplus.utils;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;

import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_OK;

public class YouTubeData implements EasyPermissions.PermissionCallbacks {
    private GoogleAccountCredential mCredential;

    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {YouTubeScopes.YOUTUBE_READONLY};

    private String mVideoId = "";

    private Activity mActivity;

    public YouTubeData(Activity parentActivity) {
        mCredential = GoogleAccountCredential.usingOAuth2(
                parentActivity.getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        mActivity = parentActivity;
    }

    public void receiveVideoData(String videoId) {
        mVideoId = videoId;
        getResultsFromApi();
    }

    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable())
            acquireGooglePlayServices();
        else if (mCredential.getSelectedAccountName() == null)
            chooseAccount();
        else if (!isDeviceOnline())
            Log.d("YouTubeData", "Device is not online");
        else
            new VideoDataTask(mCredential).execute(mVideoId);
    }

    // TODO: some fixes
    public void receiveSearchResults(String search) {
         if (!isGooglePlayServicesAvailable())
            acquireGooglePlayServices();
        else if (mCredential.getSelectedAccountName() == null)
            chooseAccount();
        else if (!isDeviceOnline())
            Log.d("YouTubeData", "Device is not online");
        else
            new VideoSearchTask(mCredential).execute(search);
    }

    private class VideoSearchTask extends AsyncTask<String, Void, String> {
        private com.google.api.services.youtube.YouTube mService;
        private Exception mLastError = null;

        VideoSearchTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            mService = new com.google.api.services.youtube.YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("YouTube Plus")
                    .build();
        }

        @Override
        protected String doInBackground(String... keywords) {
            try {
                Toast.makeText(mActivity, "TRYING!!", Toast.LENGTH_LONG).show();

                List<SearchResult> searchResults =  mService.search()
                        .list("snippet")
                        .setMaxResults(25L)
                        .setQ(keywords[0])
                        .setType("")
                        .execute()
                        .getItems();

                StringBuilder result = new StringBuilder();

                for (SearchResult r : searchResults)
                    result.append(r.getSnippet().getTitle());

                return result.toString();
            } catch (Exception e) {
                Log.e("YouTubeData", "Exception: " + e.toString());
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(mActivity, "DONE!!", Toast.LENGTH_LONG).show();

            if (mActivity instanceof VideoSearchListener)
                ((VideoSearchListener) mActivity).onSearchResultsReceived(result);
            else
                throw new UnsupportedOperationException("Activity must implement VideoSearchListener.");
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(mActivity, "CANCELLED!!", Toast.LENGTH_LONG).show();

            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    Toast.makeText(mActivity, "GooglePlayServices not available.", Toast.LENGTH_LONG).show();
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    mActivity.startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            YouTubeData.REQUEST_AUTHORIZATION);
                } else {
                    Log.e("YouTubeData", "Error occurred: " + mLastError.getMessage());
                }
            } else {
                Log.e("YouTubeData", "Request cancelled");
            }
        }
    }

    private class VideoDataTask extends AsyncTask<String, Void, Video> {
        private com.google.api.services.youtube.YouTube mService;
        private Exception mLastError = null;

        VideoDataTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            mService = new com.google.api.services.youtube.YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("YouTube Plus")
                    .build();
        }

        @Override
        protected Video doInBackground(String... videoIds) {
            try {
                return mService.videos()
                        .list("snippet")
                        .setId(videoIds[0])
                        .execute()
                        .getItems()
                        .get(0);
            } catch (Exception e) {
                Log.e("YouTubeData", "Exception: " + e.toString());
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Video output) {
            if (mActivity instanceof VideoDataListener)
                ((VideoDataListener) mActivity).onVideoDataReceived(output);
            else
                throw new UnsupportedOperationException("Activity must implement VideoDataListener.");
        }

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    Toast.makeText(mActivity, "GooglePlayServices not available.", Toast.LENGTH_LONG).show();
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    mActivity.startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            YouTubeData.REQUEST_AUTHORIZATION);
                } else {
                    Log.e("YouTubeData", "Error occurred: " + mLastError.getMessage());
                }
            } else {
                Log.e("YouTubeData", "Request cancelled");
            }
        }
    }

    public void onParentActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(mActivity,
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.",
                            Toast.LENGTH_LONG).show();
                } else {
                    getResultsFromApi();
                }

                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                mActivity.getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK)
                    getResultsFromApi();

                break;
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                mActivity, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = mActivity.getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                mActivity.startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    mActivity,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(mActivity);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(mActivity);

        if (apiAvailability.isUserResolvableError(connectionStatusCode))
            Toast.makeText(mActivity, "GooglePlayServices not available.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) { }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) { }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    public interface VideoDataListener {
        void onVideoDataReceived(Video videoData);
    }

    public interface VideoSearchListener {
        void onSearchResultsReceived(String results);
    }
}
