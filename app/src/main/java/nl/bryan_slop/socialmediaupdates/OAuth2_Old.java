package nl.bryan_slop.socialmediaupdates;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

/**
 * Created by Bryan on 6-8-2014.
 */
public class OAuth2_Old {

    public String DEBUG_TAG = "Social Media Updates OAuth2.0";

    private final String clientId; // API Key
    private final String clientSecret; // API Secret
    private final String scope;
    private final String state = new BigInteger(130, new SecureRandom()).toString(32);
    private final String baseApiURL;
    private final String baseAuthURL;
    private final String baseAccessURL;
    private final String redirectUri = "http://localhost/";
	
	private String accessToken;

    public OAuth2_Old(Map<String, String> data) {
	
		DEBUG_TAG = "SMU OAuth2.0 -> Service: " + data.get("service");

 	    clientId = data.get("clientId");
		clientSecret = data.get("clientSecret"); // API Secret
    	scope = data.get("scope"); // API Scope
    	baseApiURL = data.get("baseApiURL");
    	baseAuthURL = data.get("baseAuthURL");
    	baseAccessURL = data.get("baseAccessURL");
	
	}
	
	public static OAuth2_Old getInstanceLinkedIn() {
		Map<String, String> data = new HashMap<String, String>();
		
		data.put("service", "LinkedIn");
		data.put("clientId", "77pawijvz3gvdq");
		data.put("clientSecret", "KhbcL2uRak5OX496");
		data.put("scope", "r_basicprofile r_network rw_nus r_emailaddress");
		data.put("baseApiURL", "https://api.linkedin.com");
		data.put("baseAuthURL", "https://www.linkedin.com/uas/oauth2/authorization");
		data.put("baseAccessURL", "https://www.linkedin.com/uas/oauth2/accessToken");
		
		return new OAuth2_Old(data);
	}
	
	public static OAuth2_Old getInstanceFacebook() {
		Map<String, String> data = new HashMap<String, String>();
		
		data.put("service", "Facebook");
		data.put("clientId", "708367385866280");
		data.put("clientSecret", "c16ad051bf44b421a04ed35e259eab76");
		data.put("scope", "read_stream public_profile user_status user_photos user_videos publish_actions");
		data.put("baseApiURL", "https://graph.facebook.com/v2.1");
		data.put("baseAuthURL", "https://www.facebook.com/dialog/oauth");
		data.put("baseAccessURL", "https://graph.facebook.com/oauth/access_token");

		return new OAuth2_Old(data);
	}

    /**
     * Get the access token from the server
     * @param authCode The authorization code received from the server.
     * @return Return a String with the result from the server.
     */
    public String getAccessToken(String authCode) {

        InputStream is = null;
        String result = null;
        JSONObject json = null;

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
     * @param address The URL to send the request to.
     * @param params The parameters to send with the request.
     * @return Return an InputStream with the response.
     * @throws java.io.IOException Throws an IOException when it fails to send the request.
     */
    public InputStream sendHttpPostRequest(String address, List<NameValuePair> params) throws IOException {
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
        HttpPost httpPost = new HttpPost(address);

        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        Log.d(DEBUG_TAG, "Address: " + address + " httppost: " + httpPost.getURI() + " httppost apiParams: " + httpPost.getParams());
        HttpResponse httpResponse = httpClient.execute(httpPost);
        HttpEntity httpEntity = httpResponse.getEntity();

        return httpEntity.getContent();
    }

    /**
     * Send an Http GET request and retrieve data from the server.
     * @param params The parameters to send with the request.
     * @param apiRequest The Api specific request to send to the server.
     * @return An InputStream with the result from the server.
     * @throws IOException An IOException when sending the request to the server fails.
     */
    public InputStream sendHttpGetRequest(String apiRequest, List<NameValuePair> params) throws IOException {

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

        String url = getBaseApiURL() + apiRequest + "?" + URLEncodedUtils.format(params, null);
        //Log.d(DEBUG_TAG, "API Request URL: " + url);
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);

        HttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity httpEntity = httpResponse.getEntity();

        return httpEntity.getContent();

    }
	
	public JSONObject fetchData(String apiRequest) {
		return fetchData(apiRequest, new ArrayList<NameValuePair>());
	}
	
    public JSONObject fetchData(String apiRequest, List<NameValuePair> params) {

        InputStream is = null;
        String result = null;
        JSONObject json = null;

        try {
			
            params.add(new BasicNameValuePair("oauth2_access_token", this.accessToken));
            params.add(new BasicNameValuePair("access_token", this.accessToken));
            params.add(new BasicNameValuePair("format", "json"));

            is = sendHttpGetRequest(apiRequest, params);
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

        try {
            //Log.d(DEBUG_TAG, "Result: " + result);
            json = new JSONObject(result);
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

    /**
     * Get the authorization url
     * @return String Authorization URL
     */
    public String getAuthURL() {
        return baseAuthURL + "?response_type=code" +
                "&client_id=" + clientId +
                "&scope=" + scope +
                "&state=" + state +
                "&redirect_uri=" + redirectUri;
    }

    public void createPostLinkedIn(String message) {
        String OAuthAT = SMU_Activity.mOAuth2.getString(SMU_Activity.OAUTH2_SETTINGS_ACCESS_TOKEN_LINKEDIN, null);

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("https://api.linkedin.com/v1/people/~/shares?oauth2_access_token=" + OAuthAT);

        httpPost.setHeader("Content-Type", "application/xml");
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<share>" +
                "   <comment>" + message + "</comment>" +
                "   <visibility>" +
                "       <code>anyone</code>" +
                "   </visibility>" +
                "</share>";
        try {
            httpPost.setEntity(new StringEntity(xml));
            HttpResponse httpResponse = httpClient.execute(httpPost);

            HttpEntity httpEntity = httpResponse.getEntity();
            String result = convertResultToString(httpEntity.getContent());
            Log.d(DEBUG_TAG, "Result Linkedin Comment: " + result + "\n" +
                    "access token: " + OAuthAT);
        } catch (Exception e) {
            Log.e(DEBUG_TAG, "Something went wrong creating a linkedin comment!", e);
        }
    }

    public void createPostFacebook(String message) {
        String OAuthAT = SMU_Activity.mOAuth2.getString(SMU_Activity.OAUTH2_SETTINGS_ACCESS_TOKEN_FACEBOOK, null);

        HttpClient httpClient = new DefaultHttpClient();
        String url = "https://graph.facebook.com/me/feed";
        HttpPost httpPost = new HttpPost(url);

        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("message", message));
        params.add(new BasicNameValuePair("access_token", OAuthAT));

        try {
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse httpResponse = httpClient.execute(httpPost);

            HttpEntity httpEntity = httpResponse.getEntity();
            String result = convertResultToString(httpEntity.getContent());
            Log.d(DEBUG_TAG, "Result Facebook Comment: " + result + "\n" +
                    "access token: " + OAuthAT + "\n" +
                    "URL: " + url);
        } catch (Exception e) {
            Log.e(DEBUG_TAG, "Something went wrong creating a facebook comment!", e);
        }
    }

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

    public String getBaseApiURL() {
        return baseApiURL;
    }
}
