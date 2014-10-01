package cbedoy.cbchathead.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.TypedValue;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Carlos Bedoy on 14/09/2014.
 */
public class Utils {

    private int width;
    private int height;
    private float density;
    private String images_url;
    private int action_bar_size;
    private int status_bar_size;
    private String cache_storage;
    private String folder_storage;
    private DisplayMetrics metrics;
    private int navigation_bar_size;
    private HashMap<String, Typeface> fonts;

    public static String REBOTO_REGULAR = "Roboto-Regular.ttf";
    public static String REBOTO_THIN = "Roboto-Thin.ttf";
    public static String REBOTO_LIGHT = "Roboto-Light.ttf";
    public static String REBOTO_BOLD = "Roboto-Bold.ttf";

    private static Utils _instance;

    public static Utils getInstance(Activity activity)
    {
        if (_instance == null)
            _instance = new Utils(activity);
        return _instance;
    }

    private Utils(Activity activity)
    {
        this.metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(this.metrics);
        this.density = metrics.density;
        this.width = metrics.widthPixels;
        this.height = metrics.heightPixels;

        TypedValue tv = new TypedValue();
        if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            this.action_bar_size = TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources().getDisplayMetrics());
        else
            this.action_bar_size = 0;

        int status_bar_id = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (status_bar_id > 0)
            this.status_bar_size = activity.getResources().getDimensionPixelSize(status_bar_id);
        else
            this.status_bar_size = 0;

        int navigation_bar_id = activity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (navigation_bar_id > 0)
            this.navigation_bar_size = activity.getResources().getDimensionPixelSize(navigation_bar_id);
        else
            this.navigation_bar_size = 0;

        Context context = activity.getApplicationContext();
        File external_files_dir = context.getExternalFilesDir(null);
        if (external_files_dir != null && this.isExternalStorageWritable()) {
            this.folder_storage = external_files_dir.getAbsolutePath() + File.separator + "cbedoy" + File.separator + "images" + File.separator;
            this.cache_storage = context.getExternalCacheDir().getAbsolutePath() + File.separator;
        } else {
            this.folder_storage = context.getFilesDir().getAbsolutePath() + File.separator + "cbedoy" + File.separator + "images" + File.separator;
            this.cache_storage = context.getCacheDir().getAbsolutePath() + File.separator;
        }
        this.images_url = "https://staging.pademobile.com:700/static/pademobile/images/";
        new File(this.folder_storage).mkdirs();

