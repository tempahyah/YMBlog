package ashimdevine.apps.ymblog.repository.auth

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import ashimdevine.apps.ymblog.api.auth.YMBlogApiAuthService
import ashimdevine.apps.ymblog.api.auth.network_responses.LoginResponse
import ashimdevine.apps.ymblog.api.auth.network_responses.RegistrationResponse
import ashimdevine.apps.ymblog.models.AccountProperties
import ashimdevine.apps.ymblog.models.AuthToken
import ashimdevine.apps.ymblog.persistence.AccountPropertiesDao
import ashimdevine.apps.ymblog.persistence.AuthTokenDao
import ashimdevine.apps.ymblog.repository.JobManager
import ashimdevine.apps.ymblog.repository.NetworkBoundResource
import ashimdevine.apps.ymblog.session.SessionManager
import ashimdevine.apps.ymblog.ui.DataState
import ashimdevine.apps.ymblog.ui.Response
import ashimdevine.apps.ymblog.ui.ResponseType
import ashimdevine.apps.ymblog.ui.auth.state.AuthViewState
import ashimdevine.apps.ymblog.ui.auth.state.LoginFields
import ashimdevine.apps.ymblog.ui.auth.state.RegistrationFields
import ashimdevine.apps.ymblog.util.*
import ashimdevine.apps.ymblog.util.ErrorHandling.Companion.ERROR_SAVE_AUTH_TOKEN
import ashimdevine.apps.ymblog.util.ErrorHandling.Companion.ERROR_UNKNOWN
import ashimdevine.apps.ymblog.util.ErrorHandling.Companion.GENERIC_AUTH_ERROR
import ashimdevine.apps.ymblog.util.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import kotlinx.coroutines.Job
import javax.inject.Inject

