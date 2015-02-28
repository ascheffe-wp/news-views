package com.schef.rss.android;


import java.io.Serializable;
import java.util.Date;

/**
 * Created by scheffela on 7/26/14.
 */
public class ArsEntity implements Comparable, Serializable {



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
    public String pubName;
    public int order;

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

    public String getText() {
        return text;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setText(String localText) {

        localText = localText.replaceAll("<.*?>", "").replaceAll("\\(.*?\\)", "");
        localText = localText.replaceAll("\\n", "").replaceAll(":", "");
        localText = localText.replaceAll("&.{0,10};", "").replaceAll("\\[", "");
        localText = localText.replaceAll("\\]", "").replaceAll("-", "");
        localText = localText.replaceAll("\\.", "\\. ").replaceAll("' ", " ");
        localText = localText.replaceAll("\u201C", "\"").replaceAll("\u201D", "\"");
        localText = localText.replaceAll("\u2018", "").replaceAll("\u2019", "");
        localText = localText.replaceAll("\\?", " ");

        this.text = localText;
    }

    public String getPubName() {
        return pubName;
    }

    public void setPubName(String pubName) {
        this.pubName = pubName;
    }

    @Override
    public int compareTo(Object another) {
        ArsEntity anotherArs = (ArsEntity) another;
        int cmp = this.order > anotherArs.order ? +1 : this.order < anotherArs.order ? -1 : 0;
        return cmp;
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
                ", order=" + order +
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
        if (order != arsEntity.order) return false;
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
        result = 31 * result + (order);
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
        this.setOrder(arsEntity.getOrder());
        return this;
    }
}