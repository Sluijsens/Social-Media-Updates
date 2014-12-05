package nl.bryan_slop.socialmediaupdates;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bryan on 6-8-2014.
 */
abstract class OAuth2 {
    public static final int ACTION_FETCH_DATA = 1;

    protected String accessToken;

    protected final String clientId; // API Key
    protected final String clientSecret; // API Secret
    protected final String scope;
    protected final String baseApiURL;
    protected final String baseAuthURL;
    protected final String baseAccessURL;

    protected final String state = new BigInteger(130, new SecureRandom()).toString(32);
    protected final String redirectUri = "http://localhost/";

    protected final String DEBUG_TAG_BASE = "Social Media Updates OAuth2.0";
    protected final String DEBUG_TAG;

    protected OAuth2(Map<String, String> data) {

        DEBUG_TAG = DEBUG_TAG_BASE + " - " + data.get("service") + ":";
        clientId = data.get("clientId");
        clientSecret = data.get("clientSecret"); // API Secret
        scope = data.get("scope"); // API Scope
        baseApiURL = data.get("baseApiURL");
        baseAuthURL = data.get("baseAuthURL");
        baseAccessURL = data.get("baseAccessURL");

    }

    /**
     * Get the access token from the server
     * @param authCode The url to send the request to.
     * @return Return a String with the result from the server.
     */
    public String getAccessToken(String authCode) {

        InputStream is;
        String result;

        try {

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("grant_type", "authorization_code"));
            params.add(new BasicNameValuePair("code", authCode));
            params.add(new BasicNameValuePair("redirect_uri", redirectUri));
            params.add(new BasicNameValuePair("client_id", clientId));
            params.add(new BasicNameValuePair("client_secret", clientSecret));

            is = sendHttpPostRequest(baseAccessURL, params);
        } catch (Exception e) {

            Log.e(DEBUG_TAG, "Error when retrieving access token!", e);
            return null;
        }

        try {
            result = convertResultToString(is);
        } catch(Exception e) {
            Log.e(DEBUG_TAG, "Error when converting result!", e);
            return null;
        }

        return result;
    }

    /**
     * Send an HTTP Post request to the server and return the response.
     * @param url The URL to send the request to.
     * @param params The parameters to send with the request.
     * @return Return an InputStream with the response.
     */
    public InputStream sendHttpPostRequest(String url, List<NameValuePair> params) throws IOException {

        boolean hasResponseType = false;

        for(NameValuePair param : params) {
            if(param.getName().equals("format")) {
                hasResponseType = true;
                break;
            }
        }

        if(hasResponseType == false) {
            params.add(new BasicNameValuePair("format", "json"));
        }

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        //Log.d(DEBUG_TAG, "Address: " + url + " httppost: " + httpPost.getURI() + " httppost apiParams: " + apiParams.toString());
        HttpResponse httpResponse = httpClient.execute(httpPost);
        HttpEntity httpEntity = httpResponse.getEntity();

        return httpEntity.getContent();
    }

    /**
     * Send an Http GET request and retrieve data from the server.
     * @param params The parameters to send with the request.
     * @param apiRequest The Api specific request to send to the server.
     * @return An InputStream with the result from the server.
     */
    public InputStream sendHttpGetRequest(String apiRequest, List<NameValuePair> params) throws IOException {

        String url = getBaseApiURL() + apiRequest + "?" + URLEncodedUtils.format(params, null);
        //Log.d(DEBUG_TAG, "API Request URL: " + url);
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);

        HttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity httpEntity = httpResponse.getEntity();

        return httpEntity.getContent();
    }

    protected abstract List<NameValuePair> prepareParams(int action, List<NameValuePair> params);

    public JSONObject fetchData(String apiRequest) {
        return fetchData(apiRequest, new ArrayList<NameValuePair>());
    }

    /**
     * Get data with an API request with the given parameters.
     * @param apiRequest The API request to perform.
     * @param params Parameters to send with the API request.
     * @return
     */
    public JSONObject fetchData(String apiRequest, List<NameValuePair> params) {

        InputStream is;
        String result;
        JSONObject json;

        try {

            params = prepareParams(ACTION_FETCH_DATA, params);

            Log.d(DEBUG_TAG, "fetchData Params: " + params);

            is = sendHttpGetRequest(apiRequest, params);
        } catch (Exception e) {
            Log.e(DEBUG_TAG, "Error when sending http get request!", e);
            return null;
        }

        try {
            result = convertResultToString(is);
        } catch(Exception e) {
            Log.e(DEBUG_TAG, "Error when converting result!", e);
            return null;
        }

        try {
            //Log.d(DEBUG_TAG, "Result: " + result);
            json = new JSONObject(result);
            Log.d(DEBUG_TAG, "JSON: " + json);
        } catch (Exception e) {
            Log.e(DEBUG_TAG, "Error converting result to JSON", e);
            return null;
        }

        return json;
    }

    /**
     * Convert a result from http request to a string.
     * @param is Result received from the server.
     * @return The result as a String.
     * @throws java.io.IOException Throws an IOException when converting fails.
     */
    public String convertResultToString(InputStream is) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        while((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line + "\n");
        }
        is.close();

        return stringBuilder.toString();
    }

    public abstract JSONArray getUpdates(List<NameValuePair> params);
    public abstract HashMap<Long, UpdateMessage> preparePosts(HashMap<Long, UpdateMessage> updateMessages, JSONArray jsonArray);

    /**
     * Create a message/post.
     * @param message
     */
    public abstract void createMessage(String message);

    /**
     * Get the authorization url
     * @return String Authorization URL
     */
    public String getFullAuthUrl() {

        return baseAuthURL + "?response_type=code" +
                "&client_id=" + clientId +
                "&scope=" + scope +
                "&state=" + state +
                "&redirect_uri=" + redirectUri;
    }

    public void setAccessToken(String accessToken) {

        this.accessToken = accessToken;
    }

    public String getBaseApiURL() {
        return baseApiURL;
    }

    public boolean isGoodUpdateMessage(UpdateMessage updateMessage) {

        if(updateMessage.getLink() != null || updateMessage.getMessage() != null ||
                updateMessage.getAttachments() != null || updateMessage.getVideoUrl() != null ||
                updateMessage.getLinkImage() != null) {
            return true;
        }

        return false;
    }
}
