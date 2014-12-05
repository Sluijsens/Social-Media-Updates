package nl.bryan_slop.socialmediaupdates;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bryan on 29-10-2014.
 */
public class OAuth2LinkedIn extends OAuth2 {

    private static OAuth2LinkedIn instance;

    private OAuth2LinkedIn(Map<String, String> data) {

        super(data);
    }

    public static OAuth2LinkedIn getInstance() {

        if(OAuth2LinkedIn.instance == null) {
            Map<String, String> data = new HashMap<String, String>();

            data.put("service", "LinkedIn");
            data.put("clientId", "77pawijvz3gvdq");
            data.put("clientSecret", "KhbcL2uRak5OX496");
            data.put("scope", "r_basicprofile r_network rw_nus r_emailaddress");
            data.put("baseApiURL", "https://api.linkedin.com");
            data.put("baseAuthURL", "https://www.linkedin.com/uas/oauth2/authorization");
            data.put("baseAccessURL", "https://www.linkedin.com/uas/oauth2/accessToken");

            OAuth2LinkedIn.instance = new OAuth2LinkedIn(data);
        }

        return OAuth2LinkedIn.instance;
    }

    @Override
    protected List<NameValuePair> prepareParams(int action, List<NameValuePair> params) {

        switch(action) {
            case ACTION_FETCH_DATA:
                params.add(new BasicNameValuePair("oauth2_access_token", this.accessToken));
                params.add(new BasicNameValuePair("format", "json"));

                return params;
            default:
                return null;
        }

    }

    @Override
    public HashMap<Long, UpdateMessage> preparePosts(HashMap<Long, UpdateMessage> updateMessages, JSONArray jsonArray) {

        for(int i = 0; i < jsonArray.length(); i++) {

            try {
                JSONObject json = (JSONObject) jsonArray.get(i);

                // Get the JSONObject with the message data and declare and define the needed variables
                try {
                    JSONObject jsonPerson = json.getJSONObject("updateContent").getJSONObject("person");

                    try {
                        JSONObject jsonPost = jsonPerson.getJSONObject("currentShare");
                        // Get the creation time and create a timestamp in seconds of it. Required else message will not be shown.
                        long timestamp = Long.parseLong(jsonPost.getString("timestamp")) / 1000;


                        HashMap<String, String> updateData = new HashMap<String, String>();
                        UpdateMessage updateMessage;

                        /*
                         * Start getting message data
                         */
                        // Try to get a name
//                        Log.d(DEBUG_TAG, "jsonPerson: " + jsonPerson);
                        try {
                            updateData.put("name", jsonPerson.getString("firstName") + " " + jsonPerson.getString("lastName"));
                        } catch (JSONException e) {
                            updateData.put("name", "Poster name");
                            Log.e(DEBUG_TAG, "LinkedIn ► No name found!", e);
                        }

                        // Add the id from the posting user
                        String id = "";
                        try {
                            id = jsonPerson.getString("id");
                            updateData.put("from-id", id);
                        } catch (JSONException e) {
                            Log.i(DEBUG_TAG, "LinkedIn ► Could not find from-user id.", e);
                        }

                        JSONObject jsonImage = fetchData("/v1/people/id=" + id + ":(picture-url)");
                        updateData.put("picture", jsonImage.getString("pictureUrl"));

                        // Try to get the type of message
                        try {
                            updateData.put("type", json.getString("updateType"));
                        } catch (JSONException e) {
                            Log.e(DEBUG_TAG, "LinkedIn ► No type found!", e);
                        }

                        // Try to get a message. Can be an empty field.
                        String message = null;
                        try {
                            message = jsonPost.getString("comment");
                            if (!message.equals("")) {
                                updateData.put("message", message);
                            }
                        } catch (JSONException e) {
                            Log.i(DEBUG_TAG, "LinkedIn ► No message found!");
                        }

                        // Add link image to the message
                        try {
                            updateData.put("link-image-url", jsonPost.getString("submittedImageUrl"));
                        } catch (JSONException e) {
                            Log.i(DEBUG_TAG, "LinkedIn ► No link image was found!");
                        }

                        // Try to get the link with the additional data.
                        try {
                            updateData.put("link", jsonPost.getString("submittedUrl"));
                            updateData.put("link-title", jsonPost.getString("title"));
                            updateData.put("link-description", jsonPost.getString("description"));

                        } catch (JSONException e) {
                            Log.i(DEBUG_TAG, "LinkedIn ► No link found!");
                        }

                        /*
                         * End getting the message data
                         */

                        // Create the UpdateMessage object and add it to the HashMap with update messages
                        updateMessage = new UpdateMessage(SMU_Activity.SERVICE_LINKEDIN, timestamp, updateData);

                        Log.d(DEBUG_TAG, "JSON msg: " + updateMessage.getMessage());

                        if(isGoodUpdateMessage(updateMessage)) {
                            updateMessages.put(timestamp, updateMessage);
                        }
                    } catch (JSONException e) {
                        Log.i(DEBUG_TAG, "LinkedIn ► No current share found!");
                    }
                } catch (JSONException e) {
                    Log.e(DEBUG_TAG, "LinkedIn ► Could not retrieve JSON Object from LinkedIn result!", e);
                }
            } catch(JSONException e) {
                Log.e(DEBUG_TAG, "Not able to get post from jsonArray.", e);
            }
        }
        //Log.d(DEBUG_TAG, "LinkedIn Updates:" + linkedInUpdates);

    return updateMessages;

    }

    @Override
    public JSONArray getUpdates(List<NameValuePair> params) {
        String request = "/v1/people/~/network/updates";
        int count = 25;
        int start = 0;
        params.add(new BasicNameValuePair("count", String.valueOf(count)));
        //apiParams.add(new BasicNameValuePair("type", "CONN"));
        params.add(new BasicNameValuePair("type", "SHAR"));

        JSONArray jsonArray = new JSONArray();

        /*
        -	RECU
        -	CMPY
        -	JOBS

        -	VIRL samen met likes 	en comments.
        -	Comments en likes 	later.

        -	PFOL later
        */

        for(;;) {
            NameValuePair startParam = new BasicNameValuePair("start", String.valueOf(start));
            params.add(startParam);

            JSONObject linkedInUpdates = fetchData(request, params);
            Log.d(DEBUG_TAG, "updates linkedin: " + linkedInUpdates);
            try {
                JSONArray tmpJsonArray = linkedInUpdates.getJSONArray("values");

                for (int i = 0; i <= tmpJsonArray.length(); i++) {

                    try {
                        jsonArray.put(tmpJsonArray.getJSONObject(i));
                    } catch (JSONException e) {
                        Log.e(DEBUG_TAG, "Not able to add JSONObject to JSONArray", e);
                    }
                }

            } catch (JSONException e) {
                Log.i(DEBUG_TAG, "Could not find updates.", e);
                break;
            }

            params.remove(startParam);
            start += (count + 1);
        }

        return jsonArray;
    }

    @Override
    public void createMessage(String message) {
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
}
