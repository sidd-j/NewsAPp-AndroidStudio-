package com.example.newsapp.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.newsapp.Politics
import com.example.newsapp.R
import com.example.newsapp.ScienceNews
import com.example.newsapp.WorldNews
import com.example.newsapp.databinding.FragmentDashboardBinding
import com.example.newsapp.ui.notifications.AdapterHorizontal
import com.example.newsapp.ui.notifications.NewsArticle
import com.example.newsapp.ui.notifications.Multimedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.time.*
import java.util.Timer
import java.util.TimerTask

class DashboardFragment : Fragment() {
    private val politicsArticlesList = mutableListOf<NewsArticle>()
    private val scienceArticlesList = mutableListOf<NewsArticle>()
    private val sportsArticlesList = mutableListOf<NewsArticle>()
    private val mainArticlesList = mutableListOf<NewsArticle>()
    private lateinit var binding: FragmentDashboardBinding
    private lateinit var politicsAdapter: AdapterHorizontal
    private lateinit var scienceAdapter: AdapterHorizontal
    private lateinit var sportsAdapter: AdapterHorizontal
    private lateinit var mainsectionAdapter:AdapterSlider
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var timer: Timer


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startAutoScroll()

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            // Trigger refresh action when user swipes down
            refreshNews()


        }




        politicsAdapter = AdapterHorizontal(requireContext(),politicsArticlesList)
        binding.PoliticsRecylerView.adapter = politicsAdapter
        binding.PoliticsRecylerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        scienceAdapter = AdapterHorizontal(requireContext(),scienceArticlesList)
        binding.ScienceRecylerView.adapter = scienceAdapter
        binding.ScienceRecylerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)



        sportsAdapter = AdapterHorizontal(requireContext(),sportsArticlesList)
        binding.WorldRecylerView.adapter = sportsAdapter
        binding.WorldRecylerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)


        mainsectionAdapter = AdapterSlider(requireContext(),mainArticlesList)
        binding.mainnews.adapter = mainsectionAdapter
        binding.mainnews.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val reView = view.findViewById<RecyclerView>(R.id.mainnews)
        val nextItem = (reView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition() + 1
        reView.smoothScrollToPosition(nextItem)


        val sciencebttn = view.findViewById<Button>(R.id.sciencebttn)

        sciencebttn.setOnClickListener {
            val intent = Intent(requireContext(), ScienceNews::class.java)
            startActivity(intent)
        }

        val polibttn = view.findViewById<Button>(R.id.politicsbttn)

        polibttn.setOnClickListener {
            val intent = Intent(requireContext(), Politics::class.java)
            startActivity(intent)
        }

        val worldbttn = view.findViewById<Button>(R.id.worldbttn)

        worldbttn.setOnClickListener {
            val intent = Intent(requireContext(), WorldNews::class.java)
            startActivity(intent)
        }



        viewLifecycleOwner.lifecycleScope.launch {
            try {

                val apiKey = "rZcS3xlAHpqx9P85OIrT9vvh08YLFQGb"
                val politicsResponse = fetchNewsArticles(apiKey, "politics")
                handleNewsApiResponse(politicsResponse, politicsArticlesList)

                val mainResponse = fetchNewsArticles(apiKey, "home")
                handleNewsApiResponse(mainResponse, mainArticlesList)

                val scienceResponse = fetchNewsArticles(apiKey, "science")
                handleNewsApiResponse(scienceResponse, scienceArticlesList)

                val sportsResponse = fetchNewsArticles(apiKey, "sports")
                handleNewsApiResponse(sportsResponse, sportsArticlesList)

                delay(12000)
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle the exception (e.g., show an error message)
            }
        }
    }

    private fun refreshNews() {
        loadNews()
    }

    private fun loadNews() {
            // Show the refresh indicator
            swipeRefreshLayout.isRefreshing = true

            // Fetch news articles using coroutines
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // Fetch news articles
                    val apiKey = "rZcS3xlAHpqx9P85OIrT9vvh08YLFQGb"
                    val politicsResponse = fetchNewsArticles(apiKey, "politics")
                    handleNewsApiResponse(politicsResponse, politicsArticlesList)

                    val scienceResponse = fetchNewsArticles(apiKey, "science")
                    handleNewsApiResponse(scienceResponse, scienceArticlesList)

                    val mainResponce = fetchNewsArticles(apiKey, "home")
                    handleNewsApiResponse(mainResponce, mainArticlesList)

                    val sportsResponse = fetchNewsArticles(apiKey, "sports")
                    handleNewsApiResponse(sportsResponse, sportsArticlesList)

                    // Delay for demonstration purposes (replace with your actual data loading logic)
                    delay(2000)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Handle the exception (e.g., show an error message)
                } finally {
                    // Hide the refresh indicator after data loading is complete
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }


    private suspend fun fetchNewsArticles(apiKey: String, category: String): String {
        val url = URL("https://api.nytimes.com/svc/topstories/v2/$category.json?api-key=$apiKey")
        return withContext(Dispatchers.IO) {
            try {
                url.readText()
            } catch (e: IOException) {
                e.printStackTrace()
                ""
            }
        }
    }

    private fun handleNewsApiResponse(response: String, articlesList: MutableList<NewsArticle>) {
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
                    articlesList.add(NewsArticle(title, imageUrl, abstract, url, multimediaList, hoursAgo.toString()))
                }
            }

            // Notify the adapter that the data set has changed
            politicsAdapter.notifyDataSetChanged()
            scienceAdapter.notifyDataSetChanged()
            sportsAdapter.notifyDataSetChanged()
            mainsectionAdapter.notifyDataSetChanged()

        }
    }
    private fun startAutoScroll() {
        val handler = Handler(Looper.getMainLooper())
        val update = Runnable {
            val layoutManager = binding.mainnews.layoutManager as LinearLayoutManager
            val maxScroll = layoutManager.itemCount
            var currentPosition = layoutManager.findFirstVisibleItemPosition()

            if (currentPosition == RecyclerView.NO_POSITION) {
                currentPosition = 0
            } else if (currentPosition < maxScroll - 1) {
                currentPosition++
            } else {
                currentPosition = 0
            }

            binding.mainnews.smoothScrollToPosition(currentPosition)
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
