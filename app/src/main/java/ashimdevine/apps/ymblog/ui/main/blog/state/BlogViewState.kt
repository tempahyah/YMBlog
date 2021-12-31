package ashimdevine.apps.ymblog.ui.main.blog.state

import android.net.Uri
import ashimdevine.apps.ymblog.models.BlogPost
import ashimdevine.apps.ymblog.persistence.BlogQueryUtils.Companion.BLOG_ORDER_ASC
import ashimdevine.apps.ymblog.persistence.BlogQueryUtils.Companion.ORDER_BY_ASC_DATE_UPDATED

data class BlogViewState(
    //BlogFragment Vars
    var blogFields: BlogFields = BlogFields(),

    //ViewBlogFragment
    var viewBlogFields: ViewBlogFields = ViewBlogFields(),

    //UpdateBlogFragment Vars
    var updateBlogFields: UpdateBlogFields = UpdateBlogFields(),
)

{
    data class BlogFields(
        var blogList:List<BlogPost> = ArrayList<BlogPost>(),
        var searchQuery: String = "",
        var page:Int = 1,
        var isQueryInProgress: Boolean = false,
        var isQueryExhausted: Boolean = false,
        var filter: String = ORDER_BY_ASC_DATE_UPDATED,
        var order: String = BLOG_ORDER_ASC //

    )

    data class ViewBlogFields(
        var blogPost: BlogPost? = null,
        var isAuthorOfBlogPost: Boolean = false
    )

    data class UpdateBlogFields(
        var updatedBlogTitle: String? = null,
        var updatedBlogBody: String? = null,
        var updatedImageUri: Uri? = null
    )
}