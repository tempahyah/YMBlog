package ashimdevine.apps.ymblog.di

import androidx.lifecycle.ViewModelProvider
import ashimdevine.apps.ymblog.viewmodels.ViewModelProviderFactory
import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelFactoryModule {

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelProviderFactory): ViewModelProvider.Factory
}