package com.tradecore.app.demo

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DemoRepositoryTest {
    @Test fun accountSurvivesRepositoryRecreation() = runBlocking {
        // Dedicated preference file, deliberately separate from the user's account.
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val quote = sampleQuotes().first()
        val account = PaperTrading.deposit(
            PaperTrading.trade(PaperAccount(name = "Persistence test"), quote, "BUY", "0.01"), "125.50",
        ).copy(watchlist = setOf("BTC", "LINK"))
        DemoRepository(context, "paper_trading_test").save(account)
        assertEquals(account, DemoRepository(context, "paper_trading_test").load())
    }
}
