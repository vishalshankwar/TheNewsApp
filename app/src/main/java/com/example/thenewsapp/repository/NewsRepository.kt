package com.example.thenewsapp.repository

import com.example.thenewsapp.api.RetrofitIntance
import com.example.thenewsapp.dp.ArticleDatabase
import com.example.thenewsapp.models.Article
import com.example.thenewsapp.ui.fragment.SearchFragment
import retrofit2.http.Query

class NewsRepository(val db:ArticleDatabase) {
    suspend fun getHeadLines(countryCode:String,pageNumber: Int)=
        RetrofitIntance.api.getHeadlines(countryCode,pageNumber)
    suspend fun searchNews(searchQuery: String,pageNumber: Int)=
        RetrofitIntance.api.searForNews(searchQuery, pageNumber)
    suspend fun upsert(article: Article)=db.getArticledao().upset(article)
    fun getFaurouiteNews()=db.getArticledao().getAllArticles()
    suspend fun deleteArticle(article: Article)=db.getArticledao().deleteArticle(article)

}