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

import com.cajetan.youtubeplus.data.VideoData;
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
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_OK;

public class YouTubeData implements EasyPermissions.PermissionCallbacks {

    private GoogleAccountCredential mCredential;

    private static final String TAG = YouTubeData.class.getSimpleName();

    private static final long SEARCH_PAGE_SIZE = 20;

    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {YouTubeScopes.YOUTUBE_READONLY};

    private String mVideoId = "";
    private String searchQuery = "";
    private String searchPageToken = null;
    private List<VideoData> mVideoData;

    private RequestType mRequestType;

    private Activity mActivity;

//    TODO: think about a singleton here

    public YouTubeData(Activity parentActivity) {
        mCredential = GoogleAccountCredential.usingOAuth2(
                parentActivity.getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        mActivity = parentActivity;
    }

    public void receiveVideoData(String videoId) {
        mVideoId = videoId;
        mRequestType = RequestType.DATA_REQUEST;

        getResultsFromApi();
    }

    public void receiveSearchResults(String search, String pageToken) {
        searchQuery = search;
        searchPageToken = pageToken;
        mRequestType = RequestType.SEARCH_REQUEST;

        getResultsFromApi();
    }

    public void receiveFavouritesResults(List<VideoData> videoData) {
        mVideoData = videoData;
        mRequestType = RequestType.FAVOURITES_REQUEST;

        getResultsFromApi();
    }

    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable())
            acquireGooglePlayServices();
        else if (mCredential.getSelectedAccountName() == null)
            chooseAccount();
        else if (!isDeviceOnline())
            Toast.makeText(mActivity, "The device is not online.", Toast.LENGTH_SHORT).show();
        else {
            if (mRequestType == RequestType.DATA_REQUEST)
                new VideoDataTask(mCredential).execute(mVideoId);
            else if (mRequestType == RequestType.SEARCH_REQUEST)
                new VideoSearchTask(mCredential).execute(searchQuery, searchPageToken);
            else if (mRequestType == RequestType.FAVOURITES_REQUEST)
                new FavouritesTask(mCredential).execute(mVideoData.toArray(new VideoData[0]));
        }
    }

    private class VideoSearchTask extends AsyncTask<String, Void, List<Video>> {
        private com.google.api.services.youtube.YouTube mService;
        private Exception mLastError = null;
        private String mNextPageToken = null;
        private String mPreviousPageToken = null;

        VideoSearchTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            mService = new com.google.api.services.youtube.YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("YouTube Plus")
                    .build();
        }

        @Override
        protected List<Video> doInBackground(String... args) {
            Log.d(TAG, "Receiving search results..");

            try {
                String videoId = args[0];
                String nextPageToken = args[1];

                YouTube.Search.List searchList = mService.search()
                        .list("id")
                        .setMaxResults(SEARCH_PAGE_SIZE)
                        .setQ(videoId)
                        .setType("");

                if (nextPageToken != null) {
                    Log.d(TAG, "Next page token: " + nextPageToken);
                    searchList.setPageToken(nextPageToken);
                }

                SearchListResponse response = searchList.execute();

                this.mNextPageToken = response.getNextPageToken();
                mPreviousPageToken = response.getPrevPageToken();

                StringBuilder finalId = new StringBuilder();
                for (SearchResult r : response.getItems()) {
                    String id = r.getId().getVideoId();

//                  TODO: Some stuff with this, if id turns out to be null
//                  (new Video()).setSnippet(searchResults.get(0).getSnippet().);
                    if (id == null)
                        continue;

                    finalId.append(id);
                    finalId.append(',');
                }
                finalId.setLength(finalId.length()-1);

                return mService.videos()
                        .list("snippet,contentDetails")
                        .setId(finalId.toString())
                        .execute()
                        .getItems();
            } catch (Exception e) {
                Log.e("YouTubeData", "Exception: " + e.toString());
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Video> results) {
            Log.d(TAG, "Passing the results..");

            if (mActivity instanceof VideoSearchListener)
                ((VideoSearchListener) mActivity).onSearchResultsReceived(results,
                        mNextPageToken, mPreviousPageToken);
            else
                throw new UnsupportedOperationException("Activity must implement VideoSearchListener.");
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
                        // TODO: some space for performance improvement here
                        .list("snippet, contentDetails")
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

    private class FavouritesTask extends AsyncTask<VideoData, Void, List<Video>> {
        private com.google.api.services.youtube.YouTube mService;
        private Exception mLastError = null;

        FavouritesTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            mService = new com.google.api.services.youtube.YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("YouTube Plus")
                    .build();
        }

        @Override
        protected List<Video> doInBackground(VideoData... args) {
            Log.d(TAG, "Receiving search results..");

            try {
                StringBuilder finalId = new StringBuilder();
                for (VideoData data : args) {
                    finalId.append(data.getVideoId());
                    finalId.append(',');
                }
                finalId.setLength(finalId.length()-1);

                return mService.videos()
                        .list("snippet, contentDetails")
                        .setId(finalId.toString())
                        .execute().getItems();
            } catch (Exception e) {
                Log.e("YouTubeData", "Exception: " + e.toString());
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Video> results) {
            Log.d(TAG, "Passing the results..");

            if (mActivity instanceof FavouritesDataListener)
                ((FavouritesDataListener) mActivity).onFavouritesReceived(results);
            else
                throw new UnsupportedOperationException("Activity must implement FavouritesDataListener.");
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
            Log.d(TAG, "Requesting account permission");

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

    private enum RequestType {
        DATA_REQUEST,
        SEARCH_REQUEST,
        FAVOURITES_REQUEST,
    }

    public interface VideoDataListener {
        void onVideoDataReceived(Video videoData);
    }

    public interface VideoSearchListener {
        void onSearchResultsReceived(List<Video> results,
                                     String nextPageToken, String previousPageToken);
    }

    public interface FavouritesDataListener {
        void onFavouritesReceived(List<Video> results);
    }
}
