package com.tradecore.app.demo

import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class PaperTradingTest {
    private val quote = MarketQuote("BTC", decimal("100"), 0.0, decimal("110"), decimal("90"))
    private fun equal(expected: String, actual: BigDecimal) = assertEquals(0, decimal(expected).compareTo(actual))
    private fun rejected(block: () -> Unit) { try { block(); fail("Expected validation failure") } catch (_: IllegalArgumentException) {} catch (_: IllegalStateException) {} }

    @Test fun buyUpdatesCashPositionOrderAndLedger() {
        val result = PaperTrading.trade(PaperAccount(), quote, "BUY", "2")
        equal("9800", result.cash); equal("2", result.holdings.single().quantity)
        equal("200", result.holdings.single().cost); equal("200", result.orders.single().total)
        equal("-200", result.ledger.first().amount)
        assertTrue(result.orders.single().sample)
    }
    @Test fun partialSellPreservesCostBasisAndRecognizesProfit() {
        val bought = PaperTrading.trade(PaperAccount(), quote, "BUY", "2")
        val sold = PaperTrading.trade(bought, quote.copy(price = decimal("150")), "SELL", "0.5")
        equal("9875", sold.cash); equal("1.5", sold.holdings.single().quantity)
        equal("150", sold.holdings.single().cost); equal("25", sold.realized)
    }
    @Test fun fullSellRemovesPositionAndConservesEquity() {
        val bought = PaperTrading.trade(PaperAccount(), quote, "BUY", "3")
        val sold = PaperTrading.trade(bought, quote.copy(price = decimal("90")), "SELL", "3")
        assertTrue(sold.holdings.isEmpty()); equal("9970", sold.cash); equal("-30", sold.realized)
        equal("10000", sold.deposits)
    }
    @Test fun insufficientFundsNeverChangesOriginalAccount() {
        val account = PaperAccount()
        rejected { PaperTrading.trade(account, quote, "BUY", "101") }
        equal("10000", account.cash); assertTrue(account.orders.isEmpty())
    }
    @Test fun oversellingIsRejected() { rejected { PaperTrading.trade(PaperAccount(), quote, "SELL", "1") } }
    @Test fun invalidQuantitiesAreRejected() {
        listOf("", "abc", "0", "-1", "0.000000001", "1000000001", "NaN", "1e999999999").forEach {
            rejected { PaperTrading.trade(PaperAccount(), quote, "BUY", it) }
        }
    }
    @Test fun dustOrdersCannotRoundToFreeTrades() { rejected { PaperTrading.trade(PaperAccount(), quote, "BUY", "0.00000001") } }
    @Test fun staleNetworkQuotesAreRejectedButSamplesRemainUsable() {
        rejected { PaperTrading.trade(PaperAccount(), quote.copy(fetchedAt = 1), "BUY", "1", now = 120002) }
        equal("9900", PaperTrading.trade(PaperAccount(), quote, "BUY", "1").cash)
    }
    @Test fun freshNetworkQuoteRecordsSource() {
        val order = PaperTrading.trade(PaperAccount(), quote.copy(fetchedAt = 1000), "BUY", "1", now = 2000).orders.single()
        assertFalse(order.sample)
    }
    @Test fun repeatedBuysUseWeightedCostBasis() {
        val first = PaperTrading.trade(PaperAccount(), quote, "BUY", "2")
        val second = PaperTrading.trade(first, quote.copy(price = decimal("200")), "BUY", "1")
        equal("400", second.holdings.single().cost)
        val sold = PaperTrading.trade(second, quote.copy(price = decimal("150")), "SELL", "3")
        equal("50", sold.realized); equal("10050", sold.cash)
    }
    @Test fun depositsIncreaseCashAndFundingWithoutProfit() {
        val result = PaperTrading.deposit(PaperAccount(), "1000.25")
        equal("11000.25", result.cash); equal("11000.25", result.deposits); equal("0", result.realized)
        equal("1000.25", result.ledger.first().amount)
    }
    @Test fun invalidDepositsAreRejected() {
        listOf("-1", "0", "100001", "1.001", "", "1e999999999").forEach { rejected { PaperTrading.deposit(PaperAccount(), it) } }
    }
    @Test fun fractionalRoundTripDoesNotLeaveCostResidue() {
        val first = PaperTrading.trade(PaperAccount(), quote.copy(price = decimal("333.33")), "BUY", "0.03")
        val second = PaperTrading.trade(first, quote, "SELL", "0.01")
        val third = PaperTrading.trade(second, quote, "SELL", "0.02")
        assertTrue(third.holdings.isEmpty()); equal("-7", third.realized); equal("9993", third.cash)
    }
}
