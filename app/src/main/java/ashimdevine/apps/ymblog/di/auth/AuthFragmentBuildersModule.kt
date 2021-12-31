package ashimdevine.apps.ymblog.di.auth

import ashimdevine.apps.ymblog.ui.auth.ForgotPasswordFragment
import ashimdevine.apps.ymblog.ui.auth.LauncherFragment
import ashimdevine.apps.ymblog.ui.auth.LoginFragment
import ashimdevine.apps.ymblog.ui.auth.RegisterFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AuthFragmentBuildersModule {

    @ContributesAndroidInjector()
    abstract fun contributeLauncherFragment(): LauncherFragment

    @ContributesAndroidInjector()
    abstract fun contributeLoginFragment(): LoginFragment

    @ContributesAndroidInjector()
    abstract fun contributeRegisterFragment(): RegisterFragment

    @ContributesAndroidInjector()
    abstract fun contributeForgotPasswordFragment(): ForgotPasswordFragment

}