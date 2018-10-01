package br.ufpe.cin.if710.rss

import android.app.IntentService
import android.content.Intent
import br.ufpe.cin.if710.rss.db.database
import org.jetbrains.anko.defaultSharedPreferences
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

const val ACTION_FETCH_NEW_ITEMS = "br.ufpe.cin.if710.rss.action.FOO"

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
class DownloadIntentService : IntentService("DownloadIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_FETCH_NEW_ITEMS -> {
                downloadFeed()
            }
        }
    }

    private fun downloadFeed() {
        val rssfeed = defaultSharedPreferences.getString("rssfeed", "")

        if (rssfeed.isEmpty()) return

        val feedXML = getRssFeed(rssfeed)

        val newItems = ParserRSS.parse(feedXML)
        val oldItems = database.getUnreadItems()

        newItems.sameContentWith(oldItems)?.let {
            if (it) {
                return
            }

            newItems.forEach {
                database.insertItem(it)
            }

            val intent = Intent(BROADCAST_UPDATE_FEED)
            sendBroadcast(intent)
        }

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
