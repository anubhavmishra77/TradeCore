package com.tradecore.app.demo

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class DemoRepository(context: Context, preferencesName: String = "paper_trading_v1") {
    private val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder().connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS).callTimeout(12, TimeUnit.SECONDS).build()

    fun load(): PaperAccount {
        val raw = preferences.getString("account", null) ?: return PaperAccount()
        val obj = JSONObject(raw)
        fun array(key: String) = obj.getJSONArray(key)
        return PaperAccount(
            name = obj.getString("name"), cash = decimal(obj.getString("cash")),
            deposits = decimal(obj.getString("deposits")), realized = decimal(obj.getString("realized")),
            holdings = array("holdings").objects().map { Holding(it.getString("symbol"), decimal(it.getString("quantity")), decimal(it.getString("cost"))) },
            orders = array("orders").objects().map { PaperOrder(it.getString("id"), it.getString("symbol"), it.getString("side"), decimal(it.getString("quantity")), decimal(it.getString("price")), decimal(it.getString("total")), it.getLong("timestamp"), it.getBoolean("sample")) },
            watchlist = array("watchlist").let { a -> (0 until a.length()).map { a.getString(it) }.toSet() },
            ledger = array("ledger").objects().map { CashEntry(it.getString("id"), it.getString("label"), decimal(it.getString("amount")), it.getLong("timestamp")) },
        )
    }

    suspend fun save(account: PaperAccount) = withContext(Dispatchers.IO) {
        val obj = JSONObject().put("name", account.name).put("cash", account.cash.toPlainString())
            .put("deposits", account.deposits.toPlainString()).put("realized", account.realized.toPlainString())
            .put("watchlist", JSONArray(account.watchlist.toList()))
            .put("holdings", JSONArray(account.holdings.map { JSONObject().put("symbol", it.symbol).put("quantity", it.quantity.toPlainString()).put("cost", it.cost.toPlainString()) }))
            .put("orders", JSONArray(account.orders.map { JSONObject().put("id", it.id).put("symbol", it.symbol).put("side", it.side).put("quantity", it.quantity.toPlainString()).put("price", it.price.toPlainString()).put("total", it.total.toPlainString()).put("timestamp", it.timestamp).put("sample", it.sample) }))
            .put("ledger", JSONArray(account.ledger.map { JSONObject().put("id", it.id).put("label", it.label).put("amount", it.amount.toPlainString()).put("timestamp", it.timestamp) }))
        check(preferences.edit().putString("account", obj.toString()).commit()) { "Could not save your account. Please try again." }
    }

    suspend fun quote(asset: Asset): MarketQuote = withContext(Dispatchers.IO) {
        val obj = JSONObject(get("products/${asset.symbol}-USD/stats"))
        val price = decimal(obj.getString("last"))
        val open = decimal(obj.getString("open"))
        val high = decimal(obj.getString("high"))
        val low = decimal(obj.getString("low"))
        require(price.signum() > 0 && open.signum() > 0 && high >= low)
        MarketQuote(asset.symbol, price, ((price.toDouble() / open.toDouble()) - 1) * 100,
            high, low, System.currentTimeMillis())
    }

    suspend fun candles(symbol: String): List<Float> = withContext(Dispatchers.IO) {
        require(assets.any { it.symbol == symbol })
        val array = JSONArray(get("products/$symbol-USD/candles?granularity=3600"))
        (0 until array.length()).map { array.getJSONArray(it) }.sortedBy { it.getLong(0) }
            .takeLast(24).map { it.getDouble(4).toFloat() }.also { require(it.size >= 2 && it.all { p -> p.isFinite() && p > 0 }) }
    }

    private fun get(path: String): String {
        val request = Request.Builder().url("https://api.exchange.coinbase.com/$path")
            .header("User-Agent", "TradeCore-Android-Demo/1.0").build()
        return client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "Market data unavailable (${response.code})." }
            response.body?.string() ?: error("Empty market response.")
        }
    }
}
private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }
