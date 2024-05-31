package de.domjos.cloudapp2.widgets

import android.content.Context
import android.os.Build
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.material3.ColorProviders
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import de.domjos.cloudapp2.appbasics.ui.theme.Pink40
import de.domjos.cloudapp2.appbasics.ui.theme.Pink80
import de.domjos.cloudapp2.appbasics.ui.theme.Purple40
import de.domjos.cloudapp2.appbasics.ui.theme.Purple80
import de.domjos.cloudapp2.appbasics.ui.theme.PurpleGrey40
import de.domjos.cloudapp2.appbasics.ui.theme.PurpleGrey80

abstract class AbstractWidget<T> : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    GlanceTheme.colors
                else
                    MyAppWidgetGlanceColorScheme.colors
            ) {
                Content()
            }
        }
    }

    @Composable
    abstract fun Content()
}

object MyAppWidgetGlanceColorScheme {

    val colors = ColorProviders(
        light = LightColorScheme,
        dark = DarkColorScheme
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80

)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)