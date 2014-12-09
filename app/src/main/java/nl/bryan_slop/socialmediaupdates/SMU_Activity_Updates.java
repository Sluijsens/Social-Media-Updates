package nl.bryan_slop.socialmediaupdates;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.*;

import java.lang.ref.WeakReference;
import java.util.*;
import android.content.*;
import android.view.*;
import org.json.*;
import org.apache.http.*;
import org.apache.http.message.*;

import android.os.*;

public class SMU_Activity_Updates extends SMU_Activity {
    private List<UpdateMessage> updatesList = new ArrayList<UpdateMessage>();
    private UpdatesAdapter adapter;
    private ListView updatesListView;
    private boolean refreshUpdates = false;

    private SwipeRefreshLayout refreshLayout;

    public ImageView imageViewExpanded;
    public VideoView videoViewExpanded;

    // Define the difference between since and until in minutes.
    private final int TIME_IN_MINUTES = 60;
    private final int difference = TIME_IN_MINUTES * 60 * 1000;

    // From when to when do you want posts to be loaded.
    private long until = System.currentTimeMillis();
    private long since = until - difference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.updates);

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.SwipeRefreshLayout_Updates);
        videoViewExpanded = (VideoView) findViewById(R.id.VideoView_ShareVideo);
        imageViewExpanded = (ImageView) findViewById(R.id.ImageView_ExpandedImage);

        if(hasAnyService()) {
            LinearLayout noServicesLinearLayout = (LinearLayout) findViewById(R.id.LinearLayout_NoServices);
            noServicesLinearLayout.setVisibility(View.GONE);

            imageViewExpanded.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissExpandedImage();
                }
            });

            videoViewExpanded.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissExpandedVideo();
                }
            });
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoViewExpanded);
            videoViewExpanded.setMediaController(mediaController);

            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshPosts();
                }
            });
            refreshLayout.setColorSchemeResources(R.color.linkedin_blue,
                    R.color.facebook_blue,
                    R.color.twitter_blue,
                    R.color.googleplus_red);

            updatesListView = (ListView) findViewById(R.id.ListView_Updates);
            updatesListView.setOnScrollListener(new AbsListView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem,
                                     int visibleItemCount, int totalItemCount) {

                    int lastInScreen = firstVisibleItem + visibleItemCount;
                    if (lastInScreen >= totalItemCount - 1) {
                        if (!refreshLayout.isRefreshing()) {
                            loadPosts();
                        }
                    }
                }
            });
        } else {
            refreshLayout.setVisibility(View.GONE);
            videoViewExpanded.setVisibility(View.GONE);
            imageViewExpanded.setVisibility(View.GONE);

            Button buttonToSettings = (Button) findViewById(R.id.Button_ToSettings);
            buttonToSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openSettingsPage();
                }
            });
        }
    }

    public void refreshPosts() {

        // Reset since and until
        until = System.currentTimeMillis();
        since = until - difference;

        // Reset the list with posts
        updatesList = new ArrayList<UpdateMessage>();
        ((ArrayAdapter) updatesListView.getAdapter()).clear();

        // Make sure we clear the list
        refreshUpdates = true;

        // Load the posts
        loadPosts();

    }

    public void loadPosts() {
        UpdatesTask updatesTask = new UpdatesTask(updatesListView);
        updatesTask.execute();
    }

    @Override
    public void onBackPressed() {
        if(imageViewExpanded.getVisibility() == View.VISIBLE) {
            dismissExpandedImage();
        } else if(videoViewExpanded.getVisibility() == View.VISIBLE) {
            dismissExpandedVideo();
        } else {
            super.onBackPressed();
        }
    }

    public void dismissExpandedImage() {
        imageViewExpanded.setVisibility(View.INVISIBLE);
        imageViewExpanded.setImageDrawable(null);
    }

    public void showExpandedImage(String imageUrl) {
        imageViewExpanded.setVisibility(View.VISIBLE);

//        LoadImage loadImage = new LoadImage(imageViewExpanded);
//        loadImage.execute(imageUrl);

        int width = SMU_Activity.getDisplayWidth();
        int height = SMU_Activity.getDisplayHeight();

        imageLoader.loadBitmap(imageUrl, imageViewExpanded, width, height);
    }

    public void dismissExpandedVideo() {
        videoViewExpanded.setVisibility(View.INVISIBLE);

        videoViewExpanded.stopPlayback();
        videoViewExpanded.setVideoURI(null);
    }

    /**
     * Show the video from the post. NOTE: For now just parse URI and give it to external program
     * @param videoUrl The url of the video.
     */
    public void showExpandedVideo(String videoUrl, String secondaryType) {
//        videoViewExpanded.setVisibility(View.VISIBLE);
//

        Uri video = Uri.parse(videoUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, video);
        if( ! secondaryType.equals("shared_story") ) {
            intent.setDataAndType(video, "video/*");
        }
        startActivity(intent);
//        videoViewExpanded.setVideoURI(video);
//        videoViewExpanded.start();
    }

    /**
     * Get all the updates and put them in a List object.
     * @param updateMessages The update messages to add to the List object.
     */
    public void setAllUpdates(HashMap<Long, UpdateMessage> updateMessages) {

        Map<Long, UpdateMessage> sortedMap = new TreeMap<Long, UpdateMessage>(updateMessages);
        List<UpdateMessage> tmpUpdatesList = new ArrayList<UpdateMessage>();

        Set set = sortedMap.entrySet();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();
            UpdateMessage um = (UpdateMessage) me.getValue();

            if(!tmpUpdatesList.contains(um)) {
                tmpUpdatesList.add(um);
            }
        }
        Collections.reverse(tmpUpdatesList);

        for(UpdateMessage um : tmpUpdatesList) {
            if(!updatesList.contains(um)) {
                updatesList.add(um);
            }
        }

