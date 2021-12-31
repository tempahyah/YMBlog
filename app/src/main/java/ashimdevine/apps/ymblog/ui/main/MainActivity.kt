package ashimdevine.apps.ymblog.ui.main

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import ashimdevine.apps.ymblog.R
import ashimdevine.apps.ymblog.ui.BaseActivity
import ashimdevine.apps.ymblog.ui.auth.AuthActivity
import ashimdevine.apps.ymblog.ui.main.account.BaseAccountFragment
import ashimdevine.apps.ymblog.ui.main.account.ChangePasswordFragment
import ashimdevine.apps.ymblog.ui.main.account.UpdateAccountFragment
import ashimdevine.apps.ymblog.ui.main.blog.BaseBlogFragment
import ashimdevine.apps.ymblog.ui.main.blog.UpdateBlogFragment
import ashimdevine.apps.ymblog.ui.main.blog.ViewBlogFragment
import ashimdevine.apps.ymblog.ui.main.create_blog.BaseCreateBlogFragment
import ashimdevine.apps.ymblog.util.BottomNavController
import ashimdevine.apps.ymblog.util.setUpNavigation
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.progress_bar

class MainActivity : BaseActivity(),
        BottomNavController.NavGraphProvider,
        BottomNavController.OnNavigationGraphChanged,
        BottomNavController.OnNavigationReselectedListener
{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupActionBar()

        bottomNavigationView = findViewById(R.id.bottom_navigation_view)
        bottomNavigationView.setUpNavigation(bottomNavController, this)
        if(savedInstanceState == null){
            bottomNavController.onNavigationItemSelected()
        }


        subscribeObservers()
    }

    private fun subscribeObservers(){
        sessionManager.cachedToken.observe(this, Observer{ authToken ->
            Log.d(TAG, "MainActivity, subscribeObservers: ViewState: ${authToken}")
            if(authToken == null || authToken.account_pk == -1 || authToken.token == null){
                navAuthActivity()
                finish()
            }
        })
    }

    private fun navAuthActivity(){
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun displayProgressBar(bool: Boolean){
        if(bool){
            progress_bar.visibility = View.VISIBLE
        }
        else{
            progress_bar.visibility = View.GONE
        }
    }
    private lateinit var bottomNavigationView: BottomNavigationView
    private val bottomNavController by lazy (LazyThreadSafetyMode.NONE) {
        BottomNavController(
            this,
            R.id.main_nav_host_fragment,
            R.id.nav_blog,
            this,
            this
        )
    }

    private fun setupActionBar(){
        setSupportActionBar(tool_bar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item?.itemId){
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun expandAppBar() {
        findViewById<AppBarLayout>(R.id.app_bar).setExpanded(true)
    }

    override fun onBackPressed() = bottomNavController.onBackPressed()

    override fun getNavGraphId(itemId: Int)= when(itemId) {
        R.id.nav_blog -> {
            R.navigation.nav_blog
        }

        R.id.nav_create_blog ->{
            R.navigation.nav_create_blog
        }

        R.id.nav_account ->{
            R.navigation.nav_account
        }


        else -> {
            R.navigation.nav_blog
        }
    }

    override fun onGraphChange() {
        expandAppBar()
        cancelActiveJobs()
    }

    private fun cancelActiveJobs() {
        val fragments = bottomNavController.fragmentManager
            .findFragmentById(bottomNavController.containerId)
            ?.childFragmentManager
            ?.fragments

        if(fragments != null){

            for( fragment in fragments){
                when(fragment){
                    is BaseAccountFragment -> fragment.cancelActiveJobs()

                    is BaseBlogFragment -> fragment.cancelActiveJobs()

                    is BaseCreateBlogFragment-> fragment.cancelActiveJobs()
                }

            }
        }

        displayProgressBar(false)
    }

    override fun onReselectNavItem(navController: NavController, fragment: Fragment) = when(fragment) {
        is ViewBlogFragment ->{
            navController.navigate(R.id.action_viewBlogFragment_to_blogFragment)
        }

        is UpdateBlogFragment ->{
            navController.navigate(R.id.action_updateBlogFragment_to_blogFragment)
        }

        is UpdateAccountFragment ->{
            navController.navigate(R.id.action_updateAccountFragment_to_accountFragment)
        }

        is ChangePasswordFragment ->{
            navController.navigate(R.id.action_changePasswordFragment_to_accountFragment)
        }
        else ->{
            //do nothing
        }
    }
}