        this.fonts = new HashMap<String, Typeface>();
        try {
            String[] assets = activity.getAssets().list("");
            for (String asset : assets)
            {
                if (asset.toLowerCase(Locale.getDefault()).contains(".ttf"))
                {
                    Typeface typeface = Typeface.createFromAsset(activity.getAssets(), asset);
                    this.fonts.put(asset, typeface);
                }
            }
        }
        catch (Exception e) {
        }
    }

    public String getImagesUrl()
    {
        return this.images_url;
    }

    public String getFolderStorage()
    {
        return this.folder_storage;
    }

    public Typeface getTypeface(String type)
    {
        return this.fonts.get(type);
    }

    public int getScreenWidth()
    {
        return this.width;
    }

    public int getScreenHeight()
    {
        return this.height;
    }

    public int getActionBarSize()
    {
        return this.action_bar_size;
    }

    public int getStatusBarSize()
    {
        return this.status_bar_size;
    }

    public int getNavigatioBarSize()
    {
        return this.navigation_bar_size;
    }

    public float getScreenDensity()
    {
        return this.density;
    }

    public Bitmap getBitmap(String image)
    {
        byte[] image_bytes = Base64.decode(image.getBytes(), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(image_bytes, 0, image_bytes.length);
        return bitmap;
    }

    public Bitmap resizeBitmap(Bitmap original_image, float width, float height)
    {
        float originalWidth = original_image.getWidth(), originalHeight = original_image.getHeight();
        float scale = width / originalWidth;
        float xTranslation = 0.0f, yTranslation = (height - originalHeight * scale) / 2.0f;

        Bitmap background = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(background);
        Matrix transformation = new Matrix();
        Paint paint = new Paint();

        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale, scale);
        paint.setFilterBitmap(true);

        canvas.drawBitmap(original_image, transformation, paint);
        return background;
    }

    public Bitmap resizeBitmapBothRatios(Bitmap original_image, float width, float height)
    {
        float originalWidth = original_image.getWidth(), originalHeight = original_image.getHeight();
        float scale_x = width / originalWidth, scale_y = height / originalHeight;
        float xTranslation = 0.0f, yTranslation = 0.0f;

        Bitmap background = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(background);
        Matrix transformation = new Matrix();
        Paint paint = new Paint();

        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale_x, scale_y);
        paint.setFilterBitmap(true);

        canvas.drawBitmap(original_image, transformation, paint);
        return background;
    }

    public Bitmap getBitmap(String image, int width, int height)
    {
        Bitmap bitmap = this.getBitmap(image);
        bitmap = this.resizeBitmap(bitmap, width, height);
        return bitmap;
    }

    public Bitmap getBitmapBothRatios(String image, int width, int height)
    {
        Bitmap bitmap = this.getBitmap(image);
        bitmap = this.resizeBitmapBothRatios(bitmap, width, height);
        return bitmap;
    }

    public Bitmap getBitmapFromCacheFile(String file)
    {
        Bitmap bmp = BitmapFactory.decodeFile(this.cache_storage + file);
        return bmp;
    }

    public Bitmap getBitmapFromFile(String file)
    {
        Bitmap bmp = BitmapFactory.decodeFile(this.folder_storage + file);
        return bmp;
    }

    public void writeBitmapToFile(String name, Bitmap bmp)
    {
        if (!name.contains(".png"))
            name += ".png";

        try {
            File file = new File(Utils.getInstance(null).folder_storage + name);
            if (file.exists())
                return;

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, bytes);

            if (!file.exists())
                file.createNewFile();

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes.toByteArray());
            fos.close();
        }
        catch (Exception e)
        {
        }
    }

    public void writeBitmapToFile(String name, String image)
    {
        try
        {
            File file = new File(Utils.getInstance(null).folder_storage + name);
            if (file.exists())
                return;

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            Bitmap bmp = this.getBitmap(image);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, bytes);

            if (!file.exists())
                file.createNewFile();

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes.toByteArray());
            fos.close();
        }
        catch (Exception e)
        {
        }
    }


    public float convertDPtoPixels(float dp)
    {
        float pixels = 0;
        pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, this.metrics);
        return pixels;
    }

    public float convertPixelsToSP(float pixels)
    {
        float sp = 0;
        sp = pixels / this.metrics.scaledDensity;
        return sp;
    }


    public void downloadImagesFrom(Object images_info)
    {
        ArrayList<String> images_list = new ArrayList<String>();
        this.processObject(images_info, images_list);

        DownloadImagesAsyncTask asyncTask = new DownloadImagesAsyncTask();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, images_list);
        else
            asyncTask.execute(images_list);
    }

    private class DownloadImagesAsyncTask extends AsyncTask<ArrayList<String>, Void, Void>
    {

        protected Void doInBackground(ArrayList<String>... urls) {
            for (ArrayList<String> list : urls)
                for (String link : list)
                    this.getBitmapFromURL(link);
            return null;
        }

        private void getBitmapFromURL(String link) {
            try {
                String url_path = Utils.getInstance(null).images_url + link;
                URL url = new URL(url_path);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bmp = BitmapFactory.decodeStream(input);
                this.saveImage(bmp, link);
            }
            catch (Exception e)
            {
            }
        }

        private void saveImage(Bitmap bmp, String link)
        {
            try
            {
                FileOutputStream fos;
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                File file = new File(Utils.getInstance(null).folder_storage + link);
                if (!file.exists())
                    file.createNewFile();
                fos = new FileOutputStream(file);
                fos.write(bytes.toByteArray());
                fos.close();
            }
            catch (Exception e) {
            }
        }
    }

    private void processObject(Object images_info, ArrayList<String> images_list)
    {
        if (images_info == null)
            return;

        if (images_info instanceof List)
        {
            for (Object object : (List) images_info)
                this.processObject(object, images_list);
            return;
        }

        if (images_info instanceof Map)
        {
            Iterator it = ((Map) images_info).entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry pair = (Map.Entry) it.next();
                Object value = pair.getValue();
                this.processObject(value, images_list);
            }
            return;
        }

        if (images_info instanceof String && images_info.toString().toLowerCase(Locale.getDefault()).contains(".png"))
        {
            File file = new File(this.folder_storage + images_info.toString());
            if (!file.exists())
                images_list.add(images_info.toString());
        }
    }

    public boolean isExternalStorageWritable()
    {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state));
    }

    public boolean isExternalStorageReadable()
    {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));

    }

    public int getPixelsFromDp(int dp)
    {
        return (int) (dp * density);
    }


    public int getDialogWidth()
    {
        return Utils.getInstance(null).getPixelsFromDp(270);
    }


    public int getDialogHeight()
    {
        return Utils.getInstance(null).getScreenHeight() - ((Utils.getInstance(null).getScreenHeight() / 20) * 7);
    }


}
