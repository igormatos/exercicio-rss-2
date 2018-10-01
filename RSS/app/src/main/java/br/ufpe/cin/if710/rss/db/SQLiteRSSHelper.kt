package br.ufpe.cin.if710.rss.db

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import br.ufpe.cin.if710.rss.ItemRSS
import org.jetbrains.anko.db.createTable
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select


class SQLiteRSSHelper constructor(internal var c: Context) : SQLiteOpenHelper(c, DATABASE_NAME, null, DB_VERSION) {

    private lateinit var sqliteDB: SQLiteDatabase

    val items: List<ItemRSS>?
        @Throws(SQLException::class)
        get() {
            val list = mutableListOf<ItemRSS>()

            sqliteDB.select(DATABASE_TABLE).whereArgs("($ITEM_UNREAD = {bool})",
                    "bool" to false)
                    .exec {
                        val title = getString(getColumnIndex(RssProviderContract.TITLE))
                        val pubDate = getString(getColumnIndex(RssProviderContract.DATE))
                        val description = getString(getColumnIndex(RssProviderContract.DESCRIPTION))
                        val link = getString(getColumnIndex(RssProviderContract.LINK))
                        val item = ItemRSS(title, link, pubDate, description)
                        list.add(item)
                    }

            sqliteDB.close()

            return list
        }

    override fun onCreate(db: SQLiteDatabase) {
        sqliteDB = db

        //Executa o comando de criação de tabela
//        db.execSQL(CREATE_DB_COMMAND)
        db.createTable(DATABASE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //estamos ignorando esta possibilidade no momento
        throw RuntimeException("nao se aplica")
    }

    //IMPLEMENTAR ABAIXO
    //Implemente a manipulação de dados nos métodos auxiliares para não ficar criando consultas manualmente
    fun insertItem(item: ItemRSS): Long {
        return insertItem(item.title, item.pubDate, item.description, item.link)
    }

    fun insertItem(title: String, pubDate: String, description: String, link: String): Long {
        sqliteDB.insert(DATABASE_TABLE,
                ITEM_TITLE to title,
                ITEM_DATE to pubDate,
                ITEM_DESC to description,
                ITEM_LINK to link
        )

        sqliteDB.close()

        return 0
    }

    @Throws(SQLException::class)
    fun getItemRSS(link: String): ItemRSS? {
        var result: ItemRSS? = null

        sqliteDB.select(DATABASE_TABLE)
                .whereArgs("($ITEM_LINK > {link})",
                        "link" to link)
                .exec {
                    val title = getString(getColumnIndex(ITEM_TITLE))
                    val pubDate = getString(getColumnIndex(ITEM_DATE))
                    val description = getString(getColumnIndex(ITEM_DESC))
                    val link = getString(getColumnIndex(ITEM_LINK))
                    result = ItemRSS(title, link, pubDate, description)
                    close()
                }
        sqliteDB.close()
        return result
    }

    fun markAsUnread(link: String): Boolean {
        return false
    }

    fun markAsRead(link: String): Boolean {
        return false
    }

    companion object {
        //Nome do Banco de Dados
        private val DATABASE_NAME = "rss"

        //Nome da tabela do Banco a ser usada
        val DATABASE_TABLE = "items"

        //Versão atual do banco
        private val DB_VERSION = 1

        private var db: SQLiteRSSHelper? = null

        //Definindo Singleton
        fun getInstance(c: Context): SQLiteRSSHelper {
            if (db == null) {
                db = SQLiteRSSHelper(c.applicationContext)
            }

            return db as SQLiteRSSHelper
        }

        //Definindo constantes que representam os campos do banco de dados
        val ITEM_ROWID = RssProviderContract._ID
        val ITEM_TITLE = RssProviderContract.TITLE
        val ITEM_DATE = RssProviderContract.DATE
        val ITEM_DESC = RssProviderContract.DESCRIPTION
        val ITEM_LINK = RssProviderContract.LINK
        val ITEM_UNREAD = RssProviderContract.UNREAD

        //Definindo constante que representa um array com todos os campos
        val columns = arrayOf(ITEM_ROWID, ITEM_TITLE, ITEM_DATE, ITEM_DESC, ITEM_LINK, ITEM_UNREAD)

        //Definindo constante que representa o comando de criação da tabela no banco de dados
        private val CREATE_DB_COMMAND = "CREATE TABLE " + DATABASE_TABLE + " (" +
                ITEM_ROWID + " integer primary key autoincrement, " +
                ITEM_TITLE + " text not null, " +
                ITEM_DATE + " text not null, " +
                ITEM_DESC + " text not null, " +
                ITEM_LINK + " text not null, " +
                ITEM_UNREAD + " boolean not null);"
    }

}