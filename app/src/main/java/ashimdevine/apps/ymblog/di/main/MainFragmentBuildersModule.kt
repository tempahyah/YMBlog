package ashimdevine.apps.ymblog.di.main

import ashimdevine.apps.ymblog.ui.main.account.AccountFragment
import ashimdevine.apps.ymblog.ui.main.account.ChangePasswordFragment
import ashimdevine.apps.ymblog.ui.main.account.UpdateAccountFragment
import ashimdevine.apps.ymblog.ui.main.blog.BlogFragment
import ashimdevine.apps.ymblog.ui.main.blog.UpdateBlogFragment
import ashimdevine.apps.ymblog.ui.main.blog.ViewBlogFragment
import ashimdevine.apps.ymblog.ui.main.create_blog.CreateBlogFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainFragmentBuildersModule {

    @ContributesAndroidInjector()
    abstract fun contributeBlogFragment(): BlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeAccountFragment(): AccountFragment

    @ContributesAndroidInjector()
    abstract fun contributeChangePasswordFragment(): ChangePasswordFragment

    @ContributesAndroidInjector()
    abstract fun contributeCreateBlogFragment(): CreateBlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeUpdateBlogFragment(): UpdateBlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeViewBlogFragment(): ViewBlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeUpdateAccountFragment(): UpdateAccountFragment
}