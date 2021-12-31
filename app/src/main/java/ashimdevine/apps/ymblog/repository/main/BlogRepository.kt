package ashimdevine.apps.ymblog.repository.main

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import ashimdevine.apps.ymblog.api.GenericResponse
import ashimdevine.apps.ymblog.api.GenericResponseWithError
import ashimdevine.apps.ymblog.api.main.YMBlogApiMainService
import ashimdevine.apps.ymblog.api.main.responses.BlogCreateUpdateResponse
import ashimdevine.apps.ymblog.api.main.responses.BlogListSearchResponse
import ashimdevine.apps.ymblog.models.AuthToken
import ashimdevine.apps.ymblog.models.BlogPost
import ashimdevine.apps.ymblog.persistence.BlogPostDao
import ashimdevine.apps.ymblog.persistence.returnOrderedBlogQuery
import ashimdevine.apps.ymblog.repository.JobManager
import ashimdevine.apps.ymblog.repository.NetworkBoundResource
import ashimdevine.apps.ymblog.session.SessionManager
import ashimdevine.apps.ymblog.ui.DataState
import ashimdevine.apps.ymblog.ui.Response
import ashimdevine.apps.ymblog.ui.ResponseType
import ashimdevine.apps.ymblog.ui.main.blog.state.BlogViewState
import ashimdevine.apps.ymblog.ui.main.blog.state.BlogViewState.*
import ashimdevine.apps.ymblog.util.AbsentLiveData
import ashimdevine.apps.ymblog.util.ApiSuccessResponse
import ashimdevine.apps.ymblog.util.Constants.Companion.PAGINATION_PAGE_SIZE
import ashimdevine.apps.ymblog.util.DateUtils
import ashimdevine.apps.ymblog.util.ErrorHandling.Companion.ERROR_UNKNOWN
import ashimdevine.apps.ymblog.util.GenericApiResponse
import ashimdevine.apps.ymblog.util.SuccessHandling.Companion.RESPONSE_HAS_PERMISSION_TO_EDIT
import ashimdevine.apps.ymblog.util.SuccessHandling.Companion.RESPONSE_NO_PERMISSION_TO_EDIT
import ashimdevine.apps.ymblog.util.SuccessHandling.Companion.SUCCESS_BLOG_DELETED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class BlogRepository
@Inject
constructor(
    val ymBlogApiMainService: YMBlogApiMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
):JobManager("BlogRepository")
{
    private val TAG = "AppDebug"

    @RequiresApi(Build.VERSION_CODES.M)
    fun searchBlogPosts(
        authToken: AuthToken,
        query:String,
        filterAndOrder:String,
        page:Int
    ):LiveData<DataState<BlogViewState>>{
        return object: NetworkBoundResource<BlogListSearchResponse, List<BlogPost>, BlogViewState>(
            sessionManager.isConnectedToTheInternet(), 
            true,
            false,
            true
        ){
            override suspend fun createCacheRequestAndReturn() {
                withContext(Main){
                    //finish by viewing db cache
                    result.addSource(loadFromCache()){viewState->
                        viewState.blogFields.isQueryInProgress = false
                        if(page * PAGINATION_PAGE_SIZE > viewState.blogFields.blogList.size){
                            viewState.blogFields.isQueryExhausted = true
                        }
                        onCompleteJob(DataState.data(viewState, null))
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogListSearchResponse>) {
                val blogPostList: ArrayList<BlogPost> = ArrayList()
                for(blogPostResponse in response.body.results){
                    blogPostList.add(
                        BlogPost(
                            pk = blogPostResponse.pk,
                            title = blogPostResponse.title,
                            slug = blogPostResponse.slug,
                            body = blogPostResponse.body,
                            image = blogPostResponse.image,
                            date_updated = DateUtils.convertServerStringDateToLong(
                                blogPostResponse.date_updated
                            ),
                            username = blogPostResponse.username
                        )
                    )

                }
                updateLocalDb(blogPostList)
                createCacheRequestAndReturn()
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogListSearchResponse>> {
                return ymBlogApiMainService.searchListBlogPosts(
                    "Token ${authToken.token!!}",
                    query=query,
                    ordering=filterAndOrder,
                    page = page
                )
            }

            override fun loadFromCache(): LiveData<BlogViewState> {
                return blogPostDao.returnOrderedBlogQuery(
                    query=query,
                    filterAndOrder=filterAndOrder,
                    page=page
                )
                    .switchMap {
                        object:LiveData<BlogViewState>(){
                            override fun onActive() {
                                super.onActive()
                                value= BlogViewState(
                                    BlogFields(
                                        blogList = it,
                                        isQueryInProgress = true
                                    )
                                )
                            }
                        }
                    }

            }

            override suspend fun updateLocalDb(cacheObject: List<BlogPost>?) {
                if(cacheObject != null){
                    withContext(IO){
                        for(blogPost in cacheObject){
                            try{
                                //launch each as a separate job to be executed in parallel
                                launch {
                                    Log.d(TAG, "updateLocalDb: inserting blog: ${blogPost}")
                                    blogPostDao.insert(blogPost)
                                }
                                
                            }catch(e: Exception){
                                Log.e(TAG, "updateLocalDb: error updating cache on blog post with slug: ${blogPost.slug}" )
                            }
                        }
                    }
                }
            }

            override fun setJob(job: Job) {
                addJob("searchBlogPosts", job)
            }

        }.asLiveData()
    }
    @RequiresApi(Build.VERSION_CODES.M)
    fun isAuthorOfBlogPost(
        authToken: AuthToken,
        slug: String
    ): LiveData<DataState<BlogViewState>> {
        return object: NetworkBoundResource<GenericResponse, Any, BlogViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ){


            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                withContext(Main){

                    Log.d(TAG, "handleApiSuccessResponse: ${response.body.response}")
                    if(response.body.response.equals(RESPONSE_NO_PERMISSION_TO_EDIT)){
                        onCompleteJob(
                            DataState.data(
                                data = BlogViewState(
                                    viewBlogFields = ViewBlogFields(
                                        isAuthorOfBlogPost = false
                                    )
                                ),
                                response = null
                            )
                        )
                    }
                    else if(response.body.response.equals(RESPONSE_HAS_PERMISSION_TO_EDIT)){
                        onCompleteJob(
                            DataState.data(
                                data = BlogViewState(
                                    viewBlogFields = ViewBlogFields(
                                        isAuthorOfBlogPost = true
                                    )
                                ),
                                response = null
                            )
                        )
                    }
                    else{
                        onErrorReturn(ERROR_UNKNOWN, shouldUseDialog = false, shouldUseToast = false)
                    }
                }
            }

            // not applicable
            override fun loadFromCache(): LiveData<BlogViewState> {
                return AbsentLiveData.create()
            }

            // Make an update and change nothing.
            // If they are not the author it will return: "You don't have permission to edit that."
            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return ymBlogApiMainService.isAuthorOfBlogPost(
                    "Token ${authToken.token!!}",
                    slug
                )
            }

            // not applicable
            override suspend fun updateLocalDb(cacheObject: Any?) {

            }

            override fun setJob(job: Job) {
                addJob("isAuthorOfBlogPost", job)
            }


        }.asLiveData()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun deleteBlogPost(
        authToken: AuthToken,
        blogPost: BlogPost
    ):LiveData<DataState<BlogViewState>>{
        return object: NetworkBoundResource<GenericResponse, BlogPost, BlogViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ){
            //Not Applicable
            override suspend fun createCacheRequestAndReturn() {
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                if(response.body.response == SUCCESS_BLOG_DELETED){
                    updateLocalDb(blogPost)
                }
                else{
                    onCompleteJob(
                        DataState.error(
                            Response(
                                ERROR_UNKNOWN,
                                responseType = ResponseType.Dialog()
                            )
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return ymBlogApiMainService.deleteBlogPost(
                    authorization = "Token ${authToken.token}",
                    slug = blogPost.slug
                )
            }

            override fun loadFromCache(): LiveData<BlogViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: BlogPost?) {
                cacheObject?.let { blogPost ->
                    blogPostDao.deleteBlogPost(blogPost)
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(SUCCESS_BLOG_DELETED, ResponseType.Toast())
                        )
                    )
                }
            }

            override fun setJob(job: Job) {
                addJob("deleteBlogPost", job)
            }

        }.asLiveData()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun updateBlogPost(
        authToken: AuthToken,
        slug: String,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?
    ):LiveData<DataState<BlogViewState>>{
        return object: NetworkBoundResource<BlogCreateUpdateResponse, BlogPost, BlogViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ){
            //Not Applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(
                response: ApiSuccessResponse<BlogCreateUpdateResponse>
            ) {
                val updatedBlogPost = BlogPost(
                    response.body.pk,
                    response.body.title,
                    response.body.slug,
                    response.body.body,
                    response.body.image,
                    DateUtils.convertServerStringDateToLong(response.body.date_updated),
                    response.body.username
                )

                updateLocalDb(updatedBlogPost)

                withContext(Dispatchers.Main){
                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            BlogViewState(
                                viewBlogFields = ViewBlogFields(
                                    blogPost = updatedBlogPost
                                )
                            ),
                            Response(response.body.response, ResponseType.Toast())
                        ))
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogCreateUpdateResponse>> {
                return ymBlogApiMainService.updateBlog(
                    "Token ${authToken.token}",
                    slug,
                    title,
                    body,
                    image
                )
            }

            override fun loadFromCache(): LiveData<BlogViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: BlogPost?) {
                cacheObject?.let{blogPost ->
                    blogPostDao.updateBlogPost(
                        blogPost.pk,
                        blogPost.title,
                        blogPost.body,
                        blogPost.image
                    )
                }
            }

            override fun setJob(job: Job) {
                addJob("updateBlogPost", job)
            }

        }.asLiveData()
    }

}