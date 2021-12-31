package ashimdevine.apps.ymblog.di.main

import androidx.lifecycle.ViewModel
import ashimdevine.apps.ymblog.di.ViewModelKey
import ashimdevine.apps.ymblog.ui.main.account.AccountViewModel
import ashimdevine.apps.ymblog.ui.main.blog.viewmodels.BlogViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MainViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(AccountViewModel::class)
    abstract fun bindAccountViewModel(accountViewModel: AccountViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BlogViewModel::class)
    abstract fun bindBlogViewModel(blogViewModel: BlogViewModel): ViewModel

}