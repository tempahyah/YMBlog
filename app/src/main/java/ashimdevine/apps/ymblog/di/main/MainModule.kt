package ashimdevine.apps.ymblog.di.main

import ashimdevine.apps.ymblog.api.main.YMBlogApiMainService
import ashimdevine.apps.ymblog.persistence.AccountPropertiesDao
import ashimdevine.apps.ymblog.persistence.AppDatabase
import ashimdevine.apps.ymblog.persistence.BlogPostDao
import ashimdevine.apps.ymblog.repository.main.AccountRepository
import ashimdevine.apps.ymblog.repository.main.BlogRepository
import ashimdevine.apps.ymblog.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.create

@Module
class MainModule {

    @MainScope
    @Provides
    fun provideYMBlogApiMainService(retrofitBuilder: Retrofit.Builder): YMBlogApiMainService{
        return retrofitBuilder
            .build()
            .create(YMBlogApiMainService::class.java)
    }

    @MainScope
    @Provides
    fun provideAccountRepository(
        ymBlogApiMainService: YMBlogApiMainService,
        accountPropertiesDao: AccountPropertiesDao,
        sessionManager: SessionManager
    ):AccountRepository{
        return AccountRepository(
            ymBlogApiMainService,
            accountPropertiesDao,
            sessionManager
        )
    }

    @MainScope
    @Provides
    fun provideBlogPostDao(db:AppDatabase):BlogPostDao{
        return db.getBlogPostDao()
    }

    @MainScope
    @Provides
    fun provideBlogRepository(
        ymBlogApiMainService: YMBlogApiMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): BlogRepository{
        return BlogRepository(ymBlogApiMainService, blogPostDao, sessionManager)
    }




}