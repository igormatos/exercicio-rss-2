package br.ufpe.cin.if710.rss

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import android.content.Intent
import android.content.SharedPreferences
import android.view.Menu
import android.view.MenuItem
import br.ufpe.cin.if710.rss.db.SQLiteRSSHelper
import br.ufpe.cin.if710.rss.db.database
import org.jetbrains.anko.*


class MainActivity : Activity() {

    //ao fazer envio da resolucao, use este link no seu codigo!
    private lateinit var RSS_FEED: String

    //OUTROS LINKS PARA TESTAR...
    //http://rss.cnn.com/rss/edition.rss
    //http://pox.globo.com/rss/g1/brasil/
    //http://pox.globo.com/rss/g1/ciencia-e-saude/
    //http://pox.globo.com/rss/g1/tecnologia/

    //use ListView ao invés de TextView - deixe o atributo com o mesmo nome
    private lateinit var conteudoRSS: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var preferences: SharedPreferences
    private lateinit var helper: SQLiteRSSHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        conteudoRSS = findViewById(R.id.conteudoRSS)

        viewManager = LinearLayoutManager(this)
        preferences = defaultSharedPreferences


        helper = SQLiteRSSHelper(this)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return true
    }

    fun openSettingsActivity(item: MenuItem?) {
//        Toast.makeText(this, "Teste", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()

        RSS_FEED = preferences.getString("rssfeed", "")

        var teste = database.getUnreadItems().count()

        doAsync {
            try {

                if (RSS_FEED.isEmpty()) {
                    uiThread {
                        showDialogToAddFeed()
                    }

                    return@doAsync
                }

                val feedXML = getRssFeed(RSS_FEED)

                val list = ParserRSS.parse(feedXML)
                list.forEach {
                    database.insertItem(it)
                }

                viewAdapter = ItemAdapter(list)

                uiThread {
                    conteudoRSS.apply {
                        setHasFixedSize(true)
                        layoutManager = viewManager
                        adapter = viewAdapter
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }


    }

    fun showDialogToAddFeed() {
        alert("Deseja adicionar um feed RSS?", "Ops, nenhum feed adicionado :(") {
            yesButton { openSettingsActivity(null) }
            noButton {}
        }.show()
    }


    @Throws(IOException::class)
    private fun getRssFeed(feed: String): String {
        var `in`: InputStream? = null
        var rssFeed = ""
        try {
            val url = URL(feed)
            val conn = url.openConnection() as HttpURLConnection
            `in` = conn.inputStream
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var count = `in`!!.read(buffer)

            while (count != -1) {
                out.write(buffer, 0, count)

                count = `in`.read(buffer)
            }
            val response = out.toByteArray()
            rssFeed = String(response, Charset.forName("UTF-8"))
        } finally {
            `in`?.close()
        }
        return rssFeed
    }
}
