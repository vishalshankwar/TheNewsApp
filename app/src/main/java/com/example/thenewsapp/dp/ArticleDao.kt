package com.example.thenewsapp.dp

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.thenewsapp.models.Article

@Dao
interface ArticleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upset(article:Article):Long
    @Query("SELECT*FROM Articles")
    fun getAllArticles():LiveData<List<Article>>
    @Delete
    suspend fun deleteArticle(article:Article)
}