package com.urbysoft.xmlparser.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface StatsService {
    @GET("pls/prez2023/vysledky_kraj")
    fun getRegionData(@Query("kolo") round: String = "1", @Query("nuts") regionCode: String): Call<ResponseBody>

    @GET("pls/prez2023/vysledky_zahranici")
    fun getExternalData(@Query("kolo") round: String = "1"): Call<ResponseBody>
}