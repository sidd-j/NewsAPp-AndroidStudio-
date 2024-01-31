// NewsAdapter2.kt
package com.example.newsapp.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.ui.home.NewsArticle
import com.squareup.picasso.Picasso

class NewsAdapter2(private val articles: MutableList<NewsArticle>) : RecyclerView.Adapter<NewsAdapter2.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_slider, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentArticle = articles[position]

        holder.titleTextView.text = currentArticle.title

        if (!currentArticle.imageUrl.isNullOrEmpty()) {
            Picasso.get().load(currentArticle.imageUrl).into(holder.imageView)
            holder.imageView.visibility = View.VISIBLE
        } else {
            holder.imageView.visibility = View.GONE
        }
    }
    override fun getItemCount(): Int {
        return articles.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.articleTitleTextView)
        val imageView: ImageView = itemView.findViewById(R.id.articleImageView)
    }
}
