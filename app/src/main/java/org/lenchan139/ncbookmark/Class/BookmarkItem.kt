package org.lenchan139.ncbookmark.Class

/**
 * Created by len on 10/3/2017.
 */

class BookmarkItem {
    var title: String? = null
    var url: String? = null
    var tags: String? = null
    var id: Int = 0

    override fun toString(): String {
        return "$url|$title|$tags"
    }
}
