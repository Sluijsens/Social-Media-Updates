package nl.bryan_slop.socialmediaupdates;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SMU_Activity_Settings extends SMU_Activity {

    static TextView settingsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        //Log.d(DEBUG_TAG, "AccessToken: " + mOAuth2.getString(OAUTH2_SETTINGS_ACCESS_TOKEN_LINKEDIN, "No Token"));

        final String[] socialMediaServices = getResources().getStringArray(R.array.social_media_services);

        ListView settingsListView = (ListView) findViewById(R.id.ListView_Settings);
        SettingsAdapter adapter = new SettingsAdapter(this, R.layout.list_item_settings, socialMediaServices);
        settingsListView.setAdapter(adapter);
        settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                settingsTextView = (TextView) view.findViewById(R.id.TextView_Settings);

                if(position == SERVICE_LINKEDIN) {
                    if (LINKEDIN_HAS_ACCESS_TOKEN) {
                        SMU_AlertDialog alertDialog = SMU_AlertDialog.newInstance(getResources().getString(R.string.dialog_disconnect_title), getResources().getString(R.string.dialog_disconnect_message_linkedin), SERVICE_LINKEDIN, settingsTextView);
                        alertDialog.show(getSupportFragmentManager(), "disconnectDialog");
                    } else {
                        showAuthDialog(SERVICE_LINKEDIN, settingsTextView);
                    }
                } else if(position == SERVICE_FACEBOOK) {
                    if (FACEBOOK_HAS_ACCESS_TOKEN) {
                        SMU_AlertDialog alertDialog = SMU_AlertDialog.newInstance(getResources().getString(R.string.dialog_disconnect_title), getResources().getString(R.string.dialog_disconnect_message_facebook), SERVICE_FACEBOOK, settingsTextView);
                        alertDialog.show(getSupportFragmentManager(), "disconnectDialog");
                    } else {
                        showAuthDialog(SERVICE_FACEBOOK, settingsTextView);
                    }
                } else if(position == SERVICE_TWITTER) {
                    if (TWITTER_HAS_ACCESS_TOKEN) {
                        SMU_AlertDialog alertDialog = SMU_AlertDialog.newInstance(getResources().getString(R.string.dialog_disconnect_title), getResources().getString(R.string.dialog_disconnect_message_facebook), SERVICE_TWITTER, settingsTextView);
                        alertDialog.show(getSupportFragmentManager(), "disconnectDialog");
                    } else {
                        // TODO: Enable Twitter
                        //showAuthDialog(SERVICE_TWITTER, settingsTextView);
                        Toast.makeText(SMU_Application.getContext(),
                                "Not yet available.",
                                Toast.LENGTH_LONG).show();
                    }
                } else if(position == SERVICE_GOOGLEPLUS) {
                    if (GOOGLEPLUS_HAS_ACCESS_TOKEN) {
                        SMU_AlertDialog alertDialog = SMU_AlertDialog.newInstance(getResources().getString(R.string.dialog_disconnect_title), getResources().getString(R.string.dialog_disconnect_message_facebook), SERVICE_GOOGLEPLUS, settingsTextView);
                        alertDialog.show(getSupportFragmentManager(), "disconnectDialog");
                    } else {
                        // TODO: Enable Google Plus
                        //showAuthDialog(SERVICE_GOOGLEPLUS, settingsTextView);
                        Toast.makeText(SMU_Application.getContext(),
                                "Not yet available.",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    private class SettingsAdapter extends ArrayAdapter {

        final Context context;
        final int resourceId;
        final String[] values;

        public SettingsAdapter(Context context, int resourceId, String[] values) {
            super(context, resourceId, values);

            this.context = context;
            this.resourceId = resourceId;
            this.values = values;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(this.resourceId, parent, false);

            ImageView serviceIconImageView = (ImageView) rowView.findViewById(R.id.ImageView_ServiceIcon);
            TextView settingsTextView = (TextView) rowView.findViewById(R.id.TextView_Settings);
            Drawable icon = null;
            String settingsText = null;
            String userName = null;

            if(position == SERVICE_LINKEDIN) {
                icon = getResources().getDrawable(R.drawable.logo_linkedin);
                settingsText = getResources().getString(R.string.settings_linkedin);

                if(LINKEDIN_HAS_ACCESS_TOKEN) {
                    try {
                        FetchData apiCall = new FetchData();
                        apiCall.execute(SERVICE_LINKEDIN, "/v1/people/~:(first-name,last-name)");
                        JSONObject json = apiCall.get();

                        userName = json.getString("firstName") + " " + json.getString("lastName");
                        settingsText = getResources().getString(R.string.settings_linkedin_connected) + " " + userName;
                        rowView.setBackgroundResource(R.drawable.background_red);
                        settingsTextView.setTextColor(getResources().getColorStateList(R.color.text_color_red_background));
                    } catch (Exception e) {
                        Log.e(DEBUG_TAG, "LinkedIn: Failed to do api request.", e);
                        LINKEDIN_HAS_ACCESS_TOKEN = false;
                    }

                }
            } else if(position == SERVICE_FACEBOOK) {
                icon = getResources().getDrawable(R.drawable.logo_facebook);
                settingsText = getResources().getString(R.string.settings_facebook);

                if(FACEBOOK_HAS_ACCESS_TOKEN) {
                    try {
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("fields", "name"));

                        FetchData apiCall = new FetchData();
                        apiCall.execute(SERVICE_FACEBOOK, "/me", params);
                        JSONObject json = apiCall.get();

                        userName = json.getString("name");
                        settingsText = getResources().getString(R.string.settings_facebook_connected) + " " + userName;
                        rowView.setBackgroundResource(R.drawable.background_red);
                        settingsTextView.setTextColor(getResources().getColorStateList(R.color.text_color_red_background));
                    } catch (Exception e) {
                        Log.e(DEBUG_TAG, "Facebook: Failed to do api request.", e);
                        FACEBOOK_HAS_ACCESS_TOKEN = false;
                    }

                }
            } else if(position == SERVICE_TWITTER) {
                icon = getResources().getDrawable(R.drawable.logo_twitter);
                settingsText = getResources().getString(R.string.settings_twitter);

                if(TWITTER_HAS_ACCESS_TOKEN) {
                    try {
//                        List<NameValuePair> params = new ArrayList<NameValuePair>();
//                        params.add(new BasicNameValuePair("fields", "name"));
//
//                        FetchData apiCall = new FetchData();
//                        apiCall.execute(SERVICE_FACEBOOK, "/me", params);
//                        JSONObject json = apiCall.get();
//
//                        userName = json.getString("name");
//                        settingsText = getResources().getString(R.string.settings_facebook_connected) + " " + userName;
//                        rowView.setBackgroundResource(R.drawable.background_red);
//                        settingsTextView.setTextColor(getResources().getColorStateList(R.color.text_color_red_background));
                    } catch (Exception e) {
                        Log.e(DEBUG_TAG, "Twitter: Failed to do api request.", e);
                    }

                }
            } else if(position == SERVICE_GOOGLEPLUS) {
                icon = getResources().getDrawable(R.drawable.logo_googleplus);
                settingsText = getResources().getString(R.string.settings_googleplus);

                if(GOOGLEPLUS_HAS_ACCESS_TOKEN) {
                    try {
//                        List<NameValuePair> params = new ArrayList<NameValuePair>();
//                        params.add(new BasicNameValuePair("fields", "name"));
//
//                        FetchData apiCall = new FetchData();
//                        apiCall.execute(SERVICE_FACEBOOK, "/me", params);
//                        JSONObject json = apiCall.get();
//
//                        userName = json.getString("name");
//                        settingsText = getResources().getString(R.string.settings_facebook_connected) + " " + userName;
//                        rowView.setBackgroundResource(R.drawable.background_red);
//                        settingsTextView.setTextColor(getResources().getColorStateList(R.color.text_color_red_background));
                    } catch (Exception e) {
                        Log.e(DEBUG_TAG, "Google Plus: Failed to do api request.", e);
                    }

                }
            }
			
			// Hide divider of last list item
			if(position == values.length - 1) {
				View divider = (View) rowView.findViewById(R.id.View_SettingsDivider);
				divider.setVisibility(View.INVISIBLE);
				divider.getLayoutParams().height = 0;
			}
			
            // Set the icon and text
            serviceIconImageView.setImageDrawable(icon);
            settingsTextView.setText(settingsText);

            return rowView;
        }
    }

    public void disconnectFromService(int service, TextView settingsTextView) {
        Editor editor = mOAuth2.edit();

        if(service == SERVICE_LINKEDIN) {
            editor.remove(OAUTH2_SETTINGS_ACCESS_TOKEN_LINKEDIN);
            editor.remove(OAUTH2_SETTINGS_ACCESS_TOKEN_EXPIRES_LINKEDIN);

            LINKEDIN_HAS_ACCESS_TOKEN = false;
            setSettingsText(SERVICE_LINKEDIN, settingsTextView);
        } else if(service == SERVICE_FACEBOOK) {
            editor.remove(OAUTH2_SETTINGS_ACCESS_TOKEN_FACEBOOK);
            editor.remove(OAUTH2_SETTINGS_ACCESS_TOKEN_EXPIRES_FACEBOOK);

            FACEBOOK_HAS_ACCESS_TOKEN = false;
            setSettingsText(SERVICE_FACEBOOK, settingsTextView);
        }

        editor.commit();
    }

    public static class SMU_AlertDialog extends DialogFragment {

        public SMU_AlertDialog() {}

        public static SMU_AlertDialog newInstance(String title, String message, int service, TextView settingsTextView) {

            SMU_AlertDialog alertDialog = new SMU_AlertDialog();
            Bundle args = new Bundle();
            args.putString("title", title);
            args.putString("message", message);
            args.putInt("service", service);
            alertDialog.setArguments(args);

            return alertDialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            final String title = getArguments().getString("title");
            final String message = getArguments().getString("message");
            final int service = getArguments().getInt("service");

            return new android.app.AlertDialog.Builder(getActivity())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ((SMU_Activity_Settings) getActivity()).disconnectFromService(service, settingsTextView);
                                }
                            }
                    )
                    .setNegativeButton(android.R.string.no,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dismiss();
                                }
                            }
                    )
                    .create();
        }
    }

}
