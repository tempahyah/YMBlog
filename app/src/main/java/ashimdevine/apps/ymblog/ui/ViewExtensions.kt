package ashimdevine.apps.ymblog.ui

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import ashimdevine.apps.ymblog.R
import com.afollestad.materialdialogs.MaterialDialog

fun Activity.displayToast(@StringRes message:Int){
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}


fun Activity.displayToast(message:String){
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}


fun Activity.displaySuccessDialog(message: String?){
    MaterialDialog(this)
        .show {
            title(R.string.text_success)
            message(text = message)
            positiveButton(R.string.text_ok)
        }
}

fun Activity.displayErrorDialog(message: String?){
    MaterialDialog(this)
        .show {
            title(R.string.text_error)
            message(text = message)
            positiveButton(R.string.text_ok)
        }
}

fun Activity.displayInfoDialog(message:String?){
    MaterialDialog(this)
        .show {
            title(R.string.text_info)
            message(text = message)
            positiveButton(R.string.text_ok)
        }
}

fun Activity.areYouSureDialog(message:String?, callback:AreYouSureCallback){
    MaterialDialog(this)
        .show {
            title(R.string.are_you_sure)
            message(text = message)
            negativeButton(R.string.text_cancel) {
                callback.cancel()
            }
            positiveButton(R.string.text_ok){
                callback.proceed()
            }
        }
}

interface AreYouSureCallback{
    fun proceed()
    fun cancel()
}