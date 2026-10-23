package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.CoinSide
import com.example.model.TierLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Coin Flip", appName)
  }

  @Test
  fun `test tier level logic`() {
    val bronze = TierLevel.fromWager(500L)
    assertEquals(TierLevel.BRONZE, bronze)

    val silver = TierLevel.fromWager(15_000L)
    assertEquals(TierLevel.SILVER, silver)

    val gold = TierLevel.fromWager(150_000L)
    assertEquals(TierLevel.GOLD, gold)
  }

  @Test
  fun `test coin side opposites`() {
    assertEquals(CoinSide.TAILS, CoinSide.HEADS.opposite())
    assertEquals(CoinSide.HEADS, CoinSide.TAILS.opposite())
  }
}
