package ashimdevine.apps.ymblog.di.auth

import android.content.SharedPreferences
import ashimdevine.apps.ymblog.api.auth.YMBlogApiAuthService
import ashimdevine.apps.ymblog.persistence.AccountPropertiesDao
import ashimdevine.apps.ymblog.persistence.AuthTokenDao
import ashimdevine.apps.ymblog.repository.auth.AuthRepository
import ashimdevine.apps.ymblog.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.create

@Module
class AuthModule{

    @AuthScope
    @Provides
    fun provideFakeApiService(retrofitBuilder:Retrofit.Builder): YMBlogApiAuthService{
        return retrofitBuilder
            .build()
            .create(YMBlogApiAuthService::class.java)
    }

    @AuthScope
    @Provides
    fun provideAuthRepository(
        sessionManager: SessionManager,
        authTokenDao: AuthTokenDao,
        accountPropertiesDao: AccountPropertiesDao,
        openApiAuthService: YMBlogApiAuthService,
        sharedPreferences: SharedPreferences,
        editor: SharedPreferences.Editor
    ): AuthRepository {
        return AuthRepository(
            authTokenDao,
            accountPropertiesDao,
            openApiAuthService,
            sessionManager,
            sharedPreferences,
            editor
        )
    }

}