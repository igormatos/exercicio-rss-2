package br.ufpe.cin.if710.rss

import android.provider.BaseColumns

class ItemRSS(val title: String, val link: String, val pubDate: String, val description: String) {

    override fun toString(): String {
        return title
    }

    companion object {
        const val TABLE = "ITEM_TABLE"
        const val _ID = BaseColumns._ID
        const val TITLE = "title"
        const val DATE = "pubDate"
        const val DESCRIPTION = "description"
        const val LINK = "link"
        const val HAS_READ = "read"
        val COLUMNS = arrayOf(TITLE, DATE, DESCRIPTION, LINK, HAS_READ)
    }
}
