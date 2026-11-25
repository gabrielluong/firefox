/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.containers.ext

import android.content.Context
import mozilla.components.browser.state.state.ContainerState
import mozilla.components.feature.containers.R

/** Returns the localized label of this container. */
fun ContainerState.getLabel(context: Context): String =
    when (val label = label) {
        is ContainerState.Label.Custom -> label.name
        ContainerState.Label.Personal -> context.getString(R.string.mozac_feature_containers_label_personal)
        ContainerState.Label.Work -> context.getString(R.string.mozac_feature_containers_label_work)
        ContainerState.Label.Banking -> context.getString(R.string.mozac_feature_containers_label_banking)
        ContainerState.Label.Shopping -> context.getString(R.string.mozac_feature_containers_label_shopping)
    }
