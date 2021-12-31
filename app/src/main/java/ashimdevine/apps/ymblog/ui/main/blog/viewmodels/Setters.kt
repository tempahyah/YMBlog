package ashimdevine.apps.ymblog.ui.main.blog.viewmodels

import android.net.Uri
import ashimdevine.apps.ymblog.models.BlogPost

fun BlogViewModel.setQuery(query: String){
    val update = getCurrentViewStateOrNew()
    update.blogFields.searchQuery = query
    setViewState(update)
}

fun BlogViewModel.setBlogListData(blogList:List<BlogPost>){
    val update = getCurrentViewStateOrNew()

    update.blogFields.blogList = blogList
    setViewState(update)
}

fun BlogViewModel.setBlogPost(blogPost: BlogPost){
    val update = getCurrentViewStateOrNew()
    update.viewBlogFields.blogPost = blogPost
    setViewState(update)
}

fun BlogViewModel.setIsAuthorOfBlogPost(isAuthorOfBlogPost: Boolean){
    val update = getCurrentViewStateOrNew()
    update.viewBlogFields.isAuthorOfBlogPost = isAuthorOfBlogPost
    setViewState(update)
}

fun BlogViewModel.setQueryExhausted(isQueryExhausted:Boolean){
    val update = getCurrentViewStateOrNew()
    update.blogFields.isQueryExhausted = isQueryExhausted
    setViewState(update)
}

fun BlogViewModel.setQueryInProgress(isQueryInProgress:Boolean){
    val update = getCurrentViewStateOrNew()
    update.blogFields.isQueryInProgress = isQueryInProgress
    setViewState(update)
}

fun BlogViewModel.setBlogFilter(filter:String?){
    filter?.let {
        val update = getCurrentViewStateOrNew()
        update.blogFields.filter = filter
        setViewState(update)
    }
}

fun BlogViewModel.setBlogOrder(order:String){
    val update = getCurrentViewStateOrNew()
    update.blogFields.order = order
    setViewState(update)
}

fun BlogViewModel.removeDeletedBlogPost(){
    val update = getCurrentViewStateOrNew()
    val list = update.blogFields.blogList.toMutableList()
    for(i in 0..(list.size - 1)){
        if(list[i] == getBlogPost()){
            list.remove(getBlogPost())
            break
        }
    }
    setBlogListData(list)
}

fun BlogViewModel.setUpdatedBlogFields(
    title: String?,
    body: String?,
    uri: Uri?
){
    val update = getCurrentViewStateOrNew()
    val updateBlogFields = update.updateBlogFields
    title?.let { updateBlogFields.updatedBlogTitle = it }
    body?.let { updateBlogFields.updatedBlogBody = it }
    uri?.let { updateBlogFields.updatedImageUri = it }

    update.updateBlogFields = updateBlogFields
    setViewState(update)

}

fun BlogViewModel.updateListItem(newBlogPost: BlogPost){
    val update = getCurrentViewStateOrNew()
    val list = update.blogFields.blogList.toMutableList()
    for (i in 0..(list.size - 1)){
        if(list[1].pk == newBlogPost.pk){
            list[i] = newBlogPost
            break
        }
    }

    update.blogFields.blogList = list
    setViewState(update)
}

fun BlogViewModel.onBlogPostUpdateSuccess(blogPost: BlogPost){
    setUpdatedBlogFields(
        title = blogPost.title,
        body = blogPost.body,
        uri = null
    )

    setBlogPost(blogPost)
    updateListItem(blogPost)
}
