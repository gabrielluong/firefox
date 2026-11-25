/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.containers

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mozilla.components.browser.state.action.BrowserAction
import mozilla.components.browser.state.action.ContainerAction
import mozilla.components.browser.state.action.InitAction
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.lib.state.Middleware
import mozilla.components.lib.state.Store
import mozilla.components.support.base.log.logger.Logger

/**
 * [Middleware] that keeps [BrowserState.containers] in sync with the [ContainerRepository].
 *
 * @param repository The [ContainerRepository] holding the user's containers.
 * @param scope The [CoroutineScope] used to observe the [repository].
 */
class ContainerMiddleware(
    private val repository: ContainerRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : Middleware<BrowserState, BrowserAction> {
    private val logger = Logger("ContainerMiddleware")

    override fun invoke(
        store: Store<BrowserState, BrowserAction>,
        next: (BrowserAction) -> Unit,
        action: BrowserAction,
    ) {
        next(action)

        if (action is InitAction) {
            scope.launch {
                @Suppress("TooGenericExceptionCaught")
                try {
                    repository.containersFlow.collect { containers ->
                        store.dispatch(ContainerAction.UpdateContainersAction(containers))
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    logger.error("Failed to load the containers", e)
                }
            }
        }
    }
}
