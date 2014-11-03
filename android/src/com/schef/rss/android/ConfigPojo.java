package com.schef.rss.android;

import java.io.Serializable;

/**
 * Created by scheffela on 11/2/14.
 */
public class ConfigPojo implements Serializable{

    public String viceUrl1 = "http://www.vice.com/news/page/1";
    public String viceUrl2 = "http://www.vice.com/news/page/2";

    public String articleCssPage1 = "section.items-container > article.item";
    public String articleCssPage2 = "section.items-container > article.item";

    public String titleCss = "h2.item-title a";

    public String linkCss = "h2.item-title a";
    public String linkCssAttrName = "href";
    public String linkRegexFind = "";
    public String linkRegexReplace = "";

    public String imageCss = "div.image-container > a > noscript > img";
    public String imageCssAttrName = "src";
    public String imageRegexFind = "";
    public String imageRegexReplace = "";

    public String pubTimeCss = "div.item-meta-information span.publish-time";
    public String pubTimeAttrName = "data-publish-date";




}
