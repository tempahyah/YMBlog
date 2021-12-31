package ashimdevine.apps.ymblog.di

import ashimdevine.apps.ymblog.di.auth.AuthFragmentBuildersModule
import ashimdevine.apps.ymblog.di.auth.AuthModule
import ashimdevine.apps.ymblog.di.auth.AuthScope
import ashimdevine.apps.ymblog.di.auth.AuthViewModelModule
import ashimdevine.apps.ymblog.di.main.MainFragmentBuildersModule
import ashimdevine.apps.ymblog.di.main.MainModule
import ashimdevine.apps.ymblog.di.main.MainScope
import ashimdevine.apps.ymblog.di.main.MainViewModelModule
import ashimdevine.apps.ymblog.ui.auth.AuthActivity
import ashimdevine.apps.ymblog.ui.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @AuthScope
    @ContributesAndroidInjector(
        modules = [AuthModule::class, AuthFragmentBuildersModule::class, AuthViewModelModule::class]
    )
    abstract fun contributeAuthActivity(): AuthActivity

    @MainScope
    @ContributesAndroidInjector(
        modules = [MainModule::class, MainFragmentBuildersModule::class, MainViewModelModule::class]
    )
    abstract fun contributeMainActivity(): MainActivity
}