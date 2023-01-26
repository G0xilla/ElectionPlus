package com.urbysoft.server

import com.urbysoft.xmlparser.constants.Constants
import com.urbysoft.xmlparser.data.Nuts
import com.urbysoft.xmlparser.entity.*
import com.urbysoft.xmlparser.network.StatsService
import com.urbysoft.xmlparser.parse.XMLStatsParser
import org.springframework.stereotype.Repository
import retrofit2.Retrofit
import retrofit2.create
import java.io.IOException
import java.lang.RuntimeException
import java.util.IllegalFormatException

@Repository
class VotesRepository {
    private val parser = XMLStatsParser()
    private val retrofitStats = Retrofit.Builder()
        .baseUrl(Constants.statsBaseUrl)
        .build()
    private val statsService = retrofitStats.create<StatsService>()

    fun getVotes(): Pair<List<Region>, ExternalResultOk> {
        val regionResultList = mutableListOf<Region>()
        for(i in Nuts.values()) {
            var valid = false
            var counter = 0
            while(!valid) {
                counter++
                try {
                    val responseBody = statsService.getRegionData(round = "2", regionCode = i.code)
                        .execute()
                    when(val result = parser.parseMunicipalities(responseBody.body()!!.byteStream())) {
                        is  MunicipalitiesResultError -> {
                            println(i.code)
                            throw InvalidResponseException(message = result.message)
                        }
                        is MunicipalitiesResultOk -> {
                            valid = true
                            regionResultList.add(result.region)
                        }
                    }
                } catch (e: IOException) {
                } catch (e: RuntimeException) {
                } finally {
                    counter++
                }

                if(counter > 10) {
                    throw InvalidResponseException("Too many try")
                }
            }
        }

        var valid = false
        var counter = 0
        var externalResult: ExternalResultOk? = null
        while(!valid) {
            counter++
            try {
                val responseBody = statsService.getExternalData("2").execute()

                when(val externalStats = parser.parseExternal(responseBody.body()!!.byteStream())) {
                    is ExternalResultOk -> {
                        externalResult = externalStats
                        valid = true
                    }
                    is ExternalResultError -> {
                        throw InvalidResponseException(externalStats.message)
                    }
                }
            } catch (_: IOException) {
            } catch (_: RuntimeException) {
            } finally {
                counter++
            }

            if(counter > 10) {
                throw InvalidResponseException("Too many try")
            }
        }

        return Pair(regionResultList, externalResult!!)
    }


}