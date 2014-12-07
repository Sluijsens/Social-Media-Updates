package nl.bryan_slop.socialmediaupdates;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bryan on 5-11-2014.
 */
public class OAuth2Facebook extends OAuth2 {

    public static OAuth2Facebook instance;

    private OAuth2Facebook(Map<String, String> data) {

        super(data);
    }

    public static OAuth2Facebook getInstance() {

        if(OAuth2Facebook.instance == null) {
            Map<String, String> data = new HashMap<String, String>();

            data.put("service", "Facebook");
            data.put("clientId", "708367385866280");
            data.put("clientSecret", "c16ad051bf44b421a04ed35e259eab76");
            data.put("scope", "read_stream public_profile user_status user_photos user_videos publish_actions");
            data.put("baseApiURL", "https://graph.facebook.com/v2.1");
            data.put("baseAuthURL", "https://www.facebook.com/dialog/oauth");
            data.put("baseAccessURL", "https://graph.facebook.com/oauth/access_token");

            OAuth2Facebook.instance = new OAuth2Facebook(data);
        }

        return OAuth2Facebook.instance;

    }

    @Override
    protected List<NameValuePair> prepareParams(int action, List<NameValuePair> params) {

        switch(action) {
            case ACTION_FETCH_DATA:
                params.add(new BasicNameValuePair("access_token", this.accessToken));

                return params;
            default:
                return null;
        }
    }

    @Override
    public HashMap<Long, UpdateMessage> preparePosts(HashMap<Long, UpdateMessage> updateMessages, JSONArray jsonArray) {
        try {

            // Loop through all updates and create an own UpdateMessage class for every item.
            for (int i = 0; i < jsonArray.length(); i++) {
                // Get the JSONObject with the message data and declare and define the needed variables
                JSONObject json = (JSONObject) jsonArray.get(i);
                HashMap<String, String> updateData = new HashMap<String, String>();
                UpdateMessage updateMessage;

                /*
                 * Start getting message data
                 */
                // Try to get a name
                try {
                    updateData.put("name", json.getJSONObject("from").getString("name"));
                } catch (JSONException e) {
                    updateData.put("name", "Poster name");
                    Log.e(DEBUG_TAG, "Facebook ► No name found!", e);
                }

                // Add the profile picture via an AsynTask object
                String id = "";
                try {
                    id = json.getJSONObject("from").getString("id");
                    updateData.put("from-id", id);
                } catch (JSONException e) {
                    Log.i(DEBUG_TAG, "Facebook ► Could not find from-user id.", e);
                }

                // Add profile picture url
                int pictureWidth = SMU_Activity.calculatePixels(75);
                int pictureHeight = pictureWidth;

                List<NameValuePair> apiParams = new ArrayList<NameValuePair>();
                apiParams.add(new BasicNameValuePair("redirect", "false"));
                apiParams.add(new BasicNameValuePair("width", String.valueOf(pictureWidth)));
                apiParams.add(new BasicNameValuePair("height", String.valueOf(pictureHeight)));

                JSONObject jsonImage = fetchData("/" + id + "/picture", apiParams);
                updateData.put("picture", jsonImage.getJSONObject("data").getString("url"));

                // Try to get the person the message is sent to (if present)
                try {
                    updateData.put("to-name", ((JSONObject) json.getJSONObject("to").getJSONArray("data").get(0)).getString("name"));
                } catch (JSONException e) {
                    Log.i(DEBUG_TAG, "Facebook ► No to-user name found!");
                }

                // Try to get the type of message
                try {
                    updateData.put("type", json.getString("type"));
                } catch (JSONException e) {
                    Log.e(DEBUG_TAG, "Facebook ► No type found!", e);
                }

                // Try to get the secondary type of the message
                try {
                    updateData.put("type-secondary", json.getString("status_type"));
                } catch (JSONException e) {
                    Log.i(DEBUG_TAG, "Facebook ► No status type found!");
                }

                // Try to get a message. Can be an empty field.
                String message = null;
                try {
                    message = json.getString("message");
                    updateData.put("message", json.getString("message"));
                } catch (JSONException e) {
                    Log.i(DEBUG_TAG, "Facebook ► No message found!");
                }

                // Add link image/picture to the message
                try {
                    updateData.put("link-image-url", json.getString("full_picture"));
                } catch (JSONException e) {
                    Log.i(DEBUG_TAG, "Facebook ► No link image was found!");
                }

                // Try to get the link with the additional data. Can be empty as well.
                try {
                    updateData.put("link", json.getString("link"));
                    updateData.put("link-title", json.getString("name"));
                    updateData.put("link-description", json.getString("description"));
                    updateData.put("link-caption", json.getString("caption"));

                } catch (JSONException e) {
                    Log.i(DEBUG_TAG, "Facebook ► No link found!");
                }

                try {
                    if (json.getString("type").equals("video")) {
                        updateData.put("video", json.getString("source"));
                    }
                } catch (JSONException e) {
                    Log.i(DEBUG_TAG, "Facebook ► No video found!");
                }

                /*
                 * End getting the message data
                 */

                // Get the creation time and create a timestamp in seconds of it. Required else message will not be shown.
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+SSSS");
                long timestamp = simpleDateFormat.parse(json.getString("created_time")).getTime() / 1000;

                // Debug message to see if time has been parsed to timestamp
//                Log.d(DEBUG_TAG, "Timestamp: " + timestamp + ", created_time: " + json.getString("created_time"));

                // Create the UpdateMessage object and add it to the HashMap with update messages
                updateMessage = new UpdateMessage(SMU_Activity.SERVICE_FACEBOOK, timestamp, updateData);

//                Log.d(DEBUG_TAG, "JSON msg: " + updateMessage.getMessage());

                if (json.getString("type").equalsIgnoreCase("photo") && !json.getString("status_type").equalsIgnoreCase("added_photos")) {
                    try {

                        String postId = null;
                        postId = json.getString("id");

                        List<NameValuePair> attachmentParams = new ArrayList<NameValuePair>();
                        attachmentParams.add(new BasicNameValuePair("fields", "attachments"));
                        JSONObject attachmentsJSON = fetchData("/" + postId, attachmentParams);

                        JSONArray attachments = attachmentsJSON.getJSONObject("attachments").getJSONArray("data").getJSONObject(0).getJSONObject("subattachments").getJSONArray("data");

                        String[] arrayAttachments = new String[attachments.length()];
                        for (int y = 0; y < attachments.length(); y++) {
                            JSONObject jsonAttachment = attachments.getJSONObject(y);
                            arrayAttachments[y] = jsonAttachment.getJSONObject("media").getJSONObject("image").getString("src");
                        }
                        updateMessage.setAttachments(arrayAttachments);

                    } catch (JSONException e) {
//                        Log.d(DEBUG_TAG, "Facebook ► No attachments found");
                    }
                }

                if(isGoodUpdateMessage(updateMessage)) {
                    updateMessages.put(timestamp, updateMessage);
                }
            }
            // Debug message
//            Log.d(DEBUG_TAG, "Facebook Updates ► " + facebookUpdates);
        } catch(Exception e) {
            Log.e(DEBUG_TAG, "Facebook ► No facebook updates found. (data entry empty?)", e);
        }

        return updateMessages;
    }

