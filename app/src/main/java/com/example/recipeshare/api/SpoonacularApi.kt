package com.example.recipeshare.api

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://api.spoonacular.com/"
private const val API_KEY = "57accadfccdd4415a5c681318993aa7f" // Replace with your real API Key

interface SpoonacularApi {
    @GET("recipes/random")
    suspend fun getTrendingRecipes(
        @Query("number") number: Int = 10,   // ✅ Correct placement of @Query
        @Query("apiKey") apiKey: String = API_KEY // ✅ Correct placement
    ): RecipeResponse

    companion object {
        fun create(): SpoonacularApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SpoonacularApi::class.java)
        }
    }
}
