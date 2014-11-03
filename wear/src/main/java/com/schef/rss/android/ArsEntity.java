package com.schef.rss.android;

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
}
