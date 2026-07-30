package com.searchwars.app.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class LoginRequest(val email: String, val password: String)
data class GuestRequest(val displayName: String)
data class AuthResponse(val token: String, val username: String)
data class WordRound(val id: Long, val prompt: String, val attemptsLeft: Int)

interface SearchWarsService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/guest")
    suspend fun playAsGuest(@Body request: GuestRequest): AuthResponse

    @GET("api/game/round")
    suspend fun currentRound(): WordRound
}
