package nl.bryan_slop.socialmediaupdates;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bryan on 14-11-2014.
 */
public class ImageLoader {
    private Context context = SMU_Application.getContext();
    private HashMap<String, BitmapWorkerTask> currentTasks = new HashMap<String, BitmapWorkerTask>();

    // Cache variables
    protected LruCache<String, Bitmap> mMemoryCache;

    public ImageLoader() {

        // Get max available VM memory, exceeding this amount will throw an
        // OurOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if(getBitmapFromMemoryCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemoryCache(String key) {
        return mMemoryCache.get(key);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int width = options.outWidth;
        final int height = options.outHeight;
        int inSampleSize = 1;

        if(height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }

        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                          int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize ser
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeSampledBitmapFromStream(String imageUrl,
                                                         int reqWidth, int reqHeight) {

        // First create an InputStream to get the image from.
        try {
            URL url = new URL(imageUrl);

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(url.openConnection().getInputStream(), null, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize ser
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(url.openConnection().getInputStream(), null, options);
        } catch (MalformedURLException e) {
            Log.i(SMU_Activity.DEBUG_TAG, "Not able to parse URL from image URL.", e);
        } catch (IOException e) {
            Log.i(SMU_Activity.DEBUG_TAG, "Could not connect to image URL.", e);
        }

        return null;
    }

    public void loadBitmap(String imageUrl, ImageView imageView) {

        int width = imageView.getLayoutParams().width;
        int height = imageView.getLayoutParams().height;

        loadBitmap(imageUrl, imageView, width, height);
    }

    public void loadBitmap(String imageUrl, ImageView imageView, int reqWidth, int reqHeight) {
        if(cancelPotentialWork(imageUrl, imageView)) {
            final String imageKey = imageUrl;

            final Bitmap bitmap = getBitmapFromMemoryCache(imageKey);
            if(bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {

                if(currentTasks.containsKey(imageKey)) {
                    BitmapWorkerTask task = currentTasks.get(imageKey);
                    task.addImageView(imageView);
                } else {
                    final BitmapWorkerTask task = new BitmapWorkerTask(imageUrl, imageView);
                    final AsyncDrawable asyncDrawable =
                            new AsyncDrawable(context.getResources(), null, task);
                    imageView.setImageDrawable(asyncDrawable);
                    task.execute(reqWidth, reqHeight);
                }
            }
        }
    }

    public static boolean cancelPotentialWork(String data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if(bitmapWorkerTask != null) {
            final String bitmapData = bitmapWorkerTask.data;

            // If bitmapData is not yet set or it differs from the new data
            if(bitmapData.equals("") || ! bitmapData.equals(data)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or and existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {

        if(imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if(drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }

        return null;
    }

    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {

        private final WeakReference<ImageView> imageViewReference;
        private String data = "";
        private List<WeakReference<ImageView>> imageViewReferences = new ArrayList<WeakReference<ImageView>>();

        public BitmapWorkerTask(String imageUrl, ImageView imageView) {

            data = imageUrl;

            // Use a WeakReference to ensure the ImageView can be garbageCollected
            imageViewReference = new WeakReference<ImageView>(imageView);
            imageViewReferences.add(imageViewReference);
        }

        // Decode image in background
        @Override
        protected Bitmap doInBackground(Integer... params) {
            int reqWidth = params[0];
            int reqHeight = params[1];
            final Bitmap bitmap = decodeSampledBitmapFromStream(data, reqWidth, reqHeight);
            addBitmapToMemoryCache(data, bitmap);

            return bitmap;
        }

        // Once completed, see if ImageView is still around and set bitmap
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(isCancelled()) {
                bitmap = null;
            }

            for(int i = 0; i < imageViewReferences.size(); i++) {

                WeakReference<ImageView> imageViewReference = imageViewReferences.get(i);

                if (imageViewReference != null && bitmap != null) {

                    final ImageView imageView = imageViewReference.get();
                    final BitmapWorkerTask bitmapWorkerTask =
                            getBitmapWorkerTask(imageView);

                    if (this == bitmapWorkerTask && imageView != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }

                imageViewReferences.remove(i);
            }
        }

        public void addImageView(ImageView imageView) {
            WeakReference<ImageView> imageViewReference = new WeakReference<ImageView>(imageView);
            imageViewReferences.add(imageViewReference);
        }

        public List<WeakReference<ImageView>> getImageViewReferences() {
            return imageViewReferences;
        }
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }

    }

}
