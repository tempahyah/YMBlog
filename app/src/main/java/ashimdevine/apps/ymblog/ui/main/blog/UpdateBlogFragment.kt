package ashimdevine.apps.ymblog.ui.main.blog

import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import ashimdevine.apps.ymblog.R
import ashimdevine.apps.ymblog.ui.main.blog.state.BlogStateEvent
import ashimdevine.apps.ymblog.ui.main.blog.viewmodels.onBlogPostUpdateSuccess
import ashimdevine.apps.ymblog.ui.main.blog.viewmodels.setUpdatedBlogFields
import kotlinx.android.synthetic.main.fragment_update_blog.*
import kotlinx.android.synthetic.main.fragment_update_blog.blog_image
import okhttp3.MultipartBody

class UpdateBlogFragment : BaseBlogFragment(){


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
    }
    
    private fun subscribeObservers(){
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState->
            stateChangeListener.onDataStateChange(dataState)
            dataState.data?.let { data->
                data.data?.getContentIfNotHandled()?.let {viewState-> 
                    
                    viewState.viewBlogFields.blogPost?.let{blogPost->
                        viewModel.onBlogPostUpdateSuccess(blogPost).let {
                            findNavController().popBackStack()
                        }
                    }
                    
                    
                }
            }
            
            viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState->
                viewState.updateBlogFields.let { updatedBlogFields->
                    setBlogProperties(
                        updatedBlogFields.updatedBlogTitle,
                        updatedBlogFields.updatedBlogBody,
                        updatedBlogFields.updatedImageUri
                    )
                }
            })
        })
    }

    private fun setBlogProperties(
        updatedBlogTitle: String?,
        updatedBlogBody: String?,
        updatedImageUri: Uri?) {
        requestManager
            .load(updatedImageUri)
            .into(blog_image)

        blog_title.setText(updatedBlogTitle)
        blog_body.setText(updatedBlogBody)

    }

    private fun saveChanges() {
        var multipartBody:MultipartBody.Part? = null
        viewModel.setStateEvent(
            BlogStateEvent.UpdatedBlogPostEvent(
                blog_title.text.toString(),
                blog_body.text.toString(),
                multipartBody
            )
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save->{
                saveChanges()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        viewModel.setUpdatedBlogFields(
            uri = null,
            title = blog_title.text.toString(),
            body = blog_body.text.toString()
        )
    }


}