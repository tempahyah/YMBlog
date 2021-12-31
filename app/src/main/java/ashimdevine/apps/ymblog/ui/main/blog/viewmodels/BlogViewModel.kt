package ashimdevine.apps.ymblog.ui.main.blog.viewmodels

import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import ashimdevine.apps.ymblog.models.BlogPost
import ashimdevine.apps.ymblog.persistence.BlogQueryUtils
import ashimdevine.apps.ymblog.repository.main.BlogRepository
import ashimdevine.apps.ymblog.session.SessionManager
import ashimdevine.apps.ymblog.ui.BaseViewModel
import ashimdevine.apps.ymblog.ui.DataState
import ashimdevine.apps.ymblog.ui.Loading
import ashimdevine.apps.ymblog.ui.main.blog.state.BlogStateEvent
import ashimdevine.apps.ymblog.ui.main.blog.state.BlogStateEvent.*
import ashimdevine.apps.ymblog.ui.main.blog.state.BlogViewState
import ashimdevine.apps.ymblog.util.AbsentLiveData
import ashimdevine.apps.ymblog.util.PreferenceKeys.Companion.BLOG_FILTER
import ashimdevine.apps.ymblog.util.PreferenceKeys.Companion.BLOG_ORDER
import com.bumptech.glide.RequestManager
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject

class BlogViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val blogRepository: BlogRepository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor
): BaseViewModel<BlogStateEvent, BlogViewState>()
{
    init {
        setBlogFilter(
            sharedPreferences.getString(
                BLOG_FILTER,
                BlogQueryUtils.BLOG_FILTER_DATE_UPDATED
            )
        )

        sharedPreferences.getString(
            BLOG_ORDER,
            BlogQueryUtils.BLOG_ORDER_ASC
        )?.let {
            setBlogOrder(
                it
            )
        }
    }

    override fun initViewState(): BlogViewState {
        return BlogViewState()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun handleStateEvent(stateEvent: BlogStateEvent): LiveData<DataState<BlogViewState>> {
        when(stateEvent){
            is BlogSearchEvent ->{
                return sessionManager.cachedToken.value?.let{authToken ->
                    blogRepository.searchBlogPosts(
                        authToken= authToken,
                        query = getSearchQuery(),
                        filterAndOrder = getOrder()+getFilter(),
                        page = getPage(),

                    )
                }?:AbsentLiveData.create()
            }

            is CheckAuthorOfBlogPost -> {
                return sessionManager.cachedToken.value?.let{authToken ->
                    blogRepository.isAuthorOfBlogPost(
                        authToken = authToken,
                        slug = getSlug()
                    )
                }?:AbsentLiveData.create()
            }

            is DeleteBlogPostEvent -> {
                return sessionManager.cachedToken.value?.let{authToken ->
                    blogRepository.deleteBlogPost(
                        authToken = authToken,
                        blogPost = getBlogPost()
                    )
                }?:AbsentLiveData.create()
            }

            is UpdatedBlogPostEvent -> {
                return sessionManager.cachedToken.value?.let{authToken ->
                   val title = RequestBody.create(
                       MediaType.parse("text/plain"),
                       stateEvent.title
                   )

                    val body = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.body
                    )

                    blogRepository.updateBlogPost(
                        authToken= authToken,
                        slug=getSlug(),
                        title=title,
                        body = body,
                        image=stateEvent.image

                    )


                }?:AbsentLiveData.create()
            }

            is None ->{
                return object:LiveData<DataState<BlogViewState>>(){
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

    fun saveFilterOptions(filter:String, order:String){
        editor.putString(BLOG_FILTER, filter)
        editor.apply()

        editor.putString(BLOG_ORDER, order)
        editor.apply()
    }

    fun cancelActiveJobs(){
        blogRepository.cancelActiveJobs()
        handlePendingData()
    }

    private fun handlePendingData(){
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }
}