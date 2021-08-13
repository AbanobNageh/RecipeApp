package com.abanobnageh.recipeapp.core.usecase

class Response<Error, ResponseType>(
    var error: Error? = null,
    var response: ResponseType? = null
) {
    fun isResponse(): Boolean {
        if (this.response != null) {
            return true
        }

        return false
    }
}