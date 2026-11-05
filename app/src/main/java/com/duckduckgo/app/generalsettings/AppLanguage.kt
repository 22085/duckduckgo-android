/*
 * Copyright (c) 2026 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.app.generalsettings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * App language choices offered in General Settings (duckduckgo-android fork).
 * SYSTEM means no app-level override: the app follows the device language.
 * The choice is applied through AppCompatDelegate, which persists it
 * (LocaleManager on API 33+, SharedPreferences below) and recreates
 * activities so the new language takes effect immediately.
 */
enum class AppLanguage(val languageTag: String?) {
    SYSTEM(null),
    ENGLISH("en"),
    CHINESE("zh-CN");

    companion object {
        /** The BCP 47 tag currently applied at app level, or null when following the system. */
        fun currentTag(): String? {
            val locales = AppCompatDelegate.getApplicationLocales()
            if (locales.isEmpty) return null
            return locales.get(0)?.toLanguageTag()
        }

        /** The offered option matching the currently applied locale, SYSTEM if none matches. */
        fun current(): AppLanguage {
            val tag = currentTag() ?: return SYSTEM
            return when {
                tag.startsWith("zh") -> CHINESE
                tag.startsWith("en") -> ENGLISH
                else -> SYSTEM
            }
        }
    }

    fun apply() {
        val locales = if (languageTag == null) {
            LocaleListCompat.getEmpty()
        } else {
            LocaleListCompat.forLanguageTags(languageTag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
