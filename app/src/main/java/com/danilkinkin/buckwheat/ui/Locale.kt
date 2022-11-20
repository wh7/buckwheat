import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.danilkinkin.buckwheat.appLocale
import com.danilkinkin.buckwheat.dataStore
import com.danilkinkin.buckwheat.systemLocale
import kotlinx.coroutines.flow.first
import java.util.Locale

@Composable
fun OverrideLocalize(content: @Composable () -> Unit) {
    val systemLocale = LocalContext.current.systemLocale!!
    val overrideLocale = LocalContext.current.appLocale ?: systemLocale
    Log.d("OverrideLocalize", "Change locale to ${overrideLocale.language}")

    val (context, configuration) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Pair(LocalContext.current, LocalConfiguration.current)
    } else {
        val config = Configuration(LocalConfiguration.current)

        config.setLocales(LocaleList(overrideLocale))
        config.setLocale(overrideLocale)

        Locale.setDefault(overrideLocale)

        val context = LocalContext.current

        context.resources.configuration.setLocales(LocaleList(overrideLocale))
        context.resources.configuration.setLocale(overrideLocale)


        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        Pair(context, config)
    }

    CompositionLocalProvider(
        LocalConfiguration provides configuration,
        LocalContext provides context,
    ) {
        content()
    }
}

suspend fun switchOverrideLocale(context: Context, localeCode: String?) {
    context.dataStore.edit {
        if (localeCode != null) {
            it[stringPreferencesKey("locale")] = localeCode
        } else {
            it.minusAssign(stringPreferencesKey("locale"))
        }
    }

    context.appLocale = if (localeCode != null) Locale(localeCode) else null
}

suspend fun syncOverrideLocale(context: Context) {
    context.systemLocale = context.resources.configuration.locales[0]

    val currentValue = context.dataStore.data.first()

    val localeCode = currentValue[stringPreferencesKey("locale")]

    val locale = if (localeCode !== null) {
        Locale(localeCode)
    } else {
        null
    }

    context.appLocale = locale
}