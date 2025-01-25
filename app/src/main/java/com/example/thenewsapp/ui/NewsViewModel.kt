package com.example.thenewsapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import androidx.room.Query
import com.example.thenewsapp.models.Article
import com.example.thenewsapp.models.NewsResponse
import com.example.thenewsapp.repository.NewsRepository
import com.example.thenewsapp.util.Resource
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.Response

class NewsViewModel(app:Application,val newsRepository: NewsRepository):AndroidViewModel(app) {
    val headlines:MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var headlinespage=1
    var headlinesResponse:NewsResponse?=null
    val searchNews:MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNEwsPage=1
    var searchNewsResponce:NewsResponse?=null
    var newSearchQuery:String?=null
    var oldSearchQuery:String?=null

    init {
        getHeadlines(countryCode = "us")
    }

    fun getHeadlines(countryCode: String)=viewModelScope.launch {
        headlineInternet(countryCode)
    }
    fun searchNews(searchQuery: String)=viewModelScope.launch {
        searchNewsInternet(searchQuery)
    }
    private fun handleHeadlinesResponse(response: Response<NewsResponse>):Resource<NewsResponse>{
        if (response.isSuccessful){
            response.body()?.let { resultResponse->
                headlinespage++
                if (headlinesResponse==null){
                    headlinesResponse=resultResponse
                }else{
                    val oldArticle=headlinesResponse?.articles
                    val newArticle=resultResponse.articles
                    oldArticle?.addAll(newArticle)
                }
                return Resource.Success(headlinesResponse?:resultResponse)
            }
        }
        return Resource.Error(response.message())
    }
    private fun handlsearchNewsResponse(response: Response<NewsResponse>):Resource<NewsResponse>{
        if (response.isSuccessful){
            response.body()?.let { resultResponse->
                if (searchNewsResponce==null||newSearchQuery!=oldSearchQuery){
                    searchNEwsPage=1
                    oldSearchQuery=newSearchQuery
                    searchNewsResponce=resultResponse
                }else{
                    searchNEwsPage++
                    val oldArticle=searchNewsResponce?.articles
                    val newsArticle=resultResponse.articles
                    oldArticle?.addAll(newsArticle)
                }
                return Resource.Success(searchNewsResponce?:resultResponse)
            }
        }
        return Resource.Error(response.message())
    }
    fun addTofrourites(article: Article)=viewModelScope.launch {
        newsRepository.upsert(article)
    }
    fun getFrouritesNEws()=newsRepository.getFaurouiteNews()
    fun deleteArticle(article: Article)=viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }
    fun internetConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.run {
            when {
                hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } ?: false
    }
    private suspend fun headlineInternet(countryCode:String){
        headlines.postValue(Resource.Loading())
        try {
            if (internetConnection(this.getApplication())){
                val response=newsRepository.getHeadLines(countryCode,headlinespage)
                headlines.postValue(handleHeadlinesResponse(response))
            }else{
                headlines.postValue(Resource.Error("No Internet"))
            }
        }catch (t:Throwable){
            when(t){
                is IOException->headlines.postValue(Resource.Error("Unable to Connection"))
                else->headlines.postValue(Resource.Error("No Signal"))

            }
        }
    }
    private suspend fun searchNewsInternet(searchQuery: String){
        newSearchQuery=searchQuery
        searchNews.postValue(Resource.Loading())
        try {
            if (internetConnection(this.getApplication())){
                val response=newsRepository.searchNews(searchQuery,searchNEwsPage)
                searchNews.postValue(handlsearchNewsResponse(response))
            }else{
                searchNews.postValue(Resource.Error("No internet connection"))
            }
        }catch (t:Throwable){
            when(t){
                is IOException->searchNews.postValue(Resource.Error("Unable to connection"))
                else->searchNews.postValue(Resource.Error("No signal"))
            }
        }
    }

}