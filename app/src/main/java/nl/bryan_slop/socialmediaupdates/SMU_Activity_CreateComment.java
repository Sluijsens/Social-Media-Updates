package nl.bryan_slop.socialmediaupdates;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import static android.widget.Toast.makeText;


public class SMU_Activity_CreateComment extends SMU_Activity {

    private boolean serviceLinkedIn = false;
    private boolean serviceFacebook = false;
    private boolean serviceGooglePlus = false;
    private boolean serviceTwitter = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_comment);

        EditText editTextComment = (EditText) findViewById(R.id.EditView_CreateComment);
        final String[] socialMediaServices = getResources().getStringArray(R.array.social_media_services);

        for(int position = 0; position < socialMediaServices.length; position++) {
            LinearLayout linearLayoutCreateCommentIcons = (LinearLayout) findViewById(R.id.LinearLayout_CreateCommentIcons);

            final ImageView imageIcon = new ImageView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.height = calculatePixels(50);
            lp.width = lp.height;
            lp.setMargins(0, 0, calculatePixels(8), 0);

            imageIcon.setLayoutParams(lp);
            imageIcon.setRight(calculatePixels(8));

            if(position == SERVICE_LINKEDIN) {
                if (LINKEDIN_HAS_ACCESS_TOKEN) {
                    imageIcon.setImageDrawable(getResources().getDrawable(R.drawable.logo_linkedin));
                    toggleService(SERVICE_LINKEDIN, imageIcon);
                } else {
                    imageIcon.setImageDrawable(getResources().getDrawable(R.drawable.logo_linkedin_grey));
                }
                imageIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleService(SERVICE_LINKEDIN, imageIcon);
                    }
                });
            } else if(position == SERVICE_FACEBOOK) {
                if (FACEBOOK_HAS_ACCESS_TOKEN) {
                    toggleService(SERVICE_FACEBOOK, imageIcon);
                } else {
                    imageIcon.setImageDrawable(getResources().getDrawable(R.drawable.logo_facebook_grey));
                }
                imageIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleService(SERVICE_FACEBOOK, imageIcon);
                    }
                });
            } else if(position == SERVICE_GOOGLEPLUS) {
                if (GOOGLEPLUS_HAS_ACCESS_TOKEN) {
                    toggleService(SERVICE_GOOGLEPLUS, imageIcon);
                } else {
                    imageIcon.setImageDrawable(getResources().getDrawable(R.drawable.logo_googleplus_grey));
                }
                imageIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleService(SERVICE_GOOGLEPLUS, imageIcon);
                    }
                });
            } else if(position == SERVICE_TWITTER) {
                if (TWITTER_HAS_ACCESS_TOKEN) {
                    toggleService(SERVICE_TWITTER, imageIcon);
                } else {
                    imageIcon.setImageDrawable(getResources().getDrawable(R.drawable.logo_twitter_grey));
                }
                imageIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleService(SERVICE_TWITTER, imageIcon);
                    }
                });
            }

            linearLayoutCreateCommentIcons.addView(imageIcon);
        }

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if(Intent.ACTION_SEND.equals(action) && type != null) {
            if(type.equals("text/plain")) {
                editTextComment.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_comment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_send_comment) {
            EditText editTextComment = (EditText) findViewById(R.id.EditView_CreateComment);
            String comment = editTextComment.getText().toString();

            startActivity(new Intent(getApplicationContext(), SMU_Activity_Updates.class));

            CreatePost sc = new CreatePost();
            sc.execute(comment);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleService(int service, ImageView imageIcon) {

        if(service == SERVICE_LINKEDIN) {
            if(serviceLinkedIn) {
                serviceLinkedIn = false;
                imageIcon.setImageDrawable(getResources().getDrawable(R.drawable.logo_linkedin_grey));
            } else {
                if(LINKEDIN_HAS_ACCESS_TOKEN) {
                    serviceLinkedIn = true;
                    imageIcon.setImageDrawable(getResources().getDrawable(R.drawable.logo_linkedin));
                } else {
                    makeText(this,
                            "Please log in to LinkedIn first.",
                            Toast.LENGTH_LONG).show();
                }
            }
        } else if(service == SERVICE_FACEBOOK) {
            if(serviceFacebook) {
                serviceFacebook = false;
                imageIcon.setImageDrawable(getResources().getDrawable(R.drawable.logo_facebook_grey));
            } else {
                if(FACEBOOK_HAS_ACCESS_TOKEN) {
                    serviceFacebook = true;
                    imageIcon.setImageDrawable(getResources().getDrawable(R.drawable.logo_facebook));
                } else {
                    makeText(this,
                            "Please log in to Facebook first.",
                            Toast.LENGTH_LONG).show();
                }
            }
        } else if(service == SERVICE_GOOGLEPLUS) {
            if(serviceGooglePlus) {
                serviceGooglePlus = false;
                imageIcon.setImageDrawable(getResources().getDrawable(R.drawable.logo_googleplus_grey));
            } else {
                if(GOOGLEPLUS_HAS_ACCESS_TOKEN) {
                    serviceGooglePlus = true;
                    imageIcon.setImageDrawable(getResources().getDrawable(R.drawable.logo_googleplus));
                } else {
                    makeText(this,
                            "Please log in to Google+ first.",
                            Toast.LENGTH_LONG).show();
                }
            }
        } else if(service == SERVICE_TWITTER) {
            if(serviceTwitter) {
                serviceTwitter = false;
                imageIcon.setImageDrawable(getResources().getDrawable(R.drawable.logo_twitter_grey));
            } else {
                if(TWITTER_HAS_ACCESS_TOKEN) {
                    serviceTwitter = true;
                    imageIcon.setImageDrawable(getResources().getDrawable(R.drawable.logo_twitter));
                } else {
                    makeText(this,
                            "Please log in to Twitter first.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    public void postCreated() {
        Toast.makeText(this,
                "Post created!",
                Toast.LENGTH_LONG).show();
    }

    private class CreatePost extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            if(params != null && params.length > 0) {
                String comment = params[0];

                if(serviceLinkedIn) {
                    linkedInOAuth2.createMessage(comment);
                }
                if(serviceFacebook) {
                    facebookOAuth2.createMessage(comment);
                }

                return comment;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            postCreated();
        }


    }

}
