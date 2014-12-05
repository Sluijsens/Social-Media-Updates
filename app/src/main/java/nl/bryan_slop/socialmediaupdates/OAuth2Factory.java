package nl.bryan_slop.socialmediaupdates;

/**
 * Created by Bryan on 4-11-2014.
 */
public class OAuth2Factory {

    public OAuth2 getOAuth2Instance(int service) {

        // TODO: Remove the null returns

        switch(service) {
            case SMU_Activity.SERVICE_LINKEDIN:
                return OAuth2LinkedIn.getInstance();
            case SMU_Activity.SERVICE_FACEBOOK:
                return OAuth2Facebook.getInstance();
            case SMU_Activity.SERVICE_TWITTER:
                //return OAuth2Twitter.getInstance();
                return null;
            case SMU_Activity.SERVICE_GOOGLEPLUS:
                //return OAuth2GooglePlus.getInstance();
                return null;
            default:
                return null;
        }

    }

}
