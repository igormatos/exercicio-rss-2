package br.ufpe.cin.if710.rss.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import br.ufpe.cin.if710.rss.ItemRSS
import org.jetbrains.anko.db.*

class SQLiteAnkoHelper(context: Context) :
        ManagedSQLiteOpenHelper(context, "rss_app", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
            db.createTable(ItemRSS.TABLE,
                    true,
                    ItemRSS._ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                    ItemRSS.DATE to TEXT,
                    ItemRSS.DESCRIPTION to TEXT,
                    ItemRSS.LINK to TEXT,
                    ItemRSS.TITLE to TEXT,
                    ItemRSS.HAS_READ to INTEGER)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.dropTable(ItemRSS.TABLE, ifExists = true)
    }

    fun getItem(link: String) : ItemRSS? {

        return use {
            select(ItemRSS.TABLE, *ItemRSS.COLUMNS)
                    .whereArgs("${ItemRSS.LINK} = {link}", "link" to link)
                    .parseOpt(object: MapRowParser<ItemRSS> {
                        override fun parseRow(columns: Map<String, Any?>): ItemRSS {
                            val title = columns[ItemRSS.TITLE] as String
                            val pubDate = columns[ItemRSS.DATE] as String
                            val description = columns[ItemRSS.DESCRIPTION] as String
                            val link = columns[ItemRSS.LINK] as String

                            return ItemRSS(title, link, pubDate, description)
                        }
                    })
        }
    }

    fun getUnreadItems() : List<ItemRSS> {
        val list: MutableList<ItemRSS> = mutableListOf()

        use {
            select(ItemRSS.TABLE, *ItemRSS.COLUMNS)
                    .whereArgs("${ItemRSS.HAS_READ} = 0")
                    .exec {
                        while(moveToNext()) {
                            val title = getString(getColumnIndex(ItemRSS.TITLE))
                            val pubDate = getString(getColumnIndex(ItemRSS.DATE))
                            val description = getString(getColumnIndex(ItemRSS.DESCRIPTION))
                            val link = getString(getColumnIndex(ItemRSS.LINK))
                            list.add(ItemRSS(title, link, pubDate, description))
                        }
                    }
        }

        return list
    }

    fun insertItem(item: ItemRSS) {

        getItem(item.link)?.let {
            return
        }

        use {
            insert(ItemRSS.TABLE,
                    ItemRSS.TITLE to item.title,
                    ItemRSS.HAS_READ to 0,
                    ItemRSS.DESCRIPTION to item.description,
                    ItemRSS.LINK to item.link,
                    ItemRSS.DATE to item.pubDate)
        }

    }

    fun markRead(item: ItemRSS) {
        use {
            update(ItemRSS.TABLE, ItemRSS.HAS_READ to 1)
                    .whereArgs("${ItemRSS.LINK} = {link}", "link" to item.link)
                    .exec()
        }
    }

    companion object {
        private var instance: SQLiteAnkoHelper? = null

        @Synchronized
        fun getInstance(context: Context): SQLiteAnkoHelper {
            if (instance == null) {
                instance = SQLiteAnkoHelper(context.applicationContext)
            }
            return instance!!
        }
    }
}

val Context.database: SQLiteAnkoHelper
    get() = SQLiteAnkoHelper.getInstance(this)