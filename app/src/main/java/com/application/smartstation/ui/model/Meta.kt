package com.application.smartstation.ui.model

import com.google.gson.annotations.SerializedName
import io.realm.internal.Keep

@Keep
data class Meta(
    @SerializedName("code")
                 val code: Int,
                 @SerializedName("requestId")
                 val requestId: String)
