package ashimdevine.apps.ymblog.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import ashimdevine.apps.ymblog.models.AccountProperties
import ashimdevine.apps.ymblog.models.AuthToken
import ashimdevine.apps.ymblog.models.BlogPost

@Database (entities = [AuthToken::class, AccountProperties::class, BlogPost::class], version = 2)
abstract class AppDatabase: RoomDatabase() {

    abstract fun getAuthTokenDao(): AuthTokenDao

    abstract fun getAccountPropertiesDao(): AccountPropertiesDao

    abstract fun getBlogPostDao(): BlogPostDao

    companion object{
        const val DATABASE_NAME = "app_db"

    }

}