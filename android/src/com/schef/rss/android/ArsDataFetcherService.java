package com.schef.rss.android;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.schef.rss.android.db.ArsEntity;
import com.schef.rss.android.db.CacheDb;
import com.schef.rss.android.db.DateTimeDeserializer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by scheffela on 7/26/14.
 */
public class ArsDataFetcherService extends Service {

    private static String FILES_ROOT = "localfiles";
    public static String arsRSSFeedUrl = "http://feeds.arstechnica.com/arstechnica/index?format=xml";
    public static String viceFeedUrl = "http://www.vice.com/news/page/1";
    public static String viceFeedUrl2 = "http://www.vice.com/news/page/2";

    protected static final String TAG = ArsDataFetcherService.class.getSimpleName();

    public CacheDb cacheDb;
    private static final Object DbHelperSync = new Object();
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    private File targetDir = null;

    private ReentrantReadWriteLock rrwl = new ReentrantReadWriteLock(true);

    private Handler uiHandler;

//    private TreeSet<ArsEntity> itemsTreeSetRef;
//    private HashMap<String,ArsEntity> itemLookUp = new HashMap<String,ArsEntity>();

    private static String conentSource = "http://www.evilcorgi.com/contentservice/site/vic";

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public ArsDataFetcherService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ArsDataFetcherService.this;
        }
    }


    @Override
    public void onCreate() {
        int i = 0;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                parse();
            }
        }).start();

        return 1;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        return mBinder;
    }

    public void registerHandler(Handler newHandler) {
           uiHandler = newHandler;
    }




    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

        }
    };
    public void parse() {
        parse(false);
    }

    public void parse(boolean clean) {
        synchronized (conentSource) {
            try {
                ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = cm.getActiveNetworkInfo();

                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(Date.class, new DateTimeDeserializer(DateFormat.DEFAULT, DateFormat.DEFAULT))
                        .serializeNulls()
                        .create();


                if (ni != null && ni.isConnected()) {
                    //
                    try {
                        URL url = new URL("https://s3.amazonaws.com/arsappdir/config.json");
                        URLConnection connection = url.openConnection();
                        HttpURLConnection httpConnection = (HttpURLConnection) connection;

                        if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            String configJson = IOUtils.toString(httpConnection.getInputStream());
                            Log.w("Config", configJson);
                            ConfigPojo cp = gson.fromJson(configJson, ConfigPojo.class);

                            FileOutputStream fos = openFileOutput("config.json", Context.MODE_PRIVATE);
                            IOUtils.write(configJson, fos);
                            IOUtils.closeQuietly(fos);

                            NewApplication.getInstance().setConfigPojo(cp);
                            httpConnection.disconnect();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error Getting config", e);
                    }

                    // Connect to the web site
                    cleanStorage(getDb(getApplicationContext()));

                    String srcUrl = Utils.getPaper(getApplicationContext());

                    String body = downloadNewFile(srcUrl,4);

                    ArsEntity[] ents = gson.fromJson(body, ArsEntity[].class);

                    if(clean) {
                        synchronized (NewApplication.getInstance().getItemsTreeSet()) {
                            NewApplication.getInstance().getItemsTreeSet().clear();
                            NewApplication.getInstance().getItemsLookup().clear();
                        }
                    }

                    List<Future<ArsEntity>> tasks = new ArrayList<Future<ArsEntity>>();
                    for (ArsEntity ae : ents) {
                        if (!NewApplication.getInstance().getItemsTreeSet().contains(ae)) {
                            //|| itemLookUp.get(ae.getLink()) == null ||itemLookUp.get(ae.getLink()).getType().equals(ArsEntity.NON_IMAGE)) {
                            ImageProcessorCallable ipc = new ImageProcessorCallable(ae, getTargetDir(getApplicationContext()), getApplicationContext(), getDb(getApplicationContext()));
                            tasks.add(NewApplication.getInstance().getThreadPoolExecutor().submit(ipc));
                        }
                    }


//            rrwl.writeLock().tryLock(3, TimeUnit.MINUTES);
                    synchronized (NewApplication.getInstance().getItemsTreeSet()) {
                        for (Future<ArsEntity> future : tasks) {
                            try {
                                ArsEntity arsEntity = future.get(10, TimeUnit.MINUTES);
                                if (!NewApplication.getInstance().getItemsTreeSet().add(arsEntity)) {
                                    Log.w(TAG, "Already contained" + arsEntity);
                                }
                                System.gc();
                                NewApplication.getInstance().getItemsLookup().put(arsEntity.getLink(), arsEntity);


                            } catch (InterruptedException e) {
                                Log.e(TAG, "Got an interrupt while waiting to complete", e);
                            } catch (ExecutionException e) {
                                Log.e(TAG, "Got an execution exception while waiting for task to complete", e);
                            } catch (TimeoutException e) {
                                Log.e(TAG, "It took longer than 4 minutes an image to download. So going to cancel it", e);
                                future.cancel(true);
                            }
                        }
                    }


                    if (NewApplication.getInstance().getItemsTreeSet().isEmpty()) {
                        FileInputStream fis = openFileInput("list.json");
                        String serial = IOUtils.toString(fis);
                        TreeSet<ArsEntity> fromJson =
                                gson.fromJson(serial, new TypeToken<TreeSet<ArsEntity>>() {
                                }.getType());
                        NewApplication.getInstance().getItemsTreeSet().addAll(fromJson);
//                        itemLookUp = new HashMap<String, ArsEntity>();
                        for (ArsEntity arsEntity : fromJson.descendingSet()) {
                            NewApplication.getInstance().getItemsLookup().put(arsEntity.getLink(), arsEntity);
                        }
                        IOUtils.closeQuietly(fis);
                    }

                    String serial = gson.toJson(NewApplication.getInstance().getItemsTreeSet());

                    FileOutputStream fos = openFileOutput("list.json", Context.MODE_PRIVATE);
                    IOUtils.write(serial, fos);
                    IOUtils.closeQuietly(fos);
                } else {
                    FileInputStream fis = openFileInput("list.json");
                    String serial = IOUtils.toString(fis);
                    TreeSet<ArsEntity> fromJson =
                            gson.fromJson(serial, new TypeToken<TreeSet<ArsEntity>>() {
                            }.getType());
                    NewApplication.getInstance().getItemsTreeSet().addAll(fromJson);
//                    itemLookUp = new HashMap<String, ArsEntity>();
                    for (ArsEntity arsEntity : fromJson.descendingSet()) {
                        NewApplication.getInstance().getItemsLookup().put(arsEntity.getLink(), arsEntity);
                    }
                    IOUtils.closeQuietly(fis);
                }
//            closeDb(getApplicationContext());
                Bundle bnd = new Bundle();
                bnd.putString("action", "update");
                Log.e("DataFetch", "Creating Message");
                Message msg = new Message();
                msg.setData(bnd);
                uiHandler.sendMessage(msg);
            } catch (Exception e) {
                Log.e(TAG, "Straight problem Parsing", e);
                e.printStackTrace();
            }
        }


    }


    public void cleanStorage(SQLiteDatabase sqLiteDatabase) {

        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR, -48);
        Date daysBeforeDateWeb = cal.getTime();
        List<ArsEntity> ents = getFileEntryBeforeDate(daysBeforeDateWeb, sqLiteDatabase);
        for (ArsEntity arsEntity : ents) {
            if (arsEntity != null) {
                if (arsEntity.getLocalImgPath() != null && !arsEntity.getLocalImgPath().isEmpty()) {
                    try {
                        File imageFile = new File(arsEntity.getLocalImgPath());
                        boolean retVal = FileUtils.deleteQuietly(imageFile);
                        deleteEntry(arsEntity);
                    } catch (Exception e) {
                        Log.e(TAG, "Got an exception while trying to delete an image", e);
                    }
                } else {
                    Log.e(TAG, "Found a image that had no file path" + arsEntity);
                }
            } else {
                Log.e(TAG, "For a entry that was null. This is bad");
            }
        }
    }

    public void closeDb(Context context) {
        try {
            getDb(context).close();
        } catch (Exception e) {
            Log.e(TAG, "Problem closing Db", e);
        }
    }

    public static class ImageProcessorCallable implements Callable<ArsEntity> {

        public static int OLD = 0;
        public static int NEW = 1;
        public static int UPDATED = 2;

        private ArsEntity arsEntity;
        private File imgDir;
        private Context context;
        private SQLiteDatabase sqLiteDatabase;
        private int type;

        public ImageProcessorCallable(ArsEntity arsEntity, File imgDir, Context context, SQLiteDatabase sqLiteDatabase) {
            this.arsEntity = arsEntity;
            this.imgDir = imgDir;
            this.context = context;
            this.sqLiteDatabase = sqLiteDatabase;
        }

        @Override
        public ArsEntity call() throws Exception {
            UUID idOne = UUID.randomUUID();
            ArsEntity tabent = getArsEntityById(arsEntity.getLink(),sqLiteDatabase);
            if(tabent == null) {
                if (arsEntity.getImgUrl() != null && !arsEntity.getImgUrl().isEmpty()) {
                    try {
                        File downLoaded = downloadNewFile(arsEntity.getImgUrl(), idOne.toString() + ".jpg", imgDir, context);

                        FileUtils.copyFileToDirectory(downLoaded, getContext().getFilesDir());
                        arsEntity.setLocalImgPath(downLoaded.getPath());
                        arsEntity.setType(ArsEntity.IMAGE_TYPE);
                    } catch (RuntimeException re) {
                        Log.e(TAG,"Error downloading image",re);
                        arsEntity.setLocalImgPath(null);
                        arsEntity.setType(ArsEntity.NON_IMAGE);
                    }
                } else {
                    arsEntity.setLocalImgPath(null);
                    arsEntity.setType(ArsEntity.NON_IMAGE);
                }
                type = NEW;
            } else {
                arsEntity = tabent;
                type = OLD;
            }
            arsEntity.setLmt(new Date());
            sqLiteDatabase.insertWithOnConflict(ArsEntity.TableName, null, arsEntity.getContentValues(), SQLiteDatabase.CONFLICT_REPLACE);
            return arsEntity;
        }

        public ArsEntity getArsEntity() {
            return arsEntity;
        }

        public void setArsEntity(ArsEntity arsEntity) {
            this.arsEntity = arsEntity;
        }

        public File getImgDir() {
            return imgDir;
        }

        public void setImgDir(File imgDir) {
            this.imgDir = imgDir;
        }

        public Context getContext() {
            return context;
        }

        public void setContext(Context context) {
            this.context = context;
        }
    }

    public static class TextProcessorCallable implements Callable<ArsEntity> {

        public static int OLD = 0;
        public static int NEW = 1;
        public static int UPDATED = 2;

        private ArsEntity arsEntity;
        private File imgDir;
        private Context context;
        private SQLiteDatabase sqLiteDatabase;
        private int type;

        public TextProcessorCallable(ArsEntity arsEntity, File imgDir, Context context, SQLiteDatabase sqLiteDatabase) {
            this.arsEntity = arsEntity;
            this.imgDir = imgDir;
            this.context = context;
            this.sqLiteDatabase = sqLiteDatabase;
        }

        @Override
        public ArsEntity call() throws Exception {
            ArsEntity tabent = getArsEntityById(arsEntity.getLink(),sqLiteDatabase);
            if (arsEntity.getLink() != null && !arsEntity.getLink().isEmpty() &&
                    (arsEntity.getText() == null || arsEntity.getText().isEmpty())) {
                try {
                    URL url = new URL(arsEntity.getLink());
                    URLConnection con = url.openConnection();
                    InputStream in = con.getInputStream();
                    String encoding = con.getContentEncoding();
                    encoding = encoding == null ? "UTF-8" : encoding;
                    String body = IOUtils.toString(in, encoding);

                    body = body.replace("<![CDATA[", "").replace("]]>", "");

                    Document document = Jsoup.parse(body);
                    Elements els = document.select(".article-content p");
                    if(els.isEmpty()) {
                        els = document.select(".full-content p");
                    }
                    StringBuilder sb = new StringBuilder();
                    for(Element el : els) {
                        if(!el.hasAttr("class") || el.attr("class") == null || !el.attr("class").contains("has-image") ||
                                !el.attr("class").contains("photo-credit")) {
                            sb.append(el.text() + " ");
                        }
                    }
                    if(tabent != null) {
                        arsEntity.copyAll(tabent);
                    }
                    arsEntity.setText(sb.toString());
                    System.out.println(arsEntity.getText());
                    arsEntity.setLmt(new Date());
                    sqLiteDatabase.insertWithOnConflict(ArsEntity.TableName, null, arsEntity.getContentValues(), SQLiteDatabase.CONFLICT_REPLACE);


                } catch (Exception e) {

                }
            }
            return arsEntity;
        }

        public ArsEntity getArsEntity() {
            return arsEntity;
        }

        public void setArsEntity(ArsEntity arsEntity) {
            this.arsEntity = arsEntity;
        }

        public File getImgDir() {
            return imgDir;
        }

        public void setImgDir(File imgDir) {
            this.imgDir = imgDir;
        }

        public Context getContext() {
            return context;
        }

        public void setContext(Context context) {
            this.context = context;
        }
    }

    private SQLiteDatabase getDb(Context context) {
        synchronized (DbHelperSync) {
            if (cacheDb == null) {
                cacheDb = new CacheDb(context, CacheDb.DB_VERSION);
            }
            return cacheDb.getWritableDatabase();
        }
    }

    public File getTargetDir(Context context) {
        if (targetDir == null) {
            targetDir = context.getDir(FILES_ROOT, Context.MODE_PRIVATE);
            try {
                FileUtils.forceMkdir(targetDir);
            } catch (IOException e) {
                Log.e(TAG, "Failed to make targetDir", e);
            }
        }
        return targetDir;
    }

    public static int touchArsEntityRow (ArsEntity arsEntity, SQLiteDatabase sldb) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ArsEntity.LAST_TOUCHED_COLUMN, new Date().getTime());
        String[] myStringArray = new String[1];
        myStringArray[0] = arsEntity.getLink();
        return sldb.updateWithOnConflict(ArsEntity.TableName, contentValues, String.format(Locale.US, "%s = ?", ArsEntity.LINK_COLUMN), myStringArray ,SQLiteDatabase.CONFLICT_REPLACE);
    }

    public static int deleteEntry (ArsEntity arsEntity, SQLiteDatabase sldb) {
        return deleteArsEntity(arsEntity.getLink(), sldb);
    }

    public static int deleteArsEntity (String linkUrl, SQLiteDatabase sldb) {
        String[] myStringArray = new String[1];
        myStringArray[0] = linkUrl;
        return sldb.delete(ArsEntity.TableName, String.format(Locale.US, "%s = ?", ArsEntity.LINK_COLUMN), myStringArray);
    }

    public static int deleteAllArsEntity (String linkUrl, SQLiteDatabase sldb) {
        String[] myStringArray = new String[1];
        myStringArray[0] = linkUrl;
        return sldb.delete(ArsEntity.TableName, String.format(Locale.US, "%s = ?", ArsEntity.LINK_COLUMN), myStringArray);
    }

    public static List<ArsEntity> getAllArsEntities(SQLiteDatabase sldb) {
        List<ArsEntity> entries = getArsEntity("1=1", sldb);
        return entries;
    }

    public static ArsEntity getArsEntityById(String id, SQLiteDatabase sldb) {
        List<ArsEntity> arsEntities = getArsEntity(String.format(Locale.US, "%s = '%s'", ArsEntity.LINK_COLUMN, id), sldb);
        return arsEntities.isEmpty() ? null : arsEntities.get(0);
    }

    public static List<ArsEntity> getArsEntity(String query, SQLiteDatabase sldb) {
        Cursor cursor = null;
        ArrayList<ArsEntity> result = new ArrayList<ArsEntity>();
        try {
            cursor = sldb.query(ArsEntity.TableName, ArsEntity.Columns, query, null, null, null, null);
            while (cursor.moveToNext()) {
                result.add(new ArsEntity(cursor));
            }
        } catch (SQLiteException sqle) {
                Log.e(TAG, "Sql exception", sqle);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    public void deleteEntry(ArsEntity arsEntity) {
        String[] myStringArray = new String[1];
        myStringArray[0] = arsEntity.getLink();
        int count = getDb(getApplicationContext()).delete(ArsEntity.TableName, String.format(Locale.US, "%s = ?", ArsEntity.LINK_COLUMN), myStringArray);
    }

    List<ArsEntity> getFileEntryBeforeDate(Date date, SQLiteDatabase sldb) {

        List<ArsEntity> entries = null;
        if (date != null) {
            long timeInMilli = date.getTime();
            entries = getArsEntity(String.format(Locale.US, "%s < %d", ArsEntity.LAST_TOUCHED_COLUMN, timeInMilli), sldb);
        }
        return entries;
    }

    public static File downloadNewFile(String downloadUrl, String filename, File parentDir, Context context) {
        File file = new File(parentDir, filename);
        try {
            URL url = new URL(downloadUrl);
            FileUtils.forceMkdir(parentDir);
            URLConnection connection = url.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "Opened Connection And got a 200 Response Code");
                FileUtils.copyInputStreamToFile(httpConnection.getInputStream(), file);
                FileInputStream fis = new FileInputStream(file);
                Bitmap btm = null;
                if(isTablet(context)) {
                    btm = BitmapFactory.decodeStream(fis);
                } else {
                    btm = createScaledBitmapFromStream(fis,512,512);
                }

                btm.compress(Bitmap.CompressFormat.JPEG, 80, new FileOutputStream(file));
                btm.recycle();
                btm = null;
            }
        } catch (MalformedURLException me) {
            Log.e(TAG, "Malformed Url requested [" + downloadUrl + "]", me);
        } catch (IOException ie) {
            Log.e(TAG, "IOException occurred while fetching [" + downloadUrl + "] to file [" + filename + "]", ie);
        }
        return file;
    }

    /**
     * Read the image from the stream and create a bitmap scaled to the desired
     * size.  Resulting bitmap will be at least as large as the
     * desired minimum specified dimensions and will keep the image proportions
     * correct during scaling.
     */
    static protected Bitmap createScaledBitmapFromStream( InputStream s, int minimumDesiredBitmapWidth, int minimumDesiredBitmapHeight ) {

        final BufferedInputStream is = new BufferedInputStream(s, 32*1024);
        try {
            final BitmapFactory.Options decodeBitmapOptions = new BitmapFactory.Options();
            // For further memory savings, you may want to consider using this option
            decodeBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565; // Uses 2-bytes instead of default 4 per pixel

            if( minimumDesiredBitmapWidth >0 && minimumDesiredBitmapHeight >0 ) {
                final BitmapFactory.Options decodeBoundsOptions = new BitmapFactory.Options();
                decodeBoundsOptions.inJustDecodeBounds = true;
                is.mark(32*1024); // 32k is probably overkill, but 8k is insufficient for some jpgs
                BitmapFactory.decodeStream(is,null,decodeBoundsOptions);
                is.reset();

                final int originalWidth = decodeBoundsOptions.outWidth;
                final int originalHeight = decodeBoundsOptions.outHeight;

                // inSampleSize prefers multiples of 2, but we prefer to prioritize memory savings
                decodeBitmapOptions.inSampleSize = Math.max(1,Math.min(originalWidth / minimumDesiredBitmapWidth, originalHeight / minimumDesiredBitmapHeight));
            }

            return BitmapFactory.decodeStream(is,null,decodeBitmapOptions);

        } catch( IOException e ) {
            throw new RuntimeException(e); // this shouldn't happen
        } finally {
            try {
                is.close();
            } catch( IOException ignored ) {}
        }

    }

