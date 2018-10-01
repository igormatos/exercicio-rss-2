package br.ufpe.cin.if710.rss

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.itemlista.view.*
import android.support.v4.content.ContextCompat.startActivity
import android.content.Intent
import android.net.Uri
import br.ufpe.cin.if710.rss.db.database


class ItemAdapter(private val list: List<ItemRSS>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
                R.layout.itemlista,
                parent,
                false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(list[position])
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: ItemRSS) {

            with(itemView) {
                item_titulo.text = item.title
                item_data.text = item.pubDate

                setOnClickListener {
                    // Mark as read
                    it.context.database.markRead(item)

                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(item.link)
                    startActivity(it.context, i, null)
                }
            }
        }
    }
}