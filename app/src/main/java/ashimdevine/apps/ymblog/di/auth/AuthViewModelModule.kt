package ashimdevine.apps.ymblog.di.auth

import androidx.lifecycle.ViewModel
import ashimdevine.apps.ymblog.di.ViewModelKey
import ashimdevine.apps.ymblog.ui.auth.AuthViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class AuthViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    abstract fun bindAuthViewModel(authViewModel: AuthViewModel): ViewModel

}