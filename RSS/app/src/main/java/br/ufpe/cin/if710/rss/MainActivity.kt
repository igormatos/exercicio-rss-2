package br.ufpe.cin.if710.rss

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import android.view.Menu
import android.view.MenuItem
import br.ufpe.cin.if710.rss.db.SQLiteRSSHelper
import br.ufpe.cin.if710.rss.db.database
import org.jetbrains.anko.*


class MainActivity : Activity() {

    private lateinit var RSS_FEED: String

    private lateinit var conteudoRSS: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        conteudoRSS = findViewById(R.id.conteudoRSS)

        viewManager = LinearLayoutManager(this)
        preferences = defaultSharedPreferences
        viewAdapter = ItemAdapter(listOf())

        setBroadcastReceiver()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return true
    }

    fun openSettingsActivity(item: MenuItem?) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun refreshList(item: MenuItem?) {
        setAdapter()
        updateRecyclerView()
    }

    override fun onStart() {
        super.onStart()

        RSS_FEED = preferences.getString("rssfeed", "")

        doAsync {
            try {

                if (RSS_FEED.isEmpty()) {
                    uiThread { showDialogToAddFeed() }
                    return@doAsync
                }

                setAdapter()

                uiThread {
                   updateRecyclerView()
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }

    private fun setAdapter() {
        val items = getUnreadItems()
        viewAdapter = ItemAdapter(items)
    }

    private fun getUnreadItems() : List<ItemRSS> {
        return database.getUnreadItems()
    }

    private fun updateRecyclerView() {
        conteudoRSS.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }
    private fun setBroadcastReceiver() {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, it: Intent?) {
                if (it?.action == BROADCAST_UPDATE_FEED) {
                    setAdapter()
                    updateRecyclerView()
                }
            }
        }

        val intentFilter = IntentFilter(BROADCAST_UPDATE_FEED)
        registerReceiver(receiver, intentFilter)
    }

    private fun showDialogToAddFeed() {
        alert("Deseja adicionar um feed RSS?", "Ops, nenhum feed adicionado :(") {
            yesButton { openSettingsActivity(null) }
            noButton {}
        }.show()
    }

}
