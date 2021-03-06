package ashimdevine.apps.ymblog.ui
import android.annotation.SuppressLint
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.inputmethod.InputMethodManager
import ashimdevine.apps.ymblog.session.SessionManager
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseActivity: DaggerAppCompatActivity(),
    DataStateChangeListener,
        UICommunicationListener
{

    val TAG: String = "AppDebug"

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onUIMessageReceived(uiMessage: UIMessage) {
        when(uiMessage.uiMessageType){
            is UIMessageType.AreYouSureDialog->{
                areYouSureDialog(
                    uiMessage.message,
                    uiMessage.uiMessageType.callback
                )
            }
            
            is UIMessageType.Toast->{
                displayToast(uiMessage.message)
            }
            
            is UIMessageType.Dialog->{
                displayInfoDialog(uiMessage.message)
            }
            
            is UIMessageType.None ->{
                Log.i(TAG, "onUIMessageReceived: ${uiMessage.message}")
            }


        }
    }

    override fun onDataStateChange(dataState: DataState<*>?) {
        dataState?.let{
            GlobalScope.launch(Dispatchers.Main){
                displayProgressBar(it.loading.isLoading)

                it.error?.let { errorEvent ->
                    handleStateError(errorEvent)
                }

                it.data?.let {
                    it.response?.let { responseEvent ->
                        handleStateResponse(responseEvent)
                    }
                }
            }
        }
    }

    abstract fun displayProgressBar(bool: Boolean)

    private fun handleStateResponse(event: Event<Response>){
        event.getContentIfNotHandled()?.let{

            when(it.responseType){
                is ResponseType.Toast ->{
                    it.message?.let{message ->
                        displayToast(message)
                    }
                }

                is ResponseType.Dialog ->{
                    it.message?.let{ message ->
                        displaySuccessDialog(message)
                    }
                }

                is ResponseType.None -> {
                    Log.i(TAG, "handleStateResponse: ${it.message}")
                }
            }

        }
    }

    private fun handleStateError(event: Event<StateError>){
        event.getContentIfNotHandled()?.let{
            when(it.response.responseType){
                is ResponseType.Toast ->{
                    it.response.message?.let{message ->
                        displayToast(message)
                    }
                }

                is ResponseType.Dialog ->{
                    it.response.message?.let{ message ->
                        displayErrorDialog(message)
                    }
                }

                is ResponseType.None -> {
                    Log.i(TAG, "handleStateError: ${it.response.message}")
                }
            }
        }
    }

    @SuppressLint("ServiceCast")
    override fun hideSoftKeyboard() {
        if(currentFocus != null){
            val inputMethodManager = getSystemService(
                Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)


        }

    }

}