    @Override
    public JSONArray getUpdates(List<NameValuePair> params) {
        int limit = 25;
        int offset = 0;

        params.add(new BasicNameValuePair("fields", "id,from,to,type,status_type,message,link,name,description,caption,full_picture,source"));
        params.add(new BasicNameValuePair("limit", "25"));

        JSONArray jsonArray = new JSONArray();

        for(;;) {
            NameValuePair offsetParam = new BasicNameValuePair("offset", String.valueOf(offset));
            params.add(offsetParam);

            try {

                // Fetch the updates
                JSONObject facebookUpdates = fetchData("/me/home", params);

                // Get the updates as an JSONArray
                JSONArray tmpJsonArray = facebookUpdates.getJSONArray("data");

                if(tmpJsonArray.length() < 1) {
                    break;
                }

                for (int i = 0; i <= tmpJsonArray.length(); i++) {

                    try {
                        jsonArray.put(tmpJsonArray.getJSONObject(i));
                    } catch (JSONException e) {
                        Log.e(DEBUG_TAG, "Not able to add JSONObject to JSONArray", e);
                    }
                }
            } catch (JSONException e) {
                Log.i(DEBUG_TAG, "No posts to process.", e);
                break;
            }

            params.remove(offsetParam);
            offset += (limit + 1);
        }

        return jsonArray;
    }

    @Override
    public void createMessage(String message) {
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
//            Log.d(DEBUG_TAG, "Result Facebook Comment: " + result + "\n" +
//                    "access token: " + OAuthAT + "\n" +
//                    "URL: " + url);
        } catch (Exception e) {
            Log.e(DEBUG_TAG, "Something went wrong creating a facebook comment!", e);
        }
    }
}
