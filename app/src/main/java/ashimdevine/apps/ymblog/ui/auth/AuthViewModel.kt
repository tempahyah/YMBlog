package ashimdevine.apps.ymblog.ui.auth

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ashimdevine.apps.ymblog.api.auth.network_responses.LoginResponse
import ashimdevine.apps.ymblog.api.auth.network_responses.RegistrationResponse
import ashimdevine.apps.ymblog.models.AuthToken
import ashimdevine.apps.ymblog.repository.auth.AuthRepository
import ashimdevine.apps.ymblog.ui.BaseViewModel
import ashimdevine.apps.ymblog.ui.DataState
import ashimdevine.apps.ymblog.ui.auth.state.AuthStateEvent
import ashimdevine.apps.ymblog.ui.auth.state.AuthStateEvent.*
import ashimdevine.apps.ymblog.ui.auth.state.AuthViewState
import ashimdevine.apps.ymblog.ui.auth.state.LoginFields
import ashimdevine.apps.ymblog.ui.auth.state.RegistrationFields
import ashimdevine.apps.ymblog.util.AbsentLiveData
import ashimdevine.apps.ymblog.util.GenericApiResponse
import javax.inject.Inject

class AuthViewModel
@Inject
constructor(
    val authRepository: AuthRepository
): BaseViewModel<AuthStateEvent, AuthViewState>() {

    override fun initViewState(): AuthViewState {
        return AuthViewState()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun handleStateEvent(stateEvent: AuthStateEvent): LiveData<DataState<AuthViewState>> {

        when(stateEvent){
            is LoginAttemptEvent->{
                return authRepository.attemptLogin(
                    stateEvent.email,
                    stateEvent.password
                )
            }

            is RegisterAttemptEvent->{
                return authRepository.attemptRegistration(
                    stateEvent.email,
                    stateEvent.username,
                    stateEvent.password,
                    stateEvent.confirm_password
                )
            }

            is CheckPreviousAuthEvent->{
                return authRepository.checkPreviousAuthUser()
            }

            is None->{
                return object: LiveData<DataState<AuthViewState>>(){
                    override fun onActive() {
                        super.onActive()
                        value = DataState.data(null, null)
                    }
                }
            }
        }
    }

    fun setRegistrationFields(registrationFields: RegistrationFields){
        val update = getCurrentViewStateOrNew()
        if(update.registrationFields == registrationFields){
            return
        }
        update.registrationFields = registrationFields
        setViewState(update)
    }

    fun setLoginFields(loginFields: LoginFields){
        val update = getCurrentViewStateOrNew()
        if(update.loginFields == loginFields){
            return
        }
        update.loginFields = loginFields
        setViewState(update)
    }

    fun setAuthToken(authToken: AuthToken){
        val update = getCurrentViewStateOrNew()
        if(update.authToken == authToken){
            return
        }
        update.authToken = authToken
        setViewState(update)
    }

    fun cancelActiveJobs(){
        handlePendingData()
        authRepository.cancelActiveJobs()
    }

    fun handlePendingData(){
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}