//    public TreeSet<ArsEntity> getItemsTreeSetRef() {
//        return itemsTreeSetRef;
//    }
//
//    public void setItemsTreeSetRef(TreeSet<ArsEntity> itemsTreeSetRef) {
//        this.itemsTreeSetRef = itemsTreeSetRef;
//        this.itemLookUp.clear();
//
//        for(ArsEntity arsEntity : itemsTreeSetRef.descendingSet()) {
//            itemLookUp.put(arsEntity.getLink(),arsEntity);
//        }
//    }

//    public HashMap<String, ArsEntity> getItemLookUp() {
//        return itemLookUp;
//    }
//
//    public void setItemLookUp(HashMap<String, ArsEntity> itemLookUp) {
//        this.itemLookUp = itemLookUp;
//    }


    public static String getConentSource() {
        return conentSource;
    }

    public static void setConentSource(String conentSource) {
        ArsDataFetcherService.conentSource = conentSource;
    }

    public String downloadNewFile(String downloadUrl, int retries) {
        String result = null;
        HttpURLConnection httpConnection = null;
        for(int count = 0; count < retries; count++) {
            try {
                URL url = new URL(downloadUrl);
                URLConnection connection = url.openConnection();
                httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*");

                if (httpConnection.getResponseCode() >= 200 && httpConnection.getResponseCode() <= 400) {
                    String encoding = httpConnection.getContentEncoding();
                    encoding = encoding == null ? "UTF-8" : encoding;
                    result = IOUtils.toString(httpConnection.getInputStream(), encoding);
                    if(result == null) {
                        Log.e(TAG, "Returning null [" + downloadUrl + "]" );
                    }
                    break;
                } else {
                    Log.e(TAG, "Got bad response [" + downloadUrl + "] [" +httpConnection.getResponseCode() + "]" );
                }
            } catch (MalformedURLException me) {
                Log.e(TAG, "Malformed Url requested [" + downloadUrl + "]", me);
            } catch (Exception ie) {
                Log.e(TAG, "Error occured while fetching [" + downloadUrl + "]", ie);
            } finally {
                if (httpConnection != null) {
                    try {
                        httpConnection.disconnect();
                    } catch (Exception e) {
                        //Nothing to do, just don't crash app
                    }
                }
            }
        }
        return result;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
