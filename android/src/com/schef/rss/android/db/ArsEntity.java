package com.schef.rss.android.db;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Date;
import java.util.Locale;

/**
 * Created by scheffela on 7/26/14.
 */
public class ArsEntity implements Comparable {

    protected static final String TAG = ArsEntity.class.getSimpleName();

    public static final String TableName = "ImageEntry";
    public static String LINK_COLUMN = "Link";
    public static String TITLE_COLUMN = "Title";
    public static String IMAGE_URL_COLUMN = "ImageUrl";
    public static String LOCAL_FILE_PATH_COLUMN = "LocalFilePath";
    public static String TYPE_COLUMN = "Type";
    public static String LAST_TOUCHED_COLUMN = "LastTouched";
    public static String PUB_DATE = "PubDate";

    public static final String[] Columns = new String[] {LINK_COLUMN, TITLE_COLUMN, IMAGE_URL_COLUMN, LOCAL_FILE_PATH_COLUMN, TYPE_COLUMN, LAST_TOUCHED_COLUMN, PUB_DATE};
    public static final String[] ColumnsTypes = new String[] {"TEXT PRIMARY KEY", "TEXT", "TEXT", "TEXT", "INTEGER", "INTEGER", "INTEGER" };

    public static ITableDescription getTableDescription() {
        return new ITableDescription() {
            @Override
            public String getTableName() {
                return ArsEntity.TableName;
            }

            @Override
            public String[] getColumns() {
                return ArsEntity.Columns;
            }

            @Override
            public String[] getColumnsTypes() {
                return ArsEntity.ColumnsTypes;
            }

            @Override
            public String[] getPostCreationSql() {
                return new String[] {
                        String.format(Locale.US, "CREATE INDEX %1$s_%2$s_Index ON %1$s ( %2$s );", TableName, LOCAL_FILE_PATH_COLUMN),
                        String.format(Locale.US, "CREATE INDEX %1$s_%2$s_Index ON %1$s ( %2$s );", TableName, IMAGE_URL_COLUMN),
                        String.format(Locale.US, "CREATE INDEX %1$s_%2$s_Index ON %1$s ( %2$s );", TableName, LINK_COLUMN)
                };
            }

            @Override
            public String[] getPreDeletionSql() {
                return new String[] {
                        String.format(Locale.US, "DROP INDEX IF EXISTS %s_%s_Index", TableName, LOCAL_FILE_PATH_COLUMN),
                        String.format(Locale.US, "DROP INDEX IF EXISTS %s_%s_Index", TableName, IMAGE_URL_COLUMN),
                        String.format(Locale.US, "DROP INDEX IF EXISTS %s_%s_Index;", TableName, LINK_COLUMN)
                };
            }

        };
    }

    public static final int NON_IMAGE = 0;
    public static final int IMAGE_TYPE = 1;

    public String link;
    public String title;
    public String imgUrl;
    public String localImgPath;
    public Integer type;
    public Date lmt;
    public Date pubDate;

    public ArsEntity() {
        super();
    }

    public ArsEntity(Cursor cursor) {

        link = cursor.getString(0);
        title = cursor.getString(1);
        imgUrl = cursor.getString(2);
        localImgPath = cursor.getString(3);
        type = cursor.getInt(4);
        lmt = cursor.isNull(5) ? null : new Date(cursor.getLong(5));
        pubDate = cursor.isNull(6) ? null : new Date(cursor.getLong(6));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getLocalImgPath() {
        return localImgPath;
    }

    public void setLocalImgPath(String localImgPath) {
        this.localImgPath = localImgPath;
    }

    public Date getLmt() {
        return lmt;
    }

    public void setLmt(Date lmt) {
        this.lmt = lmt;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Date getPubDate() {
        return pubDate;
    }

    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }

    public ContentValues getContentValues() {
        ContentValues result = new ContentValues();
        result.put(LINK_COLUMN, this.getLink());
        result.put(TITLE_COLUMN, this.getTitle());
        result.put(IMAGE_URL_COLUMN, this.getImgUrl());
        result.put(LOCAL_FILE_PATH_COLUMN, this.getLocalImgPath());
        result.put(TYPE_COLUMN, this.getType());
        result.put(TITLE_COLUMN, this.getTitle());


        if(this.getLmt() != null) {
            result.put(LAST_TOUCHED_COLUMN, this.getLmt().getTime());
        } else {
            result.put(LAST_TOUCHED_COLUMN, System.currentTimeMillis());
        }

        if(this.getPubDate() != null) {
            result.put(PUB_DATE, this.getPubDate().getTime());
        } else {
            result.put(PUB_DATE, System.currentTimeMillis());
        }

        return result;
    }

    @Override
    public int compareTo(Object another) {
        ArsEntity anotherArs = (ArsEntity) another;
        return anotherArs.getPubDate().compareTo(this.getPubDate());
    }

    @Override
    public String toString() {
        return "ArsEntity{" +
                "link='" + link + '\'' +
                ", title='" + title + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", localImgPath='" + localImgPath + '\'' +
                ", type=" + type +
                ", lmt=" + lmt +
                ", pubDate=" + pubDate +
                '}';
    }
}