//        int position = updatesListView.getFirstVisiblePosition();
        adapter = new UpdatesAdapter(this, R.layout.list_item_update, updatesList);

        if(updatesListView.getAdapter() == null || refreshUpdates == true) {
            updatesListView.setAdapter(adapter);
            ((ArrayAdapter) updatesListView.getAdapter()).notifyDataSetChanged();
        } else {
            ((ArrayAdapter) updatesListView.getAdapter()).notifyDataSetChanged();
        }

//        updatesListView.setSelection(adapter.getCount() - 1);
//        updatesListView.setSelection(position);
    }

    /**
     * Retrieve facebook updates from the server and put them in a HashMap<Long, UpdateMessage> object.
     * @param updateMessages A HashMap<Long, UpdateMessage> object which may or may not already contain other messages.
     * @return An updated HashMap<Long, UpdateMessage> object.
     */
    public HashMap<Long, UpdateMessage> loadFacebookUpdates(HashMap<Long, UpdateMessage> updateMessages) {

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("since", String.valueOf((int) (since / 1000))));
        params.add(new BasicNameValuePair("until", String.valueOf((int) (until / 1000))));

        JSONArray jsonArray = SMU_Activity.facebookOAuth2.getUpdates(params);
        return SMU_Activity.facebookOAuth2.preparePosts(updateMessages, jsonArray);
    }

    public HashMap<Long, UpdateMessage> loadLinkedInUpdates(HashMap<Long, UpdateMessage> updateMessages) {

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("after", String.valueOf(since - 1)));
        params.add(new BasicNameValuePair("before", String.valueOf(until + 1)));

        JSONArray jsonArray = SMU_Activity.linkedInOAuth2.getUpdates(params);
        return SMU_Activity.linkedInOAuth2.preparePosts(updateMessages, jsonArray);

    }
	
	/*public Drawable loadImageFromWebDrawable(String url) {
        if(profileImages.containsKey(url)) {
            //return profileImages.get(url);
        }
		try {
			InputStream is = (InputStream) new URL(url).getContent();
			Drawable drawable = Drawable.createFromStream(is, "src name");
            //profileImages.putIfAbsent(url, drawable);
			return drawable;
		} catch (Exception e) {
			return null;
		}
	}*/

