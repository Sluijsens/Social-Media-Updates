package nl.bryan_slop.socialmediaupdates;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import android.graphics.drawable.*;
import android.widget.ImageView;

/**
 * Update message for status updates.
 * Created by Bryan on 25-8-2014.
 */
public class UpdateMessage {

    // mandatory variables
    private int service;
    private long timeAndDate;

    // Poster name and message
    private String type;
	private String typeSecondary;
	private String picture;
	private String fromId;
    private String name;
    private String message;
	private String toName;
    private String videoUrl;

    // Link title and description
    private String link;
    private String linkImage;
    private String linkTitle;
    private String linkDescription;
    private String linkCaption;

    // An array for post attachments
    private String[] attachments;

    /**
     * Constructor without the data fields attached.
     * @param service An Integer specifying the used social media service.
     * @param timeAndDate A long object with the time of creation.
     */
    public UpdateMessage(int service, long timeAndDate) {
        this.setService(service);
        this.setTimeAndDate(timeAndDate);
    }

    /**
     * Constructor with the data fields attached.
     * @param service An Integer specifying the used social media service.
     * @param timeAndDate A long object with the time of creation.
     * @param data A HashMap with the data fields (name, message, link, link_title, link_description, link_caption).
     */
    public UpdateMessage(int service, long timeAndDate, HashMap<String, String> data) {
        this.setService(service);
        this.setTimeAndDate(timeAndDate);

        Iterator iterator = data.entrySet().iterator();
        String value;
        String key;
        while(iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();

            key = (String) me.getKey();
            value = (String) me.getValue();

            if(key.equalsIgnoreCase("from-id")){
				this.setFromId(value);
			} else if(key.equalsIgnoreCase("type")) {
                this.setType(value);
            } else if(key.equalsIgnoreCase("picture")) {
                setPicture(value);
            } else if(key.equalsIgnoreCase("type-secondary")) {
                this.setTypeSecondary(value);
			} else if(key.equalsIgnoreCase("name")) {
                this.setName(value);
			} else if(key.equalsIgnoreCase("to-name")) {
				this.setToName(value);
            } else if(key.equalsIgnoreCase("message")) {
                this.setMessage(value);
            } else if(key.equalsIgnoreCase("video")) {
                this.setVideoUrl(value);
            } else if(key.equalsIgnoreCase("link")) {
                this.setLink(value);
			} else if(key.equalsIgnoreCase("link-image-url")) {
				this.setLinkImage(value);
            } else if(key.equalsIgnoreCase("link-title")) {
                this.setLinkTitle(value);
            } else if(key.equalsIgnoreCase("link-description")) {
                this.setLinkDescription(value);
            } else if(key.equalsIgnoreCase("link-caption")) {
                this.setLinkCaption(value);
            }

        }

    }

    /*------------------[ GETTERS & SETTERS ]------------------*/
    public int getService() {
        return service;
    }

    public void setService(int service) {
        this.service = service;
    }

    public long getTimeAndDate() {
        return timeAndDate;
    }

    public void setTimeAndDate(long timeAndDate) {
        this.timeAndDate = timeAndDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLinkTitle() {
        return linkTitle;
    }

    public void setLinkTitle(String linkTitle) {
        this.linkTitle = linkTitle;
    }

    public String getLinkDescription() {
        return linkDescription;
    }

    public void setLinkDescription(String linkDescription) {
        this.linkDescription = linkDescription;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLinkCaption() {
        return linkCaption;
    }

    public void setLinkCaption(String linkCaption) {
        this.linkCaption = linkCaption;
    }

    public String getType() {
        return type;
    }

    public void setTypeSecondary(String typeSecondary) {
        this.typeSecondary = typeSecondary;
    }
	
	public String getTypeSecondary() {
        return typeSecondary;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getLinkImage() {
        return linkImage;
    }

    public void setLinkImage(String linkImage) {
        this.linkImage = linkImage;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }
	
	public void setFromId(String fromId) {
		this.fromId = fromId;
	}
	
	public String getFromId() {
		return this.fromId;
	}

    public String[] getAttachments() {
        return attachments;
    }

    public void setAttachments(String[] attachments) {
        this.attachments = attachments;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