class AuthRepository
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val ymBlogApiAuthService: YMBlogApiAuthService,
    val sessionManager: SessionManager,
    val sharedPreferences: SharedPreferences,
    val sharedPrefsEditor: SharedPreferences.Editor
): JobManager("AuthRepository")
{

    private val TAG: String = "AppDebug"


    @RequiresApi(Build.VERSION_CODES.M)
    fun attemptLogin(email: String, password: String): LiveData<DataState<AuthViewState>>{

        val loginFieldErrors = LoginFields(email, password).isValidForLogin()
        if(!loginFieldErrors.equals(LoginFields.LoginError.none())){
            return returnErrorResponse(loginFieldErrors, ResponseType.Dialog())
        }

        return object: NetworkBoundResource<LoginResponse, Any, AuthViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ){
            override suspend fun createCacheRequestAndReturn() {
                TODO("Not yet implemented")
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<LoginResponse>) {
                Log.d(TAG, "handleApiSuccessResponse: $response")

                // Incorrect login credentials counts as a 200 response from server, so need to handle that
                if(response.body.response.equals(GENERIC_AUTH_ERROR)){
                    return onErrorReturn(response.body.errorMessage, true, false)
                }
                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(
                        response.body.pk,
                        response.body.email,
                        "",

                    )
                )

                //Will return -1 if failure
                val result= authTokenDao.insert(
                    AuthToken(
                        response.body.pk,
                        response.body.token
                    )
                )

                if(result < 0){
                    return onCompleteJob(
                        DataState.error(
                            Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog())
                        )
                    )
                }


                saveAuthenticatedUserToPrefs(email)

                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.pk, response.body.token)
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<LoginResponse>> {
                return ymBlogApiAuthService.login(email, password)
            }

            override fun setJob(job: Job) {
                addJob("attemptLogin", job)
            }

            //ignore
            override fun loadFromCache(): LiveData<AuthViewState> {
                return AbsentLiveData.create()
            }

            //ignore
            override suspend fun updateLocalDb(cacheObject: Any?) {
            }

        }.asLiveData()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun attemptRegistration(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): LiveData<DataState<AuthViewState>>{

        val registrationFieldErrors = RegistrationFields(email, username, password, confirmPassword).isValidForRegistration()
        if(registrationFieldErrors != RegistrationFields.RegistrationError.none()){
            return returnErrorResponse(registrationFieldErrors, ResponseType.Dialog())
        }

        return object: NetworkBoundResource<RegistrationResponse, Any, AuthViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ){
            override suspend fun createCacheRequestAndReturn() {
                TODO("Not yet implemented")
            }
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<RegistrationResponse>) {

                Log.d(TAG, "handleApiSuccessResponse: $response")

                if(response.body.response == GENERIC_AUTH_ERROR){
                    return onErrorReturn(response.body.errorMessage, true, false)
                }

                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(
                        response.body.pk,
                        response.body.email,
                        "",

                        )
                )

                //Will return -1 if failure
                val result= authTokenDao.insert(
                    AuthToken(
                        response.body.pk,
                        response.body.token
                    )
                )

                if(result < 0){
                    return onCompleteJob(
                        DataState.error(
                            Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog())
                        )
                    )
                }


                saveAuthenticatedUserToPrefs(email)

                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.pk, response.body.token)
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<RegistrationResponse>> {
                return ymBlogApiAuthService.register(email, username, password, confirmPassword)
            }

            override fun setJob(job: Job) {
                addJob("attemptRegistration", job)
            }

            override fun loadFromCache(): LiveData<AuthViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {

            }

        }.asLiveData()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun checkPreviousAuthUser(): LiveData<DataState<AuthViewState>>{
        val previousAuthUserEmail:String? = sharedPreferences.getString(PreferenceKeys.PREVIOUS_AUTH_USER, null)

        if (previousAuthUserEmail.isNullOrBlank()){
            Log.d(TAG, "checkPreviousAuthUser: No Previously Authenticated user found")
            return returnNoTokenFound()
        }
        return object: NetworkBoundResource<Void, Any, AuthViewState>(
            sessionManager.isConnectedToTheInternet(),
            false,
            false,
            false,

        ){
            override suspend fun createCacheRequestAndReturn() {
                accountPropertiesDao.searchByEmail(previousAuthUserEmail).let { accountProperties ->
                    Log.d(TAG, "checkPreviousAuthUser: searching for token $accountProperties")
                    accountProperties?.let {
                        if(accountProperties.pk > -1){
                            authTokenDao.searchByPk(accountProperties.pk).let { authToken ->
                                if(authToken != null){
                                    onCompleteJob(
                                        DataState.data(
                                            data = AuthViewState(
                                                authToken = authToken
                                            )
                                        )
                                    )
                                    return
                                }
                            }
                        }
                    }
                    Log.d(TAG, "checkPreviousAuthUser: AuthToken not found... ")
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(
                                RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                                ResponseType.None()
                            )
                        )
                    )
                }
            }

            //Not used in this case
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<Void>) {

            }

            //Not used in this case
            override fun createCall(): LiveData<GenericApiResponse<Void>> {
                return AbsentLiveData.create()
            }

            override fun setJob(job: Job) {
                addJob("checkPreviousAuthUser", job)
            }

            override fun loadFromCache(): LiveData<AuthViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                TODO("Not yet implemented")
            }

        }.asLiveData()
    }

    private fun returnNoTokenFound(): LiveData<DataState<AuthViewState>> {
        return object: LiveData<DataState<AuthViewState>>(){
            override fun onActive() {
                super.onActive()
                value = DataState.data(
                    data=null,
                    response = Response(RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE, ResponseType.None())
                )
            }
        }
    }

    private fun saveAuthenticatedUserToPrefs(email: String) {
        sharedPrefsEditor.putString(PreferenceKeys.PREVIOUS_AUTH_USER, email)
        sharedPrefsEditor.apply()
    }

    private fun returnErrorResponse(errorMessage: String, responseType: ResponseType): LiveData<DataState<AuthViewState>>{
        Log.d(TAG, "returnErrorResponse: ${errorMessage}")

        return object: LiveData<DataState<AuthViewState>>(){
            override fun onActive() {
                super.onActive()
                value = DataState.error(
                    Response(
                        errorMessage,
                        responseType
                    )
                )
            }
        }
    }




}
