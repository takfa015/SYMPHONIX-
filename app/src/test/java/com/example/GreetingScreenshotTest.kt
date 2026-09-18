package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.CashSession
import com.example.data.model.Disbursement
import com.example.data.model.SessionWithDetails
import com.example.ui.components.KpiCardsGrid
import com.example.ui.theme.RecapCaisseTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleSession = SessionWithDetails(
      session = CashSession(
        reference = "REC-20260918-01",
        dateText = "18 Septembre 2026",
        initialFund = 150000.0,
        initialFundTime = "19h35",
        countedCash = 38200.0,
        closingTime = "20h34",
        isClosed = true
      ),
      replenishments = emptyList(),
      disbursements = listOf(
        Disbursement(
          sessionId = 1,
          orderNumber = 1,
          time = "19:36",
          designation = "Œufs",
          parentCategory = "Matières premières",
          subCategory = "Œufs",
          amount = 57000.0
        )
      )
    )

    composeTestRule.setContent {
      RecapCaisseTheme {
        KpiCardsGrid(details = sampleSession)
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
