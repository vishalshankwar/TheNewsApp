package com.example.thenewsapp.ui.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.widget.AbsListView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thenewsapp.R
import com.example.thenewsapp.adapters.NewsAdaptor
import com.example.thenewsapp.databinding.FragmentHeadlinesBinding
import com.example.thenewsapp.ui.NewsActivity
import com.example.thenewsapp.ui.NewsViewModel
import com.example.thenewsapp.util.Constants
import com.example.thenewsapp.util.Resource


class HeadlinesFragment : Fragment(R.layout.fragment_headlines) {
    lateinit var newsViewModel: NewsViewModel
    lateinit var newsAdaptor: NewsAdaptor
    lateinit var retryButton:Button
    lateinit var errorText: TextView
    lateinit var itemHeadlineError:CardView
    lateinit var binding: FragmentHeadlinesBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding=FragmentHeadlinesBinding.bind(view)

        itemHeadlineError=view.findViewById(R.id.itemHeadlinesError)
        val inflater=requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE )as LayoutInflater
        val view:View=inflater.inflate(R.layout.item_error,null)
        retryButton=view.findViewById(R.id.retryButton)
        errorText=view.findViewById(R.id.errorText)
        newsViewModel=(activity as NewsActivity).newsViewModel
        setUpHeadLinesRecycler()
        newsAdaptor.setOnItemClickListener {
            val bundle=Bundle().apply {
                putSerializable("article",it)
            }
            findNavController().navigate(R.id.action_headlinesFragment_to_articleFragment3,bundle)
        }
        newsViewModel.headlines.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let { newsResponse ->
                        newsAdaptor.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / Constants.Query_PAGE_SIZE + 2
                        isLastPage = newsViewModel.headlinespage == totalPages
                        if (isLastPage) {
                            binding.recyclerHeadlines.setPadding(0, 0, 0, 0)
                        }
                    }
                }
                is Resource.Error<*> -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(activity,"Sorry Error:$message",Toast.LENGTH_LONG).show()
                        showErrorMeassage(message)
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })
        retryButton.setOnClickListener(){
            newsViewModel.getHeadlines("us")
        }


    }
    var isError=false
    var isLoading=false
    var isLastPage=false
    var isScrolling=false

    private fun hideProgressBar(){
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading=false
    }
    private fun showProgressBar(){
        binding.paginationProgressBar.visibility=View.VISIBLE
        isLoading=true
    }
    private fun hideErrorMessage(){
        itemHeadlineError.visibility=View.INVISIBLE
        isError=false
    }
    private fun showErrorMeassage(message: String){
        itemHeadlineError.visibility=View.VISIBLE
        errorText.text=message
        isError=true
    }
    val scrollListner=object :RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCunt = layoutManager.itemCount

            val isNoError = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCunt
            val isNotAtBignning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCunt >= Constants.Query_PAGE_SIZE
            val shouldPaginate =
                isNoError && isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBignning && isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                newsViewModel.getHeadlines("us")
                isScrolling = false
            }

        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }

        }
    }
        private fun setUpHeadLinesRecycler(){
            newsAdaptor = NewsAdaptor()
            binding.recyclerHeadlines.apply {
                adapter=newsAdaptor
                layoutManager=LinearLayoutManager(activity)
                addOnScrollListener(this@HeadlinesFragment.scrollListner)
            }
        }
    }
