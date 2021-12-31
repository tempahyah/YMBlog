package ashimdevine.apps.ymblog.ui.main.account

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import ashimdevine.apps.ymblog.models.AccountProperties
import ashimdevine.apps.ymblog.repository.main.AccountRepository
import ashimdevine.apps.ymblog.session.SessionManager
import ashimdevine.apps.ymblog.ui.BaseViewModel
import ashimdevine.apps.ymblog.ui.DataState
import ashimdevine.apps.ymblog.ui.Loading
import ashimdevine.apps.ymblog.ui.auth.state.AuthStateEvent
import ashimdevine.apps.ymblog.ui.main.account.state.AccountStateEvent
import ashimdevine.apps.ymblog.ui.main.account.state.AccountStateEvent.*
import ashimdevine.apps.ymblog.ui.main.account.state.AccountViewState
import ashimdevine.apps.ymblog.ui.main.blog.state.BlogViewState
import ashimdevine.apps.ymblog.util.AbsentLiveData
import javax.inject.Inject

class AccountViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    val accountRepository: AccountRepository
): BaseViewModel<AccountStateEvent, AccountViewState>()
{
    override fun initViewState(): AccountViewState {
        return AccountViewState()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun handleStateEvent(stateEvent: AccountStateEvent): LiveData<DataState<AccountViewState>> {
        when(stateEvent){
            is GetAccountPropertiesEvent ->{
                return  sessionManager.cachedToken.value?.let{authToken ->
                    accountRepository.getAccountProperties(authToken)
                }?: AbsentLiveData.create()
            }

            is UpdateAccountPropertiesEvent ->{
                return sessionManager.cachedToken.value?.let { authToken ->
                    authToken.account_pk?.let { pk ->
                        val newAccountProperties = AccountProperties(
                            pk,
                            stateEvent.email,
                            stateEvent.username
                        )
                        accountRepository.saveAccountProperties(
                            authToken,
                            newAccountProperties
                        )
                    }
                }?: AbsentLiveData.create()
            }
            is ChangePasswordEvent ->{
                return sessionManager.cachedToken.value?.let { authToken ->
                    accountRepository.updatePassword(
                        authToken,
                        stateEvent.currentPassword,
                        stateEvent.newPassword,
                        stateEvent.confirmNewPassword
                    )
                }?: AbsentLiveData.create()
            }

            is None ->{
                return object:LiveData<DataState<AccountViewState>>(){
                    override fun onActive() {
                        super.onActive()
                        value = DataState(
                            null,
                            Loading(false),
                            null
                        )
                    }
                }
            }
        }
    }

    fun setAccountPropertiesData(accountProperties: AccountProperties){
        val update = getCurrentViewStateOrNew()
        if(update.accountProperties == accountProperties){
            return
        }
        update.accountProperties = accountProperties
        setViewState(update)
    }

    fun logout(){
        sessionManager.logout()
    }

    fun cancelActiveJobs(){
        handlePendingData()
        accountRepository.cancelActiveJobs()
    }

    fun handlePendingData(){
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }


}