//    public static Drawable loadImageFromWeb(String sourceURL, String id) {
//        if(profileImages.containsKey(id)) {
//            return new BitmapDrawable(SMU_Application.getContext().getResources(), profileImages.get(id));
//        }
//        try {
//            URL url = new URL(sourceURL);
//            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//            profileImages.putIfAbsent(id, bmp);
//            return new BitmapDrawable(SMU_Application.getContext().getResources(), bmp);
//        } catch (Exception e) {
//            return null;
//        }
//    }
	
	private class UpdatesAdapter extends ArrayAdapter {
		Context context;
		int resourceId;
		List<UpdateMessage> values;

		public UpdatesAdapter(Context context, int resourceId, List<UpdateMessage> values) {
			super(context, resourceId, values);
			this.context = context;
			this.resourceId = resourceId;
			this.values = values;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(this.resourceId, parent, false);

            TextView messageTextView = (TextView) rowView.findViewById(R.id.TextView_UpdateMessage);
            messageTextView.setText(this.values.get(position).toString());

            final UpdateMessage updateMessage = values.get(position);
            int service = updateMessage.getService();

            if (service == SERVICE_FACEBOOK || service == SERVICE_LINKEDIN) {

                ImageView imageViewServiceLogo = (ImageView) rowView.findViewById(R.id.ImageView_ServiceIcon);
                if (service == SERVICE_FACEBOOK) {
                    imageViewServiceLogo.setImageDrawable(getResources().getDrawable(R.drawable.logo_facebook));
                } else if (service == SERVICE_LINKEDIN) {
                    imageViewServiceLogo.setImageDrawable(getResources().getDrawable(R.drawable.logo_linkedin));
                } else if (service == SERVICE_GOOGLEPLUS) {
                    imageViewServiceLogo.setImageDrawable(getResources().getDrawable(R.drawable.logo_googleplus));
                } else if (service == SERVICE_TWITTER) {
                    imageViewServiceLogo.setImageDrawable(getResources().getDrawable(R.drawable.logo_twitter));
                }

                ImageView profilePictureImageView = (ImageView) rowView.findViewById(R.id.ImageView_UpdateProfilePicture);
                if (updateMessage.getFromId() != null) {
//					LoadProfilePicture loadProfilePicture = new LoadProfilePicture(profilePictureImageView, service);
//					loadProfilePicture.execute(updateMessage.getFromId());

                    imageLoader.loadBitmap(updateMessage.getPicture(), profilePictureImageView);
                }

                TextView updateNameTextView = (TextView) rowView.findViewById(R.id.TextView_UpdateName);
                String names = updateMessage.getName();
                if (updateMessage.getToName() != null && updateMessage.getTypeSecondary() != null && updateMessage.getTypeSecondary().equalsIgnoreCase("wall_post")) {
                    names = names + " ► " + updateMessage.getToName();
                }
                updateNameTextView.setText(names);

                TextView updateMessageTextView = (TextView) rowView.findViewById(R.id.TextView_UpdateMessage);
                if (updateMessage.getMessage() != null) {
                    updateMessageTextView.setText(updateMessage.getMessage());
                } else {
                    updateMessageTextView.setVisibility(View.INVISIBLE);
                    updateMessageTextView.getLayoutParams().height = 0;
                }

                LinearLayout linkLinearLayout = (LinearLayout) rowView.findViewById(R.id.LinearLayout_UpdateShareContent);
                ImageView linkImageView = (ImageView) rowView.findViewById(R.id.ImageView_ShareImage);
                HorizontalScrollView scrollViewAttachments = (HorizontalScrollView) rowView.findViewById(R.id.ScrollView_Attachments);

                if (isMultiplePhotos(updateMessage)) {
                    linkImageView.setVisibility(View.GONE);
                    linkLinearLayout.setVisibility(View.GONE);

                    LinearLayout linearLayoutAttachments = (LinearLayout) rowView.findViewById(R.id.LinearLayout_Attachments);

                    int height = 0;
                    for (final String url : updateMessage.getAttachments()) {
                        ImageView16x9 image = new ImageView16x9(rowView.getContext());
                        image.setId(Integer.parseInt(SMU_Activity.getUniqueId()));
                        image.setScaleType(ImageView.ScaleType.CENTER_CROP);

                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(SMU_Activity.getDisplayHeight(), SMU_Activity.getDisplayWidth());
                        int left = SMU_Activity.calculatePixels(5);
                        int right = left;
                        lp.width = SMU_Activity.getDisplayWidth() - ((left + right));
                        lp.setMargins(left, 0, right, 0);

                        image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showExpandedImage(url);
                            }
                        });

                        linearLayoutAttachments.addView(image, lp);

//                        LoadImage loadImage = new LoadImage(image);
//                        loadImage.execute(url);

                        imageLoader.loadBitmap(url, image);
                    }

                } else {
                    scrollViewAttachments.setVisibility(View.GONE);

                    if (updateMessage.getLink() != null) {
                        TextView linkTitleTextView = (TextView) rowView.findViewById(R.id.TextView_LinkTitle);
                        TextView linkDescriptionTextView = (TextView) rowView.findViewById(R.id.TextView_LinkDescription);

                        linkTitleTextView.setText(updateMessage.getLinkTitle());
                        linkDescriptionTextView.setText(updateMessage.getLinkDescription());

                        linkLinearLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(updateMessage.getLink()));
                                //startActivity(Intent.createChooser(browser, "Choose application:"));
                                startActivity(browser);
                            }
                        });

                        if (updateMessage.getLinkImage() != null) {
                            if (updateMessage.getVideoUrl() != null) {
                                linkImageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        showExpandedVideo(updateMessage.getVideoUrl(), updateMessage.getTypeSecondary());
                                    }
                                });
                            } else {
                                linkImageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        showExpandedImage(updateMessage.getLinkImage());
                                    }
                                });
                            }

