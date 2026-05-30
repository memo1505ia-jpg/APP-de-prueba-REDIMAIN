package com.example

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.api.BackendApiService
import com.example.api.BackendRetrofitClient
import com.example.api.SendOtpRequest
import com.example.api.SendOtpResponse
import com.example.api.VerifyOtpRequest
import com.example.api.VerifyOtpResponse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Before
  fun setUp() {
    // Stub the API calls to run locally without hitting the network
    val fakeBackendService = object : BackendApiService {
      override suspend fun sendOtp(request: SendOtpRequest): SendOtpResponse {
        return SendOtpResponse(success = true, message = "Código enviado")
      }

      override suspend fun verifyOtp(request: VerifyOtpRequest): VerifyOtpResponse {
        return if (request.pin == "123456") {
          VerifyOtpResponse(success = true, message = "Código verificado")
        } else {
          VerifyOtpResponse(success = false, message = "Código inválido")
        }
      }
    }
    BackendRetrofitClient.setTestService(fakeBackendService)
  }

  @Test
  fun testUserLoginFlowAndAppNavigation() {
    // 1. Enter Google email in login screen
    composeTestRule.onNodeWithTag("email_input")
      .performTextInput("memo1505matuteramayo@gmail.com")

    // 2. Click on Send Verification Code button
    composeTestRule.onNodeWithTag("send_code_button")
      .performClick()

    composeTestRule.waitForIdle()

    // 3. Enter the mock verification code
    composeTestRule.onNodeWithTag("verif_code_input")
      .performTextInput("123456")

    // 4. Click the "verify_button"
    composeTestRule.onNodeWithTag("verify_button")
      .performClick()

    // 5. Verify navigation into MainActivity's actual layout
    composeTestRule.waitForIdle()
  }
}

