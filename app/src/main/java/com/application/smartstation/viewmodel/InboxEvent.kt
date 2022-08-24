package com.application.smartstation.viewmodel

import org.json.JSONObject

class InboxEvent(jsonObject: JSONObject?) {

    private var jsonObject: JSONObject? = null

    init {
        this.jsonObject = jsonObject
    }

    fun getJsonObject(): JSONObject? {
        return jsonObject
    }

    fun setJsonObject(jsonObject: JSONObject?) {
        this.jsonObject = jsonObject
    }

}