//                            LoadImage loadImage = new LoadImage(linkImageView);
//                            loadImage.execute(updateMessage.getLinkImage());
                            imageLoader.loadBitmap(updateMessage.getLinkImage(), linkImageView, getDisplayWidth(), getDisplayHeight());
                        }

                    } else {
                        linkImageView.setVisibility(View.GONE);
                        linkLinearLayout.setVisibility(View.GONE);
                    }
                }
            } else {
                rowView.setVisibility(View.GONE);
            }
			return rowView;
		}

        /**
         * Check if updateMessage has multiple photos
         * @param updateMessage UpdateMessage to check
         * @return Boolean True if it has more than 1 photo false if not or if update type is not "photo"
         */
        private boolean isMultiplePhotos(UpdateMessage updateMessage) {
            if(updateMessage.getAttachments() != null) {
                int count = updateMessage.getAttachments().length;
                if(count > 1) {
                    return true;
                }
            }
            return false;
        }
	}

    private class UpdatesTask extends AsyncTask<Object, String, JSONObject> {
        final WeakReference<ListView> listViewReference;
        HashMap<Long, UpdateMessage> updateMessages;

        public UpdatesTask(ListView listView) {
            this.listViewReference = new WeakReference<ListView>(listView);
            updateMessages = new HashMap<Long, UpdateMessage>();
        }

        @Override
        protected JSONObject doInBackground(Object... params) {

            if(LINKEDIN_HAS_ACCESS_TOKEN) {
                updateMessages = loadLinkedInUpdates(updateMessages);
            }
            if(FACEBOOK_HAS_ACCESS_TOKEN) {
                updateMessages = loadFacebookUpdates(updateMessages);
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            if(refreshLayout != null) {
                refreshLayout.setRefreshing(true);
            }
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if(listViewReference != null) {
                ListView listView = listViewReference.get();
                if(listView != null) {
                    setAllUpdates(updateMessages);

                    until = since;
                    since -= difference;

                    if(refreshUpdates == true) {
                        refreshUpdates = false;
                    }
                    refreshLayout.setRefreshing(false);
                    ProgressBar progressBarBeforeUpdates = (ProgressBar) findViewById(R.id.ProgressBar_BeforeUpdates);
                    if(progressBarBeforeUpdates.getVisibility() != View.GONE) {
                        progressBarBeforeUpdates.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    public class StackAdapter extends ArrayAdapter {
        final private Context context;
        final private List<String> images;
        final private int resourceId;

        public StackAdapter(Context context, int resourceId, List<String> images) {
            super(context, resourceId, images);
            this.context = context;
            this.resourceId = resourceId;
            this.images = images;
        }


    }

    public class AttachmentsViewStackAdapter extends ArrayAdapter {

        private final Context context;
        private int resourceId;
        private String[] imageURLs;

        public AttachmentsViewStackAdapter(Context context, int resourceId, String[] imageURLs) {
            super(context, resourceId, resourceId, imageURLs);
            this.context = context;
            this.resourceId = resourceId;
            this.imageURLs = imageURLs;
        }


        @Override
        public View getView(int position, View view, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(this.resourceId, parent, false);

            ImageView16x9 imageView16x9 = (ImageView16x9) rowView.findViewById(R.id.ImageView_Attachment);
//            LoadImage loadImage = new LoadImage(imageView16x9);
//            loadImage.execute(imageURLs[position]);
            imageLoader.loadBitmap(imageURLs[position], imageView16x9, getDisplayWidth(), getDisplayHeight());

            return rowView;

        }
    }

}
