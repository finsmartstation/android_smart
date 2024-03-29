package com.application.smartstation.ui.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Response(
    @SerializedName("confident")
    val confident: Boolean,
    @SerializedName("venues")
    val venues: List<Venue>
)
