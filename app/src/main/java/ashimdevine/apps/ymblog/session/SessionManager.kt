package ashimdevine.apps.ymblog.session

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ashimdevine.apps.ymblog.models.AuthToken
import ashimdevine.apps.ymblog.persistence.AuthTokenDao
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val application: Application
) {

    private val TAG: String = "AppDebug"

    private val _cachedToken = MutableLiveData<AuthToken>()

    val cachedToken: LiveData<AuthToken>
        get() = _cachedToken

    fun login(newValue: AuthToken){
        setValue(newValue)
    }

    fun logout(){
        Log.d(TAG, "logout: ")


        CoroutineScope(IO).launch{
            var errorMessage: String? = null
            try{
                _cachedToken.value!!.account_pk?.let { authTokenDao.nullifyToken(it)
                } ?: throw CancellationException("Token Error. Logging out user.")
            }catch (e: CancellationException) {
                Log.e(TAG, "logout: ${e.message}")
                errorMessage = e.message
            }
            catch (e: Exception) {
                Log.e(TAG, "logout: ${e.message}")
                errorMessage = errorMessage + "\n" + e.message
            }
            finally {
                errorMessage?.let{
                    Log.e(TAG, "logout: ${errorMessage}" )
                }
                Log.d(TAG, "logout: finally")
                setValue(null)
            }
        }
    }

    fun setValue(newValue: AuthToken?) {
        GlobalScope.launch(Main) {
            if (_cachedToken.value != newValue) {
                _cachedToken.value = newValue
            }
        }
    }

//    fun isConnectedToTheInternet(): Boolean{
//        val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        try{
//            return cm.activeNetworkInfo.isConnected
//        }catch (e: Exception){
//            Log.e(TAG, "isConnectedToTheInternet: ${e.message}")
//        }
//        return false
//    }

//    @SuppressLint("NewApi")
//    fun isConnectedToTheInternet(context: Context):Boolean =
//        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
//            getNetworkCapabilities(activeNetwork)?.run {
//                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
//                        || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
//                        || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
//            } ?: false
//        }

    @RequiresApi(Build.VERSION_CODES.M)
    fun isConnectedToTheInternet(): Boolean {
        val cm = application.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        return capabilities?.hasCapability(NET_CAPABILITY_INTERNET) == true
    }
}