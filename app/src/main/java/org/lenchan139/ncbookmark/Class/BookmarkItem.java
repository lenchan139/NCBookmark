package org.lenchan139.ncbookmark.Class;

/**
 * Created by len on 10/3/2017.
 */

public class BookmarkItem {
    String title;
    String url;
    String tags;

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTags() {
        return tags;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return url+"|"+title+"|"+tags;
    }
}