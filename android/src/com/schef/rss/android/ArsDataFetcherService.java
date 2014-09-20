package com.schef.rss.android;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.schef.rss.android.db.ArsEntity;
import com.schef.rss.android.db.CacheDb;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    protected static final String TAG = ArsDataFetcherService.class.getSimpleName();

    public CacheDb cacheDb;
    private static final Object DbHelperSync = new Object();
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    private File targetDir = null;

    private ReentrantReadWriteLock rrwl = new ReentrantReadWriteLock(true);

    private Handler uiHandler;

    private TreeSet<ArsEntity> itemsTreeSetRef;

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
        try {
            // Connect to the web site

            URL url = new URL(arsRSSFeedUrl);
            URLConnection con = url.openConnection();
            InputStream in = con.getInputStream();
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            String body = IOUtils.toString(in, encoding);

            body = body.replace("<![CDATA[", "").replace("]]>", "");

            Document document = Jsoup.parse(body);
            Elements items = document.select("channel > item");

            List<ArsEntity> ents = new ArrayList<ArsEntity>();

            if (!items.isEmpty()) {
                for (int i = 0; i < items.size(); i++) {
                    ArsEntity ae = new ArsEntity();
                    Element el = items.get(i);
                    Elements elTitle = el.getElementsByTag("title");
                    if (!elTitle.isEmpty()) {
                        String title = elTitle.get(0).text();
                        ae.setTitle(title);
                    }
                    Elements elLink = el.getElementsByTag("guid");
                    if (!elLink.isEmpty()) {
                        String link = elLink.get(0).text();
                        ae.setLink(link);
                    }
                    Elements elPubDate = el.getElementsByTag("pubDate");
                    if (!elPubDate.isEmpty()) {
                        String pubDate = elPubDate.get(0).text();
                        try {
                            DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
                            Date date = formatter.parse(pubDate);
                            ae.setPubDate(date);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Elements encode = el.getElementsByTag("content:encoded");
                    Element wrap = encode.get(0).getElementById("rss-wrap");

//Sat, 26 Jul 2014 22:05:02 +0000
//                    Element wrap = el.getElementById("rss-wrap");
                    Elements img = wrap.getElementsByTag("img");
                    if (!img.isEmpty()) {
                        String imgUrls = img.get(0).attr("src");
                        ae.setImgUrl(imgUrls);
                    }
                    ents.add(ae);
                }
            }

            List<Future<ArsEntity>> tasks = new ArrayList<Future<ArsEntity>>();
            for(ArsEntity ae : ents){
                ImageProcessorCallable ipc = new ImageProcessorCallable(ae,getTargetDir(getApplicationContext()),getApplicationContext(), getDb(getApplicationContext()));
                tasks.add(NewApplication.getInstance().getThreadPoolExecutor().submit(ipc));
            }

//            rrwl.writeLock().tryLock(3, TimeUnit.MINUTES);
            synchronized (itemsTreeSetRef) {
                for (Future<ArsEntity> future : tasks) {
                    try {
                        ArsEntity arsEntity = future.get(10, TimeUnit.MINUTES);
                        itemsTreeSetRef.add(arsEntity);
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

            closeDb(getApplicationContext());
            Bundle bnd = new Bundle();
            bnd.putString("action","update");
            Log.e("DataFetch","Creating Message");
            Message msg = new Message();
            msg.setData(bnd);
            uiHandler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
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
                    File downLoaded = downloadNewFile(arsEntity.getImgUrl(), idOne.toString() + ".jpg", imgDir);
                    FileUtils.copyFileToDirectory(downLoaded,getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES));
                    arsEntity.setLocalImgPath(downLoaded.getPath());
                    arsEntity.setType(ArsEntity.IMAGE_TYPE);
                } else {
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
        try {
            cursor = sldb.query(ArsEntity.TableName, ArsEntity.Columns, query, null, null, null, null);
            ArrayList<ArsEntity> result = new ArrayList<ArsEntity>();
            while (cursor.moveToNext()) {
                result.add(new ArsEntity(cursor));
            }
            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void deleteEntry(ArsEntity arsEntity) {
        String[] myStringArray = new String[1];
        myStringArray[0] = arsEntity.getLink();
        int count = getDb(getApplicationContext()).delete(ArsEntity.TableName, String.format(Locale.US, "%s = ?", ArsEntity.LINK_COLUMN), myStringArray);
    }



    public static File downloadNewFile(String downloadUrl, String filename, File parentDir) {
        File file = new File(parentDir, filename);
        try {
            URL url = new URL(downloadUrl);
            FileUtils.forceMkdir(parentDir);
            URLConnection connection = url.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "Opened Connection And got a 200 Response Code");
                FileUtils.copyInputStreamToFile(httpConnection.getInputStream(), file);

            }
        } catch (MalformedURLException me) {
            Log.e(TAG, "Malformed Url requested [" + downloadUrl + "]", me);
        } catch (IOException ie) {
            Log.e(TAG, "IOException occurred while fetching [" + downloadUrl + "] to file [" + filename + "]", ie);
        }
        return file;
    }

    public ReentrantReadWriteLock getRrwl() {
        return rrwl;
    }

    public void setRrwl(ReentrantReadWriteLock rrwl) {
        this.rrwl = rrwl;
    }

    public TreeSet<ArsEntity> getItemsTreeSetRef() {
        return itemsTreeSetRef;
    }

    public void setItemsTreeSetRef(TreeSet<ArsEntity> itemsTreeSetRef) {
        this.itemsTreeSetRef = itemsTreeSetRef;
    }
}
