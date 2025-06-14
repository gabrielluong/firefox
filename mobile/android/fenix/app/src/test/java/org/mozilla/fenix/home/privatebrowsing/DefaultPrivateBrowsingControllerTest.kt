/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.privatebrowsing

import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import mozilla.components.browser.state.store.BrowserStore
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.fenix.R
import org.mozilla.fenix.components.AppStore
import org.mozilla.fenix.components.appstate.AppState
import org.mozilla.fenix.components.usecases.FenixBrowserUseCases
import org.mozilla.fenix.home.privatebrowsing.controller.DefaultPrivateBrowsingController
import org.mozilla.fenix.utils.Settings

@RunWith(AndroidJUnit4::class)
class DefaultPrivateBrowsingControllerTest {

    private val appStore: AppStore = mockk(relaxed = true)
    private val navController: NavController = mockk(relaxed = true)
    private val settings: Settings = mockk(relaxed = true)
    private val fenixBrowserUseCases: FenixBrowserUseCases = mockk(relaxed = true)

    private lateinit var store: BrowserStore
    private lateinit var controller: DefaultPrivateBrowsingController

    @Before
    fun setup() {
        store = BrowserStore()
        controller = DefaultPrivateBrowsingController(
            navController = navController,
            fenixBrowserUseCases = fenixBrowserUseCases,
            settings = settings,
        )

        every { appStore.state } returns AppState()

        every { navController.currentDestination } returns mockk {
            every { id } returns R.id.homeFragment
        }
    }

    @Test
    fun `WHEN private browsing learn more link is clicked THEN open support page in browser`() {
        val learnMoreURL = "https://support.mozilla.org/en-US/kb/common-myths-about-private-browsing?as=u&utm_source=inproduct"

        controller.handleLearnMoreClicked()

        verify {
            navController.navigate(R.id.browserFragment)
            fenixBrowserUseCases.loadUrlOrSearch(
                searchTermOrURL = learnMoreURL,
                newTab = true,
                private = true,
            )
        }
    }

    @Test
    fun `GIVEN homepage as a new tab is enabled  WHEN private browsing learn more link is clicked THEN open support page in browser`() {
        every { settings.enableHomepageAsNewTab } returns true

        val learnMoreURL = "https://support.mozilla.org/en-US/kb/common-myths-about-private-browsing?as=u&utm_source=inproduct"

        controller.handleLearnMoreClicked()

        verify {
            navController.navigate(R.id.browserFragment)
            fenixBrowserUseCases.loadUrlOrSearch(
                searchTermOrURL = learnMoreURL,
                newTab = false,
                private = true,
            )
        }
    }
}
