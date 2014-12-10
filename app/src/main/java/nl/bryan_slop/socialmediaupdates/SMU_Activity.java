package nl.bryan_slop.socialmediaupdates;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;
import android.widget.*;
import android.content.*;
import android.app.*;
import org.apache.http.*;
import java.util.*;
import org.apache.http.message.*;


public class SMU_Activity extends ActionBarActivity {

	protected SMU_Application mApp = null;
    public static DisplayMetrics displayMetrics;
    public static int uniqueId = 0;
    protected RetainFragment dataFragment;
    public String RETAIN_FRAGMENT_TAG = "RetainFragment";

    // Constants and global vars
    public static final String DEBUG_TAG = "Social Media Updates";
    public static final int OAUTH2_ACTION_NEW = 0;
    public static final int OAUTH2_ACTION_EXTEND = 1;

    // Settings
    public static final String APP_SETTINGS_STRETCH_IMAGES = "StretchImages";

    // Constants to specify used services
	public static final int SERVICE_LINKEDIN = 0;
    public static boolean LINKEDIN_HAS_ACCESS_TOKEN = false;
	public static final int SERVICE_FACEBOOK = 1;
	public static boolean FACEBOOK_HAS_ACCESS_TOKEN = false;
    public static final int SERVICE_TWITTER = 2;
    public static boolean TWITTER_HAS_ACCESS_TOKEN = false;
	public static final int SERVICE_GOOGLEPLUS = 3;
	public static boolean GOOGLEPLUS_HAS_ACCESS_TOKEN = false;

    // OAuth 2.0 SharedPreferences
    public static SharedPreferences mOAuth2 = null;

    // OAuth 2.0 Settings for SharedPreferences
    public static final String OAUTH2_SETTINGS = "OAuth2_Old";
    public static final String OAUTH2_SETTINGS_AUTH_CODE = "AuthCode";
	// LinkedIn Settings
    public static final String OAUTH2_SETTINGS_ACCESS_TOKEN_LINKEDIN = "AccessTokenLinkedIn";
    public static final String OAUTH2_SETTINGS_ACCESS_TOKEN_EXPIRES_LINKEDIN = "AccessTokenExpiresLinkedIn";
	// Facebook Settings
	public static final String OAUTH2_SETTINGS_ACCESS_TOKEN_FACEBOOK = "AccessTokenFacebook";
    public static final String OAUTH2_SETTINGS_ACCESS_TOKEN_EXPIRES_FACEBOOK = "AccessTokenExpiresFacebook";

    // OAut2 instances and factory
    public static final OAuth2Factory oAuth2Factory = new OAuth2Factory();
    public static OAuth2 linkedInOAuth2;
    public static OAuth2 facebookOAuth2;
    public static OAuth2 twitterOAuth2;
    public static OAuth2 googlePlusOAuth2;

    // ImageLoader
    protected ImageLoader imageLoader = new ImageLoader();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
		mApp = (SMU_Application) this.getApplicationContext();
        SMU_Activity.displayMetrics = getApplicationContext().getResources().getDisplayMetrics();

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		
		/*LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View actionBarCustomView = inflater.inflate(R.layout.actionbar, null);
		
		actionBar.setCustomView(actionBarCustomView);
		actionBar.setDisplayShowCustomEnabled(true);*/
		
//        final String[] socialMediaServices = getResources().getStringArray(R.array.social_media_services);
//        for(int i = 0; i < socialMediaServices.length; i++) {
//            if(socialMediaServices[i].contains("linkedin")) {
//                SERVICE_LINKEDIN = i;
//            } else if(socialMediaServices[i].contains("facebook")) {
//				SERVICE_FACEBOOK = i;
//			} else if(socialMediaServices[i].contains("googleplus")) {
//				SERVICE_GOOGLEPLUS = i;
//			} else if(socialMediaServices[i].contains("twitter")) {
//				SERVICE_TWITTER = i;
//			}
//        }

        FragmentManager fm = getSupportFragmentManager();
        dataFragment = (RetainFragment) fm.findFragmentByTag(RETAIN_FRAGMENT_TAG);

        if(dataFragment == null) {
            dataFragment = new RetainFragment();
            fm.beginTransaction().add(dataFragment, RETAIN_FRAGMENT_TAG).commit();
            dataFragment.setImageLoader(imageLoader);
        }

        imageLoader = dataFragment.getImageLoader();

