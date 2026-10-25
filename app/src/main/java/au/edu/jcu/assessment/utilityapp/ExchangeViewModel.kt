package au.edu.jcu.assessment.utilityapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import au.edu.jcu.assessment.utilityapp.data.ExchangeRepository
import au.edu.jcu.assessment.utilityapp.data.ExchangeResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val supportedCurrencies = listOf("KZT", "MNT", "USD", "EUR", "GBP", "TRY", "NZD")

data class ExchangeUiState(
    val currency: String = "KZT",
    val amount: String = "100",
    val showUnitRate: Boolean = true,
    val loading: Boolean = false,
    val data: ExchangeResponse? = null,
    val error: String? = null
)

/** Pure calculation shared by UI and unit tests. Invalid inputs never become zero. */
fun convertAmount(amount: String, rate: Double?): Double? {
    val value = amount.toDoubleOrNull() ?: return null
    if (!value.isFinite() || value < 0 || rate == null || !rate.isFinite() || rate <= 0) return null
    return (value * rate).takeIf { it.isFinite() }
}

class ExchangeViewModel(private val repository: ExchangeRepository) : ViewModel() {
    private val _state = MutableStateFlow(ExchangeUiState())
    val state = _state.asStateFlow()

    init { refresh() }

    fun setCurrency(value: String) {
        if (value in supportedCurrencies) _state.update { it.copy(currency = value) }
    }
    fun setAmount(value: String) { _state.update { it.copy(amount = value) } }
    fun setUnitRate(value: Boolean) { _state.update { it.copy(showUnitRate = value) } }

    fun refresh() {
        if (_state.value.loading) return
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val data = repository.latestAud()
                _state.update { it.copy(data = data, loading = false) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _state.update { it.copy(loading = false,
                    error = "Could not update rates. Check your connection and try again.") }
            }
        }
    }

    class Factory(private val repository: ExchangeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ExchangeViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return ExchangeViewModel(repository) as T
        }
    }
}
