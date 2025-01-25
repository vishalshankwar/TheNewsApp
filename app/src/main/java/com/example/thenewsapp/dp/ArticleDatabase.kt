package com.example.thenewsapp.dp

import android.content.Context
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.thenewsapp.models.Article
import kotlinx.coroutines.internal.synchronized

@Database(
    entities =[Article::class],
    version = 1

)
@TypeConverters(Converters::class)
 abstract class ArticleDatabase :RoomDatabase(){
     abstract fun getArticledao():ArticleDao
     companion object{
         @Volatile
         private var instance:ArticleDatabase?=null
         private val LOCK=Any()

         operator fun invoke(context:Context)= instance?: kotlin.synchronized(LOCK){
             instance?:createDatabase(context).also {
                 instance=it
             }
         }

         private fun createDatabase(context: Context)=
             Room.databaseBuilder(
                 context.applicationContext,
                 ArticleDatabase::class.java,
                 name = "Article_db.db"
             ).build()
     }
}