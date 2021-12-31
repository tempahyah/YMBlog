package ashimdevine.apps.ymblog.repository.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import ashimdevine.apps.ymblog.api.GenericResponse
import ashimdevine.apps.ymblog.api.main.YMBlogApiMainService
import ashimdevine.apps.ymblog.models.AccountProperties
import ashimdevine.apps.ymblog.models.AuthToken
import ashimdevine.apps.ymblog.persistence.AccountPropertiesDao
import ashimdevine.apps.ymblog.repository.JobManager
import ashimdevine.apps.ymblog.repository.NetworkBoundResource
import ashimdevine.apps.ymblog.session.SessionManager
import ashimdevine.apps.ymblog.ui.DataState
import ashimdevine.apps.ymblog.ui.Response
import ashimdevine.apps.ymblog.ui.ResponseType
import ashimdevine.apps.ymblog.ui.main.account.state.AccountViewState
import ashimdevine.apps.ymblog.util.AbsentLiveData
import ashimdevine.apps.ymblog.util.ApiSuccessResponse
import ashimdevine.apps.ymblog.util.GenericApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AccountRepository
@Inject
constructor(
    val ymBlogApiMainService: YMBlogApiMainService,
    val accountPropertiesDao: AccountPropertiesDao,
    val sessionManager: SessionManager
): JobManager("AccountRepository")
{
    private val TAG: String = "AppDebug"
    private var repositoryJob: Job? = null

    @RequiresApi(Build.VERSION_CODES.M)
    fun getAccountProperties(authToken: AuthToken): LiveData<DataState<AccountViewState>> {
        return object: NetworkBoundResource<AccountProperties, AccountProperties, AccountViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            false,
            true
        ){

            // if network is down, view the cache and return
            override suspend fun createCacheRequestAndReturn() {
                withContext(Dispatchers.Main){

                    // finishing by viewing db cache
                    result.addSource(loadFromCache()){ viewState ->
                        onCompleteJob(DataState.data(viewState, null))
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<AccountProperties>) {
                updateLocalDb(response.body)

                createCacheRequestAndReturn()
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                return accountPropertiesDao.searchByPk(authToken.account_pk!!)
                    .switchMap {
                        object: LiveData<AccountViewState>(){
                            override fun onActive() {
                                super.onActive()
                                value = AccountViewState(it)
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cacheObject: AccountProperties?) {
                cacheObject?.let {
                    accountPropertiesDao.updateAccountProperties(
                        cacheObject.pk,
                        cacheObject.email,
                        cacheObject.username
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<AccountProperties>> {
                return ymBlogApiMainService
                    .getAccountProperties(
                        "Token ${authToken.token!!}"
                    )
            }


            override fun setJob(job: Job) {
                addJob("getAccountProperties", job)


            }


        }.asLiveData()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun saveAccountProperties(authToken: AuthToken, accountProperties: AccountProperties): LiveData<DataState<AccountViewState>> {
        return object: NetworkBoundResource<GenericResponse, Any, AccountViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ){

            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                updateLocalDb(null) // The update does not return a CacheObject

                withContext(Dispatchers.Main){
                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(response.body.response, ResponseType.Toast())
                        ))
                }
            }

            // not used in this case
            override fun loadFromCache(): LiveData<AccountViewState> {
                return AbsentLiveData.create()
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return ymBlogApiMainService.saveAccountProperties(
                    "Token ${authToken.token!!}",
                    accountProperties.email,
                    accountProperties.username
                )
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                return accountPropertiesDao.updateAccountProperties(
                    accountProperties.pk,
                    accountProperties.email,
                    accountProperties.username
                )
            }

            override fun setJob(job: Job) {
                addJob("saveAccountProperties", job)
            }

        }.asLiveData()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun updatePassword(authToken: AuthToken, currentPassword: String, newPassword: String, confirmNewPassword: String): LiveData<DataState<AccountViewState>> {
        return object: NetworkBoundResource<GenericResponse, Any, AccountViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ){

            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                withContext(Dispatchers.Main){
                    // finish with success response
                    onCompleteJob(
                        DataState.data(null,
                            Response(response.body.response, ResponseType.Toast())
                        ))
                }
            }

            // not used in this case
            override fun loadFromCache(): LiveData<AccountViewState> {
                return AbsentLiveData.create()
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return ymBlogApiMainService.updatePassword(
                    "Token ${authToken.token!!}",
                    currentPassword,
                    newPassword,
                    confirmNewPassword
                )
            }

            // not used in this case
            override suspend fun updateLocalDb(cacheObject: Any?) {

            }

            override fun setJob(job: Job) {
                addJob("updatePassword", job)

            }

        }.asLiveData()
    }

}
