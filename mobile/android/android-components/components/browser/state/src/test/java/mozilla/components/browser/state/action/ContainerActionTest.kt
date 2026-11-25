/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.browser.state.action

import mozilla.components.browser.state.reducer.BrowserStateReducer
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.ContainerState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContainerActionTest {

    @Test
    fun `UpdateContainersAction - Replaces the BrowserState containers`() {
        var state = BrowserState()
        assertTrue(state.containers.isEmpty())

        val container1 =
            ContainerState(
                contextId = "1",
                label = ContainerState.Label.Personal,
                color = ContainerState.Color.BLUE,
                icon = ContainerState.Icon.FINGERPRINT,
            )
        val container2 =
            ContainerState(
                contextId = "2",
                label = ContainerState.Label.Custom("Travel"),
                color = ContainerState.Color.GREEN,
                icon = ContainerState.Icon.VACATION,
            )

        state = BrowserStateReducer.reduce(state, ContainerAction.UpdateContainersAction(listOf(container1, container2)))
        assertEquals(listOf(container1, container2), state.containers.values.toList())

        state = BrowserStateReducer.reduce(state, ContainerAction.UpdateContainersAction(listOf(container2)))
        assertEquals(listOf(container2), state.containers.values.toList())
    }
}
