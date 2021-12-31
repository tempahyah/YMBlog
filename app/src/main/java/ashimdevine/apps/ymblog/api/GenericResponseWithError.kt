package ashimdevine.apps.ymblog.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GenericResponseWithError (
    @SerializedName("response")
    @Expose
    var response: String,

    @SerializedName("error_message")
    @Expose
    var error_message: String
) {}