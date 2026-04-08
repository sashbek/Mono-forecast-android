package org.pakicek.monoforecast.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface EchoApiInterface {

    @GET("server/echo/{echoPath}")
    suspend fun getByPath(
        @Path(value = "echoPath", encoded = true) echoPath: String
    ): Response<Any?>

    @PUT("server/echo/{echoPath}")
    suspend fun putByPath(
        @Path(value = "echoPath", encoded = true) echoPath: String,
        @Body payload: Any?
    ): Response<Any?>
}