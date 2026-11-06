package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.FocusSessionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FocusLock", appName)
  }

  @Test
  fun `verify timer presets and default duration`() {
    val presets = FocusSessionManager.PRESET_MINUTES
    assertEquals(listOf(15, 25, 30, 45, 60, 90), presets)
    assertEquals(30, FocusSessionManager.selectedDurationMinutes.value)
  }
}
