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

package com.duckduckgo.app.googlesdk

import android.content.Context
import android.view.View
import com.duckduckgo.app.browser.R
import com.duckduckgo.codegen.annotations.PriorityKey
import com.duckduckgo.common.ui.view.listitem.TwoLineListItem
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.navigation.api.GlobalActivityStarter
import com.duckduckgo.settings.api.OtherSettingsPlugin
import dev.zacsweers.metro.ContributesIntoMap
import javax.inject.Inject

/**
 * Settings entry for the Google Ads test screen. Shows in the main Settings
 * list, below the other settings entries.
 */
@ContributesIntoMap(ActivityScope::class)
@PriorityKey(300)
class GoogleAdsSettingsEntry @Inject constructor(
    private val globalActivityStarter: GlobalActivityStarter,
) : OtherSettingsPlugin {

    override fun getView(context: Context): View {
        return TwoLineListItem(context).apply {
            setPrimaryText(context.getString(R.string.googleAdsTestSettingsTitle))
            setSecondaryText(context.getString(R.string.googleAdsTestSettingsSubtitle))
            setClickListener { globalActivityStarter.start(context, GoogleAdsTestNoParams) }
        }
    }
}
