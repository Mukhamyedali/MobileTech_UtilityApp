package au.edu.jcu.assessment.utilityapp.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

data class ExchangeResponse(val base: String, val date: String, val rates: Map<String, Double>)

interface ExchangeApi {
    @GET("v4/latest/AUD")
    suspend fun latestAud(): ExchangeResponse
}

interface ExchangeRepository {
    suspend fun latestAud(): ExchangeResponse
}

class NetworkExchangeRepository(private val api: ExchangeApi) : ExchangeRepository {
    override suspend fun latestAud(): ExchangeResponse {
        val response = api.latestAud()
        require(response.base == "AUD" && response.date.isNotBlank()) { "Invalid rate response" }
        require(response.rates["AUD"] == 1.0) { "Missing base rate" }
        return response
    }
}

/** Application-level dependency container; dependencies are injected into the ViewModel. */
object AppContainer {
    val repository: ExchangeRepository by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .callTimeout(25, TimeUnit.SECONDS)
            .build()
        val api = Retrofit.Builder()
            .baseUrl("https://api.exchangerate-api.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(ExchangeApi::class.java)
        NetworkExchangeRepository(api)
    }
}
