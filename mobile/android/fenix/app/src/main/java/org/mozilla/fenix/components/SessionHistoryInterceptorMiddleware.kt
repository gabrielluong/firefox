/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.components

import mozilla.components.browser.state.action.BrowserAction
import mozilla.components.browser.state.action.EngineAction
import mozilla.components.browser.state.selector.findTab
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.lib.state.Middleware
import mozilla.components.lib.state.MiddlewareContext
import org.mozilla.fenix.AppRequestInterceptor

class SessionHistoryInterceptorMiddleware(
    private val requestInterceptor: AppRequestInterceptor,
) : Middleware<BrowserState, BrowserAction> {

    override fun invoke(
        context: MiddlewareContext<BrowserState, BrowserAction>,
        next: (BrowserAction) -> Unit,
        action: BrowserAction,
    ) {
        val currentState = context.state

        when (action) {
            is EngineAction.GoBackAction -> {
                currentState.findTab(action.tabId)?.let { tab ->
                    val (items, currentIndex) = tab.content.history
                    val historyItem = items[currentIndex - 1]
                    requestInterceptor.onNavigateBack(historyItem.uri)
                }
            }

            is EngineAction.GoForwardAction -> {
                currentState.findTab(action.tabId)?.let { tab ->
                    val (items, currentIndex) = tab.content.history
                    val historyItem = items[currentIndex + 1]
                    requestInterceptor.onNavigateForward(historyItem.uri)
                }
            }

            is EngineAction.GoToHistoryIndexAction -> {
                currentState.findTab(action.tabId)?.let { tab ->
                    val (items) = tab.content.history
                    val historyItem = items[action.index]
                    requestInterceptor.onNavigateForward(historyItem.uri)
                }
            }

            else -> {
                // no-op
            }
        }

        next(action)
    }
}
