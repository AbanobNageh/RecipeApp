package com.abanobnageh.recipeapp.core.usecase

import com.abanobnageh.recipeapp.core.error.Error

interface Usecase<ResponseType, Params> {
    suspend fun call(params: Params) : Response<Error, ResponseType>
}

class NoParams() {}