package com.schef.rss.android.db;

import android.content.ContentValues;
import android.database.Cursor;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;

/**
 * Created by scheffela on 7/26/14.
 */
public class ArsEntity implements Comparable, Serializable {

    protected static final String TAG = ArsEntity.class.getSimpleName();

    public static final String TableName = "ImageEntry";
    public static String LINK_COLUMN = "Link";
    public static String TITLE_COLUMN = "Title";
    public static String IMAGE_URL_COLUMN = "ImageUrl";
    public static String LOCAL_FILE_PATH_COLUMN = "LocalFilePath";
    public static String TYPE_COLUMN = "Type";
    public static String LAST_TOUCHED_COLUMN = "LastTouched";
    public static String PUB_DATE = "PubDate";
    public static String ARTICLE_TEXT_COLUMN = "artText";

    public static final String[] Columns = new String[] {LINK_COLUMN, TITLE_COLUMN, IMAGE_URL_COLUMN, LOCAL_FILE_PATH_COLUMN, TYPE_COLUMN, LAST_TOUCHED_COLUMN, PUB_DATE, ARTICLE_TEXT_COLUMN};
    public static final String[] ColumnsTypes = new String[] {"TEXT PRIMARY KEY", "TEXT", "TEXT", "TEXT", "INTEGER", "INTEGER", "INTEGER", "TEXT" };

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
    public String text;

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
        text = cursor.getString(7);
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ContentValues getContentValues() {
        ContentValues result = new ContentValues();
        result.put(LINK_COLUMN, this.getLink());
        result.put(TITLE_COLUMN, this.getTitle());
        result.put(IMAGE_URL_COLUMN, this.getImgUrl());
        result.put(LOCAL_FILE_PATH_COLUMN, this.getLocalImgPath());
        result.put(TYPE_COLUMN, this.getType());
        result.put(ARTICLE_TEXT_COLUMN, this.getText());


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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArsEntity)) return false;

        ArsEntity arsEntity = (ArsEntity) o;

        if (imgUrl != null ? !imgUrl.equals(arsEntity.imgUrl) : arsEntity.imgUrl != null)
            return false;
        if (link != null ? !link.equals(arsEntity.link) : arsEntity.link != null) return false;
        if (lmt != null ? !lmt.equals(arsEntity.lmt) : arsEntity.lmt != null) return false;
        if (localImgPath != null ? !localImgPath.equals(arsEntity.localImgPath) : arsEntity.localImgPath != null)
            return false;
        if (pubDate != null ? !pubDate.equals(arsEntity.pubDate) : arsEntity.pubDate != null)
            return false;
        if (title != null ? !title.equals(arsEntity.title) : arsEntity.title != null) return false;
        if (type != null ? !type.equals(arsEntity.type) : arsEntity.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = link != null ? link.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (imgUrl != null ? imgUrl.hashCode() : 0);
        result = 31 * result + (localImgPath != null ? localImgPath.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (lmt != null ? lmt.hashCode() : 0);
        result = 31 * result + (pubDate != null ? pubDate.hashCode() : 0);
        return result;
    }

    public ArsEntity copyAll (ArsEntity arsEntity) {
        this.setPubDate(arsEntity.getPubDate());
        this.setText(arsEntity.getText());
        this.setLink(arsEntity.getLink());
        this.setLmt(arsEntity.getLmt());
        this.setTitle(arsEntity.getTitle());
        this.setImgUrl(arsEntity.getImgUrl());
        this.setLocalImgPath(arsEntity.getLocalImgPath());
        this.setType(arsEntity.getType());
        return this;
    }
}
