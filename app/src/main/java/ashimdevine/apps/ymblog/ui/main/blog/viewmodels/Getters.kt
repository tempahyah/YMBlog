package ashimdevine.apps.ymblog.ui.main.blog.viewmodels

import android.net.Uri
import ashimdevine.apps.ymblog.models.BlogPost

fun BlogViewModel.getPage():Int{
    getCurrentViewStateOrNew().let{
        return it.blogFields.page
    }
}

fun BlogViewModel.getBlogPost(): BlogPost{
    getCurrentViewStateOrNew().let {
        return it.viewBlogFields.blogPost?.let {blogPost->
            return blogPost
        }?:getDummyBlogPost()
    }

}

fun BlogViewModel.getDummyBlogPost(): BlogPost {
    return BlogPost(-1, "", "", "", "", 1, "")
}

fun BlogViewModel.getFilter():String{
    getCurrentViewStateOrNew().let{
        return it.blogFields.filter
    }
}

fun BlogViewModel.getSlug(): String {
    getCurrentViewStateOrNew().let{
        it.viewBlogFields.blogPost?.let {
            return it.slug
        }
    }
    return ""
}

fun BlogViewModel.isAuthorOfBlogPost(): Boolean{
    getCurrentViewStateOrNew().let {
        return it.viewBlogFields.isAuthorOfBlogPost
    }
}

fun BlogViewModel.getOrder():String{
    getCurrentViewStateOrNew().let{
        return it.blogFields.order
    }
}

fun BlogViewModel.getSearchQuery():String{
    getCurrentViewStateOrNew().let{
        return it.blogFields.searchQuery
    }
}

fun BlogViewModel.getIsQueryExhausted():Boolean{
    getCurrentViewStateOrNew().let {
        return it.blogFields.isQueryExhausted
    }
}

fun BlogViewModel.getIsQueryInProgress():Boolean{
    getCurrentViewStateOrNew().let {
        return it.blogFields.isQueryInProgress
    }
}

fun BlogViewModel.getUpdatedBlogUri(): Uri?{
    getCurrentViewStateOrNew().let {
        it.updateBlogFields.updatedImageUri?.let {
            return it
        }
    }
    return null
}