        linkedInOAuth2 = oAuth2Factory.getOAuth2Instance(SERVICE_LINKEDIN);
        facebookOAuth2 = oAuth2Factory.getOAuth2Instance(SERVICE_FACEBOOK);
        twitterOAuth2 = oAuth2Factory.getOAuth2Instance(SERVICE_TWITTER);
        googlePlusOAuth2 = oAuth2Factory.getOAuth2Instance(SERVICE_GOOGLEPLUS);

        mOAuth2 = getSharedPreferences(OAUTH2_SETTINGS, Context.MODE_PRIVATE);
        checkAccessTokens();
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		mApp.setCurrentActivity(this);
	}

	@Override
	protected void onPause() {
		clearReferences();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
        super.onDestroy();
		clearReferences();
        dataFragment.setImageLoader(imageLoader);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.updates, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
			goToSettingsPage();
            return true;
        } else if(id == R.id.action_create_comment) {
            startActivity(new Intent(getApplicationContext(), SMU_Activity_CreateComment.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void goToSettingsPage() {
        startActivity(new Intent(getApplicationContext(), SMU_Activity_Settings.class));
    }

    public static Integer[] getDisplaySizes() {
        Integer[] displaySizes = new Integer[2];

        WindowManager wm = (WindowManager) SMU_Application.getContext().getSystemService(WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if(android.os.Build.VERSION.SDK_INT >= 13) {
            Point size = new Point();
            display.getSize(size);

            displaySizes[0] = size.x;
            displaySizes[1] = size.y;
        } else {
            displaySizes[0] = display.getWidth();
            displaySizes[1] = display.getHeight();
        }

        return displaySizes;
    }

    public static int getDisplayWidth() {
        Integer[] displaySizes = getDisplaySizes();

        return displaySizes[0];
    }

    public static int getDisplayHeight() {
        Integer[] displaySizes = getDisplaySizes();

        return displaySizes[1];
    }

    public static String getUniqueId() {
        SMU_Activity.uniqueId++;
        return String.valueOf(SMU_Activity.uniqueId);
    }

    public static int calculatePixels(int widthInDp) {
        return (int) ((float) widthInDp * SMU_Activity.displayMetrics.density);
    }

    /**
     * Show the authorization dialog for OAuth 2.0
     */
    public void showAuthDialog(final int service, final TextView textView) {
		
		String authURL = null;
		if(service == SERVICE_LINKEDIN) {
			authURL = linkedInOAuth2.getFullAuthUrl();
		} else if(service == SERVICE_FACEBOOK) {
			authURL = facebookOAuth2.getFullAuthUrl();
		}
		
        final Dialog authDialog = new Dialog(SMU_Activity.this);
        authDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        authDialog.setContentView(R.layout.auth_dialog);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(authDialog.getWindow().getAttributes());
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        authDialog.getWindow().setAttributes(lp);

        WebView authWebView = (WebView) authDialog.findViewById(R.id.WebView_Authorize);
        authWebView.getSettings().setJavaScriptEnabled(true);
        authWebView.loadUrl(authURL);
//        Log.d(DEBUG_TAG, "Address WEBVIEW AUTHURL: " + authURL);
//        String test = "http://localhost/?access_token=CAAKEQaJnsCgBAIwulfZClzquRrUQKYIc8wZBpxISHB9lZB5dhK1pKZCiGvzJSsGVmfbZBm8aUqrOSZBkeZBj1cdMKW3yEhMOuSPmoGxAVdDnS7suGIvjq0fmf5pp9tYPdTLlEBHm3Gb6eAtZCL02EHSlTSr1VX7EnbZBWjaWbZCuvNqzxA1qKjPvDREtG7JJiBk4bceCmZCQORy0a1WXbfxHaZB3&expires=5139895";
//        String accessToken = null;
//        String expiresIn = null;
//        try {
//            Uri uri = Uri.parse(test);
//            accessToken = uri.getQueryParameter("access_token");
//            expiresIn = uri.getQueryParameter("expires");
//
//        } catch (Exception e) {
//            Log.e(DEBUG_TAG, "No URI!", e);
//        }
//
//        Log.d(DEBUG_TAG,"Auth URL: " + authURL + "\n" +
//                "Access Token: " + accessToken + "\n" +
//                "Expires in: " + expiresIn);
//		Toast.makeText(this,
//		"Auth URL: " + authURL,
//		Toast.LENGTH_LONG).show();

        authWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView webView, String url) {
//                Log.d(DEBUG_TAG, "Address Response url: " + url);

                if(url.contains("?code=")) {

                    Uri uri = Uri.parse(url);
                    String authCode = uri.getQueryParameter("code");

                    //if(!mOAuth2.getString(OAUTH2_SETTINGS_AUTH_CODE, "").equals(authCode)) {

                        Editor editor = mOAuth2.edit();
                        editor.putString(OAUTH2_SETTINGS_AUTH_CODE, authCode);
                        editor.commit();

                        /*
                        Log.d(DEBUG_TAG, "Address Response url: " + url);
                        Toast.makeText(getApplicationContext(),
                                "Auth code: " + authCode,
                                Toast.LENGTH_LONG).show();
                        */

                    authDialog.dismiss();

                        GetAccessToken getAccessToken = new GetAccessToken(textView);
                        getAccessToken.execute(service, authCode, OAUTH2_ACTION_NEW);
                        try {
                            JSONObject result = getAccessToken.get();
                            saveAccessToken(service, result);

//                            Log.d(DEBUG_TAG, "Result json: " + result);
                        } catch (Exception e) {
                            Log.e(DEBUG_TAG, "Error retrieving/saving JSON Result from AsyncTask!", e);
                        }
                    //}
                } else if(url.contains("error=")) {
                /*Toast.makeText(getApplicationContext(),
                    "Something went wrong!",
                    Toast.LENGTH_LONG).show();*/
                    authDialog.dismiss();
                }

            }

        });
        authDialog.show();
        authDialog.setCancelable(true);
    }

    /**
     * Check if the access tokens are still valid and not expired
     */
    public void checkAccessTokens() {
        Time time = new Time();
        time.setToNow();
        long timeInSeconds = time.toMillis(false) / 1000;

        // Check LinkedIn Access Token
        if(mOAuth2.contains(OAUTH2_SETTINGS_ACCESS_TOKEN_EXPIRES_LINKEDIN)) {

            if(mOAuth2.getLong(OAUTH2_SETTINGS_ACCESS_TOKEN_EXPIRES_LINKEDIN, 0) >= timeInSeconds) {
                LINKEDIN_HAS_ACCESS_TOKEN = true;
				linkedInOAuth2.setAccessToken(mOAuth2.getString(OAUTH2_SETTINGS_ACCESS_TOKEN_LINKEDIN, ""));
            } else {
                LINKEDIN_HAS_ACCESS_TOKEN = false;
            }
        }
		
		// Check Facebook Access Token
		if(mOAuth2.contains(OAUTH2_SETTINGS_ACCESS_TOKEN_EXPIRES_FACEBOOK)) {

            if(mOAuth2.getLong(OAUTH2_SETTINGS_ACCESS_TOKEN_EXPIRES_FACEBOOK, 0) >= timeInSeconds) {
                FACEBOOK_HAS_ACCESS_TOKEN = true;
				facebookOAuth2.setAccessToken(mOAuth2.getString(OAUTH2_SETTINGS_ACCESS_TOKEN_FACEBOOK, ""));
            } else {
                FACEBOOK_HAS_ACCESS_TOKEN = false;
            }
        }
    }

    public boolean hasAnyService() {

        if(FACEBOOK_HAS_ACCESS_TOKEN || LINKEDIN_HAS_ACCESS_TOKEN || TWITTER_HAS_ACCESS_TOKEN ||
                GOOGLEPLUS_HAS_ACCESS_TOKEN) {
            return true;
        }

        return false;
    }

    /**
     * Set the texts at the settings page
     * @param service
     * @param textView
     */
    public void setSettingsText(int service, TextView textView) {

        Time time = new Time();
        time.setToNow();
        long timeInSeconds = time.toMillis(false) / 1000;

        if(service == SERVICE_LINKEDIN) {

            if (LINKEDIN_HAS_ACCESS_TOKEN) {

                if(textView != null) {

                    String fullName = "";

                    try {
                        FetchData apiCall = new FetchData();
                        apiCall.execute(SERVICE_LINKEDIN, "/v1/people/~:(first-name,last-name)");
                        JSONObject json = apiCall.get();

//                        Log.d(DEBUG_TAG, "Get Name json: " + json);

                        fullName = json.getString("firstName") + " " + json.getString("lastName");
                    } catch (Exception e) {
                        Log.e(DEBUG_TAG, "Failed to do api request.", e);
                    }
                    textView.setText(getResources().getString(R.string.settings_linkedin_connected) + " " + fullName);
					textView.setTextColor(getResources().getColorStateList(R.color.text_color_red_background));
                    ((LinearLayout) textView.getParent().getParent()).setBackgroundResource(R.drawable.background_red);
                }


            } else {

                if(textView != null) {
                    textView.setText(getResources().getString(R.string.settings_linkedin));
					textView.setTextColor(getResources().getColorStateList(R.color.text_color_light_grey_background));
                    ((LinearLayout)textView.getParent().getParent()).setBackgroundResource(R.drawable.background_light_grey);
                }
            }
//            Log.d(DEBUG_TAG, "Expires " + mOAuth2.getLong(OAUTH2_SETTINGS_ACCESS_TOKEN_EXPIRES_LINKEDIN, 0) + " <> " + timeInSeconds + " timeInSeconds");
        } else if(service == SERVICE_FACEBOOK) {

            if (FACEBOOK_HAS_ACCESS_TOKEN) {

                if(textView != null) {

                    String fullName = "";

                    try {
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair("fields", "name"));
						
                        FetchData apiCall = new FetchData();
                        apiCall.execute(SERVICE_FACEBOOK, "/me", params);
                        JSONObject json = apiCall.get();

                        fullName = json.getString("name");
//                        Log.d(DEBUG_TAG, "json: " + json);
                    } catch (Exception e) {
                        Log.e(DEBUG_TAG, "Failed to do api request.", e);
                    }
                    textView.setText(getResources().getString(R.string.settings_facebook_connected) + " " + fullName);
					textView.setTextColor(getResources().getColorStateList(R.color.text_color_red_background));
                    ((LinearLayout) textView.getParent().getParent()).setBackgroundResource(R.drawable.background_red);
                }


            } else {

                if(textView != null) {
                    textView.setText(getResources().getString(R.string.settings_facebook));
					textView.setTextColor(getResources().getColorStateList(R.color.text_color_light_grey_background));
                    ((LinearLayout)textView.getParent().getParent()).setBackgroundResource(R.drawable.background_light_grey);
                }
            }
//            Log.d(DEBUG_TAG, "Expires " + mOAuth2.getLong(OAUTH2_SETTINGS_ACCESS_TOKEN_EXPIRES_FACEBOOK, 0) + " <> " + timeInSeconds + " timeInSeconds");
        }

    }

    /**
     * Save Access Token to the Shared Preferences.
     * @param positionService An Integer specifying the used service.
     * @param result JSONObject The result retrieved from the server as JSONObject.
     * @throws JSONException Exception when having an error parsing the JSONObject.
     */
    public void saveAccessToken(int positionService, JSONObject result) throws JSONException {

        Time time = new Time();
        time.setToNow();
        Editor editor = mOAuth2.edit();
//        Log.d(DEBUG_TAG, "Current time (sec): " + (time.toMillis(false) / 1000));

        if(positionService == SERVICE_LINKEDIN) {
            editor.putString(OAUTH2_SETTINGS_ACCESS_TOKEN_LINKEDIN, result.getString("access_token"));
            editor.putLong(OAUTH2_SETTINGS_ACCESS_TOKEN_EXPIRES_LINKEDIN, (result.getLong("expires_in") + (time.toMillis(false) / 1000) - (60 * 60 * 24 * 10)));
        } else if(positionService == SERVICE_FACEBOOK) {
			editor.putString(OAUTH2_SETTINGS_ACCESS_TOKEN_FACEBOOK, result.getString("access_token"));
            editor.putLong(OAUTH2_SETTINGS_ACCESS_TOKEN_EXPIRES_FACEBOOK, (result.getLong("expires_in") + (time.toMillis(false) / 1000) - (60 * 60 * 24 * 10)));
		}
        editor.commit();
//        Log.d(DEBUG_TAG, "AccessToken: " + mOAuth2.getString(OAUTH2_SETTINGS_ACCESS_TOKEN_FACEBOOK, "No Token"));
    }
	
	private void clearReferences(){
        Activity currActivity = mApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            mApp.setCurrentActivity(null);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static class RetainFragment extends Fragment {

        // ImageLoader we want to retain
        private ImageLoader imageLoader;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        public void setImageLoader(ImageLoader imageLoader) {
            this.imageLoader = imageLoader;
        }

        public ImageLoader getImageLoader() {
            return this.imageLoader;
        }
    }

    private class GetAccessToken extends AsyncTask<Object, String, JSONObject> {
        int service;
        TextView textView;

        public GetAccessToken(TextView textView) {
            this.textView = textView;
        }

        @Override
        protected JSONObject doInBackground(Object... params) {

            JSONObject json = null;
            service = (Integer) params[0];
            String authCode = (String) params[1];
            int action = OAUTH2_ACTION_NEW;

            if(params[2] != null) {
                action = (Integer) params[2];
            }

            if(service == SERVICE_LINKEDIN) {
//               Log.d(DEBUG_TAG, "Linkedin test");
                try {
                    json = new JSONObject(linkedInOAuth2.getAccessToken(authCode));
                } catch (Exception e) {
                    Log.e(DEBUG_TAG, "Error converting result to JSON", e);
                    return null;
                }
            } else if(service == SERVICE_FACEBOOK) {
                json = new JSONObject();
                Uri tempUri = Uri.parse("http://localhost/?" + facebookOAuth2.getAccessToken(authCode));
                try {
                    json.put("access_token", tempUri.getQueryParameter("access_token"));
                    json.put("expires_in", tempUri.getQueryParameter("expires"));
                } catch (JSONException e) {
                    Log.e(DEBUG_TAG, "Could not retrieve access token from facebook!", e);
                }
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {

            checkAccessTokens();
            setSettingsText(service, textView);
        }

    }

    protected class FetchData extends AsyncTask<Object, String, JSONObject> {
        int service;

        @Override
        protected JSONObject doInBackground(Object... params) {

            service = (Integer) params[0];
            String apiRequest = (String) params[1];
			List<NameValuePair> apiParams = null;
			if(params.length == 3) {
				apiParams = (List<NameValuePair>) params[2];
			}
            JSONObject json = null;

            if(service == SERVICE_LINKEDIN) {
                json = linkedInOAuth2.fetchData(apiRequest);
            } else if(service == SERVICE_FACEBOOK) {
				json = facebookOAuth2.fetchData(apiRequest, apiParams);
			}

            return json;
        }
    }

    protected class FetchDataWithResult extends AsyncTask<String, Void, JSONObject> {
        int service;
        List<NameValuePair> apiParams;

        public FetchDataWithResult(List<NameValuePair> params, int service) {
            this.service = service;
            this.apiParams = params;
        }

        public FetchDataWithResult(int service) {
            this.service = service;
            this.apiParams = new ArrayList<NameValuePair>();
        }

        @Override
        protected JSONObject doInBackground(String... params) {

            String request = params[0];

            JSONObject json = null;

            if(service == SERVICE_LINKEDIN) {
                json = linkedInOAuth2.fetchData(request, apiParams);
            } else if(service == SERVICE_FACEBOOK) {
                json = facebookOAuth2.fetchData(request, apiParams);
            }

            return json;
        }
    }

    protected class LoadProfilePictures extends AsyncTask<String, Void, Boolean> {
        int service;
        ImageView imageView;

        public LoadProfilePictures(int service, ImageView imageView) {
            this.service = service;
            this.imageView = imageView;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String imageUrl = "";
            String id = params[0];

            if(SERVICE_LINKEDIN == service) {
                try {
                    JSONObject jsonImage = linkedInOAuth2.fetchData("/v1/people/id=" + id + ":(picture-url)");
                    imageUrl = jsonImage.getString("pictureUrl");
                } catch (JSONException e) {
                    Log.e(DEBUG_TAG, "Could not get profile picture url LinkedIn.", e);
                }
            } else if(SERVICE_FACEBOOK == service) {

                try {
                    int pictureWidth = calculatePixels(75);
                    int pictureHeight = pictureWidth;

                    List<NameValuePair> apiParams = new ArrayList<NameValuePair>();
                    apiParams.add(new BasicNameValuePair("redirect", "false"));
                    apiParams.add(new BasicNameValuePair("width", String.valueOf(pictureWidth)));
                    apiParams.add(new BasicNameValuePair("height", String.valueOf(pictureHeight)));

                    JSONObject jsonImage = facebookOAuth2.fetchData("/" + id + "/picture", apiParams);
                    imageUrl = jsonImage.getJSONObject("data").getString("url");

                } catch(Exception e) {
                    Log.e(DEBUG_TAG, "Could not get profile picture url from Facebook.", e);
                }
            }

            imageLoader.loadBitmap(imageUrl, imageView);

            return true;
        }
    }

}