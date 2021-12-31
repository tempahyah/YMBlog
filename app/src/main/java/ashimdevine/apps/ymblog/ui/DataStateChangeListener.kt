package ashimdevine.apps.ymblog.ui

interface DataStateChangeListener {

    fun onDataStateChange(dataState: DataState<*>?)

    fun expandAppBar()

    fun hideSoftKeyboard()

}