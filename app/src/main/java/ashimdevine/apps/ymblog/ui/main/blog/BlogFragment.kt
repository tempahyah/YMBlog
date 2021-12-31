package ashimdevine.apps.ymblog.ui.main.blog

import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ashimdevine.apps.ymblog.R
import ashimdevine.apps.ymblog.models.BlogPost
import ashimdevine.apps.ymblog.persistence.BlogQueryUtils.Companion.BLOG_FILTER_DATE_UPDATED
import ashimdevine.apps.ymblog.persistence.BlogQueryUtils.Companion.BLOG_FILTER_USERNAME
import ashimdevine.apps.ymblog.persistence.BlogQueryUtils.Companion.BLOG_ORDER_ASC
import ashimdevine.apps.ymblog.ui.DataState
import ashimdevine.apps.ymblog.ui.main.blog.BaseBlogFragment
import ashimdevine.apps.ymblog.ui.main.blog.state.BlogStateEvent
import ashimdevine.apps.ymblog.ui.main.blog.state.BlogViewState
import ashimdevine.apps.ymblog.ui.main.blog.viewmodels.*
import ashimdevine.apps.ymblog.util.ErrorHandling
import ashimdevine.apps.ymblog.util.TopSpacingItemDecoration
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.fragment_blog.*
import javax.inject.Inject

class BlogFragment : BaseBlogFragment(),
    BlogListAdapter.Interaction,
        SwipeRefreshLayout.OnRefreshListener
{



    private lateinit var recyclerAdapter: BlogListAdapter

    private lateinit var searchView:SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        setHasOptionsMenu(true)
        swipe_refresh.setOnRefreshListener(this)
        initRecyclerView()
        subscribeObservers()
        if(savedInstanceState == null){
            viewModel.loadFirstPage()
        }
    }

    private fun onBlogSearchOrFilter(){
        viewModel.loadFirstPage().let {
            resetUI()
        }
    }

    private fun resetUI(){
        blog_post_recyclerview.smoothScrollToPosition(0)
        stateChangeListener.hideSoftKeyboard()
        focusable_view.requestFocus()
    }

    private fun subscribeObservers(){
        viewModel.dataState.observe(viewLifecycleOwner, Observer{ dataState ->
            if(dataState != null){
                handlePagination(dataState)
                stateChangeListener.onDataStateChange(dataState)

            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer{ viewState ->
            Log.d(TAG, "BlogFragment, ViewState: ${viewState}")
            if(viewState != null){
                recyclerAdapter.apply {
                    preloadGlideImages(
                        requestManager,
                        viewState.blogFields.blogList
                    )
                    submitList(
                        blogList = viewState.blogFields.blogList,
                        isQueryExhausted = viewState.blogFields.isQueryExhausted
                    )
                }

            }
        })
    }

    private fun initSearchView(menu: Menu){
        activity?.apply {
            val searchManager:SearchManager = getSystemService(SEARCH_SERVICE) as SearchManager
            searchView = menu.findItem(R.id.action_search).actionView as SearchView
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            searchView.maxWidth = Integer.MAX_VALUE
            searchView.setIconifiedByDefault(true)
            searchView.isSubmitButtonEnabled = true
        }

        //Case 1: ENTER ON COMPUTER KEYBOARD OR ARROW  ON VIRTUAL KEYBOARD
        val searchPlate = searchView.findViewById(R.id.search_src_text) as EditText
        searchPlate.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_SEARCH){
                val searchQuery = v.text.toString()
                viewModel.setQuery(searchQuery).let {
                    onBlogSearchOrFilter()
                }

            }
            true
        }
        //Case 2: SEARCH BUTTON CLICKED
        (searchView.findViewById(R.id.search_go_btn)as View).setOnClickListener{
            val searchQuery = searchPlate.text.toString()
            viewModel.setQuery(searchQuery).let {
                onBlogSearchOrFilter()
            }
        }
    }

    private fun handlePagination(dataState: DataState<BlogViewState>){
        //Handle incoming data from DataState
        dataState.data?.let {
            it.data?.let {
                it.getContentIfNotHandled()?.let{
                    viewModel.handleIncomingBlogListData(it)
                }
            }
        }
        //Check for pagination end or else server will return ApiErrorResponse if page is not   valid

        dataState.error?.let { event ->
            event.peekContent().response.message?.let {
                if (ErrorHandling.isPaginationDone(it)){
                    //Handle error message event
                    event.getContentIfNotHandled()

                    //set query exhausted
                    viewModel.setQueryExhausted(true)
                }
            }
        }

    }
    private fun initRecyclerView(){

        blog_post_recyclerview.apply {
            layoutManager = LinearLayoutManager(this@BlogFragment.context)
            val topSpacingDecorator = TopSpacingItemDecoration(30)
            removeItemDecoration(topSpacingDecorator) // does nothing if not applied already
            addItemDecoration(topSpacingDecorator)

            recyclerAdapter = BlogListAdapter(requestManager,  this@BlogFragment)
            addOnScrollListener(object: RecyclerView.OnScrollListener(){

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastPosition = layoutManager.findLastVisibleItemPosition()
                    if (lastPosition == recyclerAdapter.itemCount.minus(1)) {
                        Log.d(TAG, "BlogFragment: attempting to load next page...")
                        viewModel.nextPage()
                    }
                }
            })
            adapter = recyclerAdapter
        }

    }

    override fun onItemSelected(position: Int, item: BlogPost) {
        viewModel.setBlogPost(item)
        findNavController().navigate(R.id.action_blogFragment_to_viewBlogFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // clear references (can leak memory)
        blog_post_recyclerview.adapter = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_menu, menu)
        initSearchView(menu)
    }

    override fun onRefresh() {
        onBlogSearchOrFilter()
        swipe_refresh.isRefreshing = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_filter_settings ->{
                showFilterOptions()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showFilterOptions(){
        //Step1: Show dialog
        activity?.let{
            val dialog = MaterialDialog(it)
                .noAutoDismiss()
                .customView(R.layout.layout_blog_filter)

            val view = dialog.getCustomView()


        //Step2: Highlight the previous filter options

            val filter = viewModel.getFilter()


            if(filter.equals(BLOG_FILTER_DATE_UPDATED)){
                view.findViewById<RadioGroup>(R.id.filter_group).check(R.id.filter_date)
            }
            else{
                view.findViewById<RadioGroup>(R.id.filter_group).check(R.id.filter_author)
            }

            val order = viewModel.getOrder()
            if(order.equals(BLOG_ORDER_ASC)){
                view.findViewById<RadioGroup>(R.id.order_group).check(R.id.filter_asc)
            }
            else{
                view.findViewById<RadioGroup>(R.id.order_group).check(R.id.filter_desc)
            }

        //Step3: Listen for new applied filters
            view.findViewById<TextView>(R.id.positive_button).setOnClickListener {
                val selectedFilter = dialog.getCustomView().findViewById<RadioButton>(
                    dialog.getCustomView().findViewById<RadioGroup>(R.id.filter_group).checkedRadioButtonId
                )

                val selectedOrder = dialog.getCustomView().findViewById<RadioButton>(
                    dialog.getCustomView().findViewById<RadioGroup>(R.id.order_group).checkedRadioButtonId
                )

                var filter = BLOG_FILTER_DATE_UPDATED
                if (selectedFilter.text.toString().equals(getString(R.string.filter_author))){
                    filter = BLOG_FILTER_USERNAME
                }

                var order = ""
                if(selectedOrder.text.toString().equals(getString(R.string.filter_desc))){
                    order = "-"
                }

        //Step4: Set filter and order in the view model and Save to shared preferences

                viewModel.saveFilterOptions(filter, order).let{
                    viewModel.setBlogFilter(filter)
                    viewModel.setBlogOrder(order)
                    onBlogSearchOrFilter()
                }
                dialog.dismiss()
            }

            view.findViewById<TextView>(R.id.negative_button).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

    }
}


