package ashimdevine.apps.ymblog.ui.auth

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import ashimdevine.apps.ymblog.R
import ashimdevine.apps.ymblog.models.AuthToken
import ashimdevine.apps.ymblog.ui.auth.state.AuthStateEvent
import ashimdevine.apps.ymblog.ui.auth.state.LoginFields
import ashimdevine.apps.ymblog.util.ApiEmptyResponse
import ashimdevine.apps.ymblog.util.ApiErrorResponse
import ashimdevine.apps.ymblog.util.ApiSuccessResponse
import ashimdevine.apps.ymblog.util.GenericApiResponse
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : BaseAuthFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "LoginFragment: ${viewModel}")

        subscribeObservers()

        login_button.setOnClickListener {
            login()
        }
    }

    fun subscribeObservers(){
        viewModel.viewState.observe(viewLifecycleOwner, Observer{
            it.loginFields?.let{
                it.login_email?.let{input_email.setText(it)}
                it.login_password?.let{input_password.setText(it)}
            }
        })
    }

    fun login(){
        viewModel.setStateEvent(
            AuthStateEvent.LoginAttemptEvent(
                input_email.text.toString(),
                input_password.text.toString()
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setLoginFields(
            LoginFields(
                input_email.text.toString(),
                input_password.text.toString()
            )
        )
    }

}

