package com.tradecore.app.demo

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

private val ZERO = BigDecimal.ZERO
fun decimal(value: String) = value.toBigDecimal()
fun BigDecimal.money(): BigDecimal = setScale(2, RoundingMode.HALF_UP)

data class Asset(val symbol: String, val name: String, val samplePrice: String, val sampleChange: Double)
val assets = listOf(
    Asset("BTC", "Bitcoin", "67420.50", 2.34),
    Asset("ETH", "Ethereum", "3521.80", 1.82),
    Asset("SOL", "Solana", "148.62", -1.26),
    Asset("LINK", "Chainlink", "14.35", 3.12),
    Asset("AVAX", "Avalanche", "28.74", -0.64),
    Asset("DOGE", "Dogecoin", "0.1245", 0.93),
)
data class MarketQuote(
    val symbol: String, val price: BigDecimal, val change: Double,
    val high: BigDecimal, val low: BigDecimal, val fetchedAt: Long = 0,
) {
    val isSample get() = fetchedAt == 0L
    fun isStale(now: Long = System.currentTimeMillis()) = !isSample && now - fetchedAt > 120_000
}
fun sampleQuotes() = assets.map { asset ->
    val price = decimal(asset.samplePrice)
    MarketQuote(asset.symbol, price, asset.sampleChange, price * decimal("1.04"), price * decimal("0.96"))
}
data class Holding(val symbol: String, val quantity: BigDecimal, val cost: BigDecimal)
data class PaperOrder(
    val id: String, val symbol: String, val side: String, val quantity: BigDecimal,
    val price: BigDecimal, val total: BigDecimal, val timestamp: Long, val sample: Boolean,
)
data class CashEntry(val id: String, val label: String, val amount: BigDecimal, val timestamp: Long)
data class PaperAccount(
    val name: String = "", val cash: BigDecimal = decimal("10000.00"),
    val deposits: BigDecimal = decimal("10000.00"), val realized: BigDecimal = ZERO,
    val holdings: List<Holding> = emptyList(), val orders: List<PaperOrder> = emptyList(),
    val watchlist: Set<String> = setOf("BTC", "ETH", "SOL"),
    val ledger: List<CashEntry> = listOf(CashEntry("initial", "Welcome balance", decimal("10000"), System.currentTimeMillis())),
)

/** Pure, immutable paper ledger. Never submits orders to an exchange. */
object PaperTrading {
    fun trade(account: PaperAccount, quote: MarketQuote, side: String, input: String, now: Long = System.currentTimeMillis()): PaperAccount {
        require(side == "BUY" || side == "SELL") { "Choose buy or sell." }
        require(assets.any { it.symbol == quote.symbol }) { "Unknown asset." }
        require(input.matches(Regex("[0-9]{1,10}(\\.[0-9]{1,8})?"))) { "Enter a positive quantity with up to 8 decimals." }
        val quantity = input.toBigDecimalOrNull() ?: error("Enter a valid quantity.")
        require(quantity > ZERO && quantity.scale() <= 8 && quantity <= decimal("1000000000")) { "Enter a positive quantity with up to 8 decimals." }
        require(quote.price > ZERO && !quote.isStale(now)) { "This price is stale. Refresh prices before trading." }
        val total = (quote.price * quantity).money()
        require(total >= decimal("0.01")) { "Minimum order value is $0.01." }
        val previous = account.holdings.find { it.symbol == quote.symbol } ?: Holding(quote.symbol, ZERO, ZERO)
        val buy = side == "BUY"
        if (buy) require(account.cash >= total) { "Not enough virtual cash. Add demo funds in Wallet." }
        else require(previous.quantity >= quantity) { "Not enough ${quote.symbol} to sell." }
        val remaining = if (buy) previous.quantity + quantity else previous.quantity - quantity
        val removedCost = if (buy) ZERO else if (remaining.compareTo(ZERO) == 0) previous.cost
            else previous.cost.multiply(quantity).divide(previous.quantity, 12, RoundingMode.HALF_UP)
        val holding = Holding(quote.symbol, remaining, if (buy) previous.cost + total else previous.cost - removedCost)
        val id = UUID.randomUUID().toString()
        val cashChange = if (buy) total.negate() else total
        return account.copy(
            cash = account.cash + cashChange,
            realized = account.realized + if (buy) ZERO else total - removedCost,
            holdings = account.holdings.filterNot { it.symbol == quote.symbol } + if (remaining > ZERO) listOf(holding) else emptyList(),
            orders = listOf(PaperOrder(id, quote.symbol, side, quantity, quote.price, total, now, quote.isSample)) + account.orders,
            ledger = listOf(CashEntry(id, "$side ${quote.symbol}", cashChange, now)) + account.ledger,
        )
    }

    fun deposit(account: PaperAccount, input: String): PaperAccount {
        require(input.matches(Regex("[0-9]{1,6}(\\.[0-9]{1,2})?"))) { "Enter $1–$100,000 with up to 2 decimals." }
        val amount = input.toBigDecimalOrNull() ?: error("Enter a valid amount.")
        require(amount >= decimal("1") && amount <= decimal("100000") && amount.scale() <= 2) { "Enter $1–$100,000 with up to 2 decimals." }
        return account.copy(cash = account.cash + amount, deposits = account.deposits + amount,
            ledger = listOf(CashEntry(UUID.randomUUID().toString(), "Demo funds added", amount, System.currentTimeMillis())) + account.ledger)
    }
}
