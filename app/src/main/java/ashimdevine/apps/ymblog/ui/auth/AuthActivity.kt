package ashimdevine.apps.ymblog.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import ashimdevine.apps.ymblog.R
import ashimdevine.apps.ymblog.ui.BaseActivity
import ashimdevine.apps.ymblog.ui.ResponseType
import ashimdevine.apps.ymblog.ui.ResponseType.*
import ashimdevine.apps.ymblog.ui.auth.state.AuthStateEvent
import ashimdevine.apps.ymblog.ui.main.MainActivity
import ashimdevine.apps.ymblog.viewmodels.ViewModelProviderFactory
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_auth.*
import javax.inject.Inject

class AuthActivity : BaseActivity(),
    NavController.OnDestinationChangedListener
{
    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        viewModel.cancelActiveJobs()
    }


    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        viewModel = ViewModelProvider(this, providerFactory).get(AuthViewModel::class.java)
        findNavController(R.id.auth_nav_host_fragment).addOnDestinationChangedListener(this)

        subscribeObservers()

    }

    override fun onResume() {
        super.onResume()
        checkPreviousAuthUser()
    }

    override fun expandAppBar() {
        findViewById<AppBarLayout>(R.id.app_bar).setExpanded(true)
    }

    private fun subscribeObservers(){

        viewModel.dataState.observe(this, Observer { dataState ->
            onDataStateChange(dataState)
            dataState.data?.let { data ->
                data.data?.let { event ->
                    event.getContentIfNotHandled()?.let {
                        it.authToken?.let {
                            Log.d(TAG, "AuthActivity, DataState: ${it}")
                            viewModel.setAuthToken(it)
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(this, Observer{
            Log.d(TAG, "AuthActivity, subscribeObservers: AuthViewState: ${it}")
            it.authToken?.let{
                sessionManager.login(it)
            }
        })

        sessionManager.cachedToken.observe(this, Observer{ dataState ->
            Log.d(TAG, "AuthActivity, subscribeObservers: AuthDataState: ${dataState}")
            dataState.let{ authToken ->
                if(authToken != null && authToken.account_pk != -1 && authToken.token != null){
                    navMainActivity()
                }
            }
        })
    }

    fun checkPreviousAuthUser(){
        viewModel.setStateEvent(AuthStateEvent.CheckPreviousAuthEvent())
    }

    fun navMainActivity(){
        Log.d(TAG, "navMainActivity: called.")
        val intent = Intent(this, MainActivity::class.java)
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
}

