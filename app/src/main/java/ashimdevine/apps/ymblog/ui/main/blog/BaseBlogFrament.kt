package ashimdevine.apps.ymblog.ui.main.blog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import ashimdevine.apps.ymblog.R
import ashimdevine.apps.ymblog.ui.DataStateChangeListener
import ashimdevine.apps.ymblog.ui.UICommunicationListener
import ashimdevine.apps.ymblog.ui.main.blog.viewmodels.BlogViewModel
import ashimdevine.apps.ymblog.viewmodels.ViewModelProviderFactory
import com.bumptech.glide.RequestManager
import dagger.android.support.DaggerFragment
import javax.inject.Inject

abstract class BaseBlogFragment : DaggerFragment(){

    val TAG: String = "AppDebug"

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var requestManager: RequestManager

    lateinit var viewModel: BlogViewModel

    lateinit var stateChangeListener: DataStateChangeListener

    lateinit var uiCommunicationListener: UICommunicationListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpActionBarWithNavController(R.id.blogFragment, activity as AppCompatActivity)
        viewModel = activity?.run{
            ViewModelProvider(this, providerFactory).get(BlogViewModel::class.java)
        }?:throw Exception("Invalid Activity")

        cancelActiveJobs()
    }
    fun setUpActionBarWithNavController(fragmentId:Int, activity: AppCompatActivity){
        val appBarConfiguration = AppBarConfiguration(setOf(fragmentId))
        NavigationUI.setupActionBarWithNavController(
            activity,
            findNavController(),
            appBarConfiguration
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            stateChangeListener = context as DataStateChangeListener
        }catch(e: ClassCastException){
            Log.e(TAG, "$context must implement DataStateChangeListener" )
        }

        try{
            uiCommunicationListener = context as UICommunicationListener
        }catch(e: ClassCastException){
            Log.e(TAG, "$context must implement UICommunicationListener" )
        }
    }

    fun cancelActiveJobs(){
        viewModel.cancelActiveJobs()
    }
}