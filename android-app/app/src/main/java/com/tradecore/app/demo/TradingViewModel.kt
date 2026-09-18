package com.tradecore.app.demo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TradingState(
    val account: PaperAccount = PaperAccount(), val quotes: List<MarketQuote> = sampleQuotes(),
    val refreshing: Boolean = false, val saving: Boolean = false, val message: String? = null,
    val marketStatus: String = "Offline sample prices • USD", val storageError: Boolean = false,
)
class TradingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DemoRepository(application)
    private val mutable = MutableStateFlow(TradingState())
    val state = mutable.asStateFlow()
    init {
        try { mutable.update { it.copy(account = repository.load()) } }
        catch (_: Exception) { mutable.update { it.copy(storageError = true, message = "Saved account could not be read. Reset it in Wallet to start again.") } }
        refresh()
    }
    fun dismissMessage() = mutable.update { it.copy(message = null) }
    fun refresh() {
        if (mutable.value.refreshing) return
        viewModelScope.launch {
            mutable.update { it.copy(refreshing = true) }
            val results = assets.map { asset -> async {
                try { repository.quote(asset) } catch (e: CancellationException) { throw e } catch (_: Exception) { null }
            } }.awaitAll()
            mutable.update { current ->
                val quotes = current.quotes.map { old -> results.filterNotNull().find { it.symbol == old.symbol } ?: old }
                val count = results.count { it != null }
                current.copy(quotes = quotes, refreshing = false, marketStatus = when (count) {
                    assets.size -> "Coinbase prices • USD • just refreshed"
                    0 -> "Connection unavailable • sample or last fetched prices"
                    else -> "$count of ${assets.size} prices refreshed • others are sample or cached"
                })
            }
        }
    }
    fun start(name: String) = change { account ->
        require(name.trim().length in 2..30) { "Enter a name between 2 and 30 characters." }
        account.copy(name = name.trim())
    }
    fun watch(symbol: String) = change { it.copy(watchlist = if (symbol in it.watchlist) it.watchlist - symbol else it.watchlist + symbol) }
    fun trade(quote: MarketQuote, side: String, quantity: String, done: () -> Unit) = change(done) {
        PaperTrading.trade(it, quote, side, quantity)
    }
    fun deposit(amount: String, done: () -> Unit) = change(done) { PaperTrading.deposit(it, amount) }
    fun reset(done: () -> Unit) = change(done, allowReset = true) { PaperAccount(name = it.name) }
    suspend fun candles(symbol: String) = repository.candles(symbol)
    private fun change(done: () -> Unit = {}, allowReset: Boolean = false, transform: (PaperAccount) -> PaperAccount) {
        if (mutable.value.saving) return
        if (mutable.value.storageError && !allowReset) {
            mutable.update { it.copy(message = "Reset the unreadable account from Wallet before making changes.") }; return
        }
        mutable.update { it.copy(saving = true) }
        viewModelScope.launch {
            try {
                val updated = transform(mutable.value.account)
                repository.save(updated)
                mutable.update { it.copy(account = updated, saving = false, storageError = false) }
                done()
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { mutable.update { it.copy(saving = false, message = e.message ?: "Something went wrong. Please try again.") } }
        }
    }
}
