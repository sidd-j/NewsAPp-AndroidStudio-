package com.example.newsapp.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.newsapp.AutomobileNews
import com.example.newsapp.FashionNews
import com.example.newsapp.Politics
import com.example.newsapp.R
import com.example.newsapp.SportsNews
import com.example.newsapp.TechnologyNews
import com.example.newsapp.TravelNews
import com.example.newsapp.databinding.FragmentCategoriesBinding
import com.example.newsapp.ui.home.Multimedia
import com.example.newsapp.ui.home.NewsArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.util.Timer
import java.util.TimerTask


class NotificationsFragment : Fragment() {
    private val articlesList = mutableListOf<NewsArticle>()
    private lateinit var binding: FragmentCategoriesBinding
    private lateinit var adapter: NewsAdapter2
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var timer: Timer


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val frameLayout = view.findViewById<FrameLayout>(R.id.framelayout4)

        frameLayout.setOnClickListener {
            val intent = Intent(requireContext(), TechnologyNews::class.java)
            startActivity(intent)
        }

        val frameLayoutpoli = view.findViewById<FrameLayout>(R.id.framelayout2)

        frameLayoutpoli.setOnClickListener {
            val intent = Intent(requireContext(), Politics::class.java)
            startActivity(intent)
        }

        val frameLayouttravel = view.findViewById<FrameLayout>(R.id.framelayout3)

        frameLayouttravel.setOnClickListener {
            val intent = Intent(requireContext(), TravelNews::class.java)
            startActivity(intent)
        }

        val frameLayoutsports = view.findViewById<FrameLayout>(R.id.framelayout1)

        frameLayoutsports.setOnClickListener {
            val intent = Intent(requireContext(), SportsNews::class.java)
            startActivity(intent)
        }

        val frameLayoutautomobile = view.findViewById<FrameLayout>(R.id.framelayout5)

        frameLayoutautomobile.setOnClickListener {
            val intent = Intent(requireContext(), AutomobileNews::class.java)
            startActivity(intent)
        }

        val frameLayoutfashion = view.findViewById<FrameLayout>(R.id.framelayout6)

        frameLayoutfashion.setOnClickListener {
            val intent = Intent(requireContext(), FashionNews::class.java)
            startActivity(intent)
        }
        startAutoScroll()

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            // Trigger refresh action when user swipes down
            refreshNews()


        }


        adapter = NewsAdapter2(articlesList)
        binding.horizontalRecyclerView.adapter = adapter
        binding.horizontalRecyclerView.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val apiKey = "rZcS3xlAHpqx9P85OIrT9vvh08YLFQGb"
                val response = fetchNewsArticles(apiKey)
                handleNewsApiResponse(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun fetchNewsArticles(apiKey: String): String {
        val url = URL("https://api.nytimes.com/svc/topstories/v2/technology.json?api-key=$apiKey")
        return withContext(Dispatchers.IO) {
            try {
                url.readText()
            } catch (e: IOException) {
                e.printStackTrace()
                ""
            }
        }
    }


    private fun refreshNews() {
        loadNews()
    }

    private fun handleNewsApiResponse(response: String) {
        val jsonObject = JSONObject(response)
        val resultsArray = jsonObject.optJSONArray("results")

        if (resultsArray != null) {
            for (i in 0 until resultsArray.length()) {
                val articleObject = resultsArray.getJSONObject(i)
                val title = articleObject.optString("title", "")
                val abstract = articleObject.optString("abstract", "")
                val url = articleObject.optString("url", "")
                val multimediaArray = articleObject.optJSONArray("multimedia")
                val datepublish = articleObject.optString(("published_date"))
                val dateTime = OffsetDateTime.parse(datepublish)
                val hoursAgo = Duration.between(dateTime.toInstant(), Instant.now()).toHours()
                val multimediaList = mutableListOf<Multimedia>()
                multimediaArray?.let {
                    for (j in 0 until it.length()) {
                        val multimediaObject = it.getJSONObject(j)
                        val multimediaUrl = multimediaObject.optString("url", "")
                        val format = multimediaObject.optString("format", "")
                        val height = multimediaObject.optInt("height", 0)
                        val width = multimediaObject.optInt("width", 0)
                        multimediaList.add(Multimedia(multimediaUrl, format, height, width))
                    }
                }

                if (title.isNotBlank() && abstract.isNotBlank() && url.isNotBlank()) {
                    // Here, imageUrl should be the first multimedia url if available
                    val imageUrl = multimediaList.firstOrNull()?.url ?: ""
                    articlesList.add(NewsArticle(title, imageUrl, abstract, url, multimediaList,hoursAgo.toString()))
                }
            }

            adapter.notifyDataSetChanged()
        }
    }


    private fun loadNews() {
        // Show the refresh indicator
        swipeRefreshLayout.isRefreshing = true

        // Use lifecycleScope to launch a coroutine for fetching news
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val apiKey = "rZcS3xlAHpqx9P85OIrT9vvh08YLFQGb"
                val response = fetchNewsArticles(apiKey)

                // Clear the existing articles list before adding new ones
                articlesList.clear()

                // Handle the API response to populate the articlesList
                handleNewsApiResponse(response)

                // Delay for demonstration purposes (replace with your actual data loading logic)
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle the exception (e.g., show an error message)
            } finally {
                // Hide the refresh indicator after data loading is complete
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun startAutoScroll() {
        val handler = Handler(Looper.getMainLooper())
        val update = Runnable {
            val layoutManager = binding.horizontalRecyclerView.layoutManager as LinearLayoutManager
            val maxScroll = layoutManager.itemCount
            var currentPosition = layoutManager.findFirstVisibleItemPosition()

            if (currentPosition == RecyclerView.NO_POSITION) {
                currentPosition = 0
            } else if (currentPosition < maxScroll - 1) {
                currentPosition++
            } else {
                currentPosition = 0
            }

            binding.horizontalRecyclerView.smoothScrollToPosition(currentPosition)
        }

        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                handler.post(update)
            }
        }, 10000, 3000) // Delay 3 seconds, repeat every 3 seconds
    }

    private fun stopAutoScroll() {
        timer.cancel()
    }

}
