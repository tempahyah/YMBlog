package ashimdevine.apps.ymblog.util

import android.app.Activity
import android.content.Context
import androidx.annotation.IdRes
import androidx.annotation.NavigationRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import ashimdevine.apps.ymblog.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNavController(
    val context: Context,
    @IdRes val containerId:  Int,
    @IdRes val appStartDestinationId: Int,
    val graphChangeListener: OnNavigationGraphChanged?,
    val navGraphProvider: NavGraphProvider,
)
{
    private  val TAG: String = "AppDebug"
    lateinit var activity:Activity
    lateinit var fragmentManager: FragmentManager
    lateinit var navItemChangedListener: OnNavigationItemChanged
    private val navigationBackStack:BackStack = BackStack.of(appStartDestinationId)

    init {
        if (context is Activity){
            activity = context
            fragmentManager = (activity as FragmentActivity).supportFragmentManager
        }
    }

    fun onNavigationItemSelected(itemId: Int = navigationBackStack.last()): Boolean{
        val fragment = fragmentManager.findFragmentByTag(itemId.toString())
            ?: NavHostFragment.create(navGraphProvider.getNavGraphId(itemId))
        fragmentManager.beginTransaction().setCustomAnimations(
            R.anim.fade_in,
            R.anim.fade_out,
            R.anim.fade_in,
            R.anim.fade_out

        )
            .replace(containerId, fragment, itemId.toString())
            .addToBackStack(null)
            .commit()

        // Add to Backstack
        navigationBackStack.moveLast(itemId)

        //Updated checked Item
        navItemChangedListener.onItemChanged(itemId)

        //Communicate with Activity
        graphChangeListener?.onGraphChange()

        return true
    }

    fun onBackPressed(){
        val childFragmentManager = fragmentManager.findFragmentById(containerId)!!
            .childFragmentManager
        when{
            childFragmentManager.popBackStackImmediate()->{

            }

            //Fragment Backstack is empty so try to go back on the navigation stack
            navigationBackStack.size > 1 ->{
                navigationBackStack.removeLast()

                //Update the container with new fragment
                onNavigationItemSelected()
            }

            //If the stack has only one and it is not the navigation home we should
            //Ensure that the application always leave from startDestination
            navigationBackStack.last() != appStartDestinationId ->{
                navigationBackStack.removeLast()
                navigationBackStack.add(0,appStartDestinationId)
                onNavigationItemSelected()
            }
            else -> activity.finish()
        }
    }

    private class BackStack: ArrayList<Int>(){
        companion object{
            fun of(vararg elements: Int): BackStack{
                val b = BackStack()
                b.addAll(elements.toTypedArray())
                return b
            }
        }

        fun removeLast() = removeAt(size-1)

        fun moveLast(item: Int){
            remove(item)
            add(item)
        }
    }

    interface OnNavigationItemChanged{
        fun onItemChanged(itemId: Int)
    }

    fun setOnItemNavigationChanged(listener: (itemId:Int)-> Unit){
        this.navItemChangedListener = object : OnNavigationItemChanged {
            override fun onItemChanged(itemId: Int) {
                listener.invoke(itemId)
            }
        }
    }

    interface NavGraphProvider{
        @NavigationRes
        fun getNavGraphId(itemId: Int): Int
    }

    interface OnNavigationGraphChanged{
        fun onGraphChange()
    }

    interface OnNavigationReselectedListener{
        fun onReselectNavItem(navController: NavController, fragment:Fragment)
    }

}

fun BottomNavigationView.setUpNavigation(
    bottomNavController: BottomNavController,
    onReselectListener: BottomNavController.OnNavigationReselectedListener
){
    setOnNavigationItemSelectedListener {
        bottomNavController.onNavigationItemSelected(it.itemId)
    }

    setOnNavigationItemReselectedListener {
        bottomNavController
            .fragmentManager
            .findFragmentById(bottomNavController.containerId)!!
            .childFragmentManager
            .fragments[0]?.let { fragment ->
                onReselectListener.onReselectNavItem(
                    bottomNavController.activity.findNavController(bottomNavController.containerId),
                    fragment
                )
        }
    }

    bottomNavController.setOnItemNavigationChanged { itemId ->
        menu.findItem(itemId).isChecked = true
    }
}