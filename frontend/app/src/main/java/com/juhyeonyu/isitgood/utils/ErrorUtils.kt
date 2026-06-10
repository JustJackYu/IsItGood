package com.juhyeonyu.isitgood.utils

import org.json.JSONObject
import retrofit2.HttpException

fun parseHttpError(e: HttpException): String {
    return try {
        val errorBody = e.response()?.errorBody()?.string()
        if (errorBody != null) JSONObject(errorBody).optString("message", "Something went wrong")
        else "Something went wrong"
    } catch (ex: Exception) {
        "Something went wrong"
    }
}