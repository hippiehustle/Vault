package com.skyview.weather.presentation.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.skyview.weather.core.security.TapSequenceTracker
import com.skyview.weather.util.Constants
import com.skyview.weather.util.TapTarget
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Weather widget using Glance API.
 * Displays current weather with tap targets for vault authentication.
 */
class WeatherWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WeatherWidgetContent(context)
        }
    }

    @Composable
    private fun WeatherWidgetContent(context: Context) {
        val size = LocalSize.current

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFFE3F2FD))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tap Target #1: Weather Icon
            Box(
                modifier = GlanceModifier
                    .size(48.dp)
                    .clickable(actionRunCallback<TapTarget1ActionCallback>()),
                contentAlignment = Alignment.Center
            ) {
                // Weather icon placeholder
                Text(
                    text = "☀️",
                    style = TextStyle(
                        fontSize = 40.sp
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Tap Target #2: Temperature
            Box(
                modifier = GlanceModifier
                    .clickable(actionRunCallback<TapTarget2ActionCallback>())
            ) {
                Text(
                    text = "72°",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(Color(0xFF1E88E5))
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            // Location name
            Text(
                text = "San Francisco",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = ColorProvider(Color(0xFF666666))
                )
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            // Condition
            Text(
                text = "Partly Cloudy",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = ColorProvider(Color(0xFF888888))
                )
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Tap Target #3: High/Low temperatures
            Box(
                modifier = GlanceModifier
                    .clickable(actionRunCallback<TapTarget3ActionCallback>())
            ) {
                Text(
                    text = "H: 78° L: 65°",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = ColorProvider(Color(0xFF666666))
                    )
                )
            }
        }
    }
}

/**
 * Widget receiver.
 */
@AndroidEntryPoint
class WeatherWidgetReceiver : GlanceAppWidgetReceiver() {

    @Inject
    lateinit var tapSequenceTracker: TapSequenceTracker

    override val glanceAppWidget: GlanceAppWidget = WeatherWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // Handle custom actions
        when (intent.action) {
            Constants.ACTION_WIDGET_TAP_1 -> {
                tapSequenceTracker.recordTap(TapTarget.CLOUD_ICON)
            }
            Constants.ACTION_WIDGET_TAP_2 -> {
                tapSequenceTracker.recordTap(TapTarget.TEMPERATURE)
            }
            Constants.ACTION_WIDGET_TAP_3 -> {
                tapSequenceTracker.recordTap(TapTarget.HIGH_LOW_TEMP)
            }
        }
    }
}

/**
 * Action callback for first tap target.
 */
class TapTarget1ActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // Record tap in sequence tracker
        val intent = Intent(context, WeatherWidgetReceiver::class.java).apply {
            action = Constants.ACTION_WIDGET_TAP_1
        }
        context.sendBroadcast(intent)
    }
}

/**
 * Action callback for second tap target.
 */
class TapTarget2ActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val intent = Intent(context, WeatherWidgetReceiver::class.java).apply {
            action = Constants.ACTION_WIDGET_TAP_2
        }
        context.sendBroadcast(intent)
    }
}

/**
 * Action callback for third tap target.
 */
class TapTarget3ActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val intent = Intent(context, WeatherWidgetReceiver::class.java).apply {
            action = Constants.ACTION_WIDGET_TAP_3
        }
        context.sendBroadcast(intent)
    }
}
