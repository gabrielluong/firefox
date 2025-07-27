/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.compose.browser.toolbar.ui

import android.view.SoundEffectConstants
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import mozilla.components.compose.base.Badge
import mozilla.components.compose.base.BadgeState
import mozilla.components.compose.base.Divider
import mozilla.components.compose.base.modifier.thenConditional
import mozilla.components.compose.base.theme.AcornTheme
import mozilla.components.compose.browser.toolbar.store.BrowserToolbarInteraction
import mozilla.components.compose.browser.toolbar.store.BrowserToolbarInteraction.BrowserToolbarEvent
import mozilla.components.compose.browser.toolbar.store.BrowserToolbarInteraction.BrowserToolbarMenu
import mozilla.components.compose.browser.toolbar.store.BrowserToolbarInteraction.CombinedEventAndMenu
import mozilla.components.compose.browser.toolbar.store.BrowserToolbarMenuItem
import mozilla.components.compose.browser.toolbar.store.BrowserToolbarMenuItem.BrowserToolbarMenuIconButton
import mozilla.components.compose.browser.toolbar.store.BrowserToolbarMenuItem.BrowserToolbarMenuButton
import mozilla.components.compose.browser.toolbar.store.BrowserToolbarMenuItem.BrowserToolbarMenuButton.ContentDescription.StringContentDescription
import mozilla.components.compose.browser.toolbar.store.BrowserToolbarMenuItem.BrowserToolbarMenuButton.ContentDescription.StringResContentDescription
import mozilla.components.compose.browser.toolbar.store.BrowserToolbarMenuItem.BrowserToolbarMenuButton.Icon.DrawableIcon
import mozilla.components.compose.browser.toolbar.store.BrowserToolbarMenuItem.BrowserToolbarMenuButton.Icon.DrawableResIcon
import mozilla.components.compose.browser.toolbar.store.BrowserToolbarMenuItem.BrowserToolbarMenuButton.Text.StringResText
import mozilla.components.compose.browser.toolbar.store.BrowserToolbarMenuItem.BrowserToolbarMenuButton.Text.StringText
import mozilla.components.compose.browser.toolbar.store.BrowserToolbarMenuItem.BrowserToolbarMenuDivider
import mozilla.components.support.base.log.logger.Logger

@Stable
internal fun BrowserToolbarInteraction.toMenuItems(): List<BrowserToolbarMenuItem> = when (this) {
    is BrowserToolbarMenu -> items()
    is CombinedEventAndMenu -> menu.items()
    else -> emptyList()
}

@Composable
@Suppress("LongMethod")
internal fun menuItemComposable(
    source: BrowserToolbarMenuItem,
    onInteraction: (BrowserToolbarEvent) -> Unit,
): @Composable () -> Unit {
    return when (source) {
        is BrowserToolbarMenuButton -> {
            @Composable {
                val view = LocalView.current
                val contentDescription = source.contentDescription()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .thenConditional(
                            Modifier.clickable(
                                role = Role.Button,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(
                                    bounded = true,
                                    color = AcornTheme.colors.ripple,
                                ),
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    source.onClick?.let { onInteraction(it) }
                                },
                            ),
                        ) { source.onClick != null }
                        .semantics(mergeDescendants = true) {
                            this.contentDescription = contentDescription
                        }
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .padding(horizontal = 16.dp),
                ) {
                    when (source.icon) {
                        is DrawableIcon -> {
                            Image(
                                painter = rememberDrawablePainter(source.icon.drawable),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                contentScale = ContentScale.Crop,
                                colorFilter = when (source.icon.shouldTint) {
                                    true -> if (source.state == BrowserToolbarMenuButton.State.ACTIVE) {
                                        ColorFilter.tint(AcornTheme.colors.iconAccentViolet)
                                    } else {
                                        ColorFilter.tint(AcornTheme.colors.iconPrimary)
                                    }
                                    else -> null
                                },
                            )
                        }
                        is DrawableResIcon -> {
                            Icon(
                                painter = painterResource(source.icon.resourceId),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (source.state == BrowserToolbarMenuButton.State.ACTIVE) {
                                    AcornTheme.colors.iconAccentViolet
                                } else {
                                    AcornTheme.colors.iconPrimary
                                },
                            )
                        }
                        null -> {}
                    }

                    if (source.icon != null) {
                        Spacer(modifier = Modifier.width(20.dp))
                    }

                    Text(
                        text = source.text(),
                        modifier = Modifier.wrapContentSize(Alignment.CenterStart),
                        color = if (source.state == BrowserToolbarMenuButton.State.ACTIVE) {
                            AcornTheme.colors.textAccent
                        } else {
                            AcornTheme.colors.textPrimary
                        },
                        maxLines = 1,
                        style = AcornTheme.typography.subtitle1,
                    )

                    if (source.badgeText != null) {
                        Spacer(modifier = Modifier.width(20.dp))

                        Badge(
                            text = source.badgeText(),
                            state = if (source.state == BrowserToolbarMenuButton.State.ACTIVE) {
                                BadgeState.ACTIVE
                            } else {
                                BadgeState.DEFAULT
                            },
                            backgroundColor = AcornTheme.colors.badgeActive,
                        )
                    }
                }
            }
        }

        is BrowserToolbarMenuIconButton -> {
            @Composable {
                val view = LocalView.current
                val contentDescription = source.contentDescription()

                Box(
                    modifier = Modifier
                        .thenConditional(
                            Modifier.clickable(
                                role = Role.Button,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(
                                    bounded = true,
                                    color = AcornTheme.colors.ripple,
                                ),
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    source.onClick?.let { onInteraction(it) }
                                },
                            ),
                        ) { source.onClick != null }
                        .semantics(mergeDescendants = true) {
                            this.contentDescription = contentDescription
                        }
                        .minimumInteractiveComponentSize(),
                ) {
                    when (source.icon) {
                        is BrowserToolbarMenuIconButton.Icon.DrawableIcon -> {
                            Image(
                                painter = rememberDrawablePainter(source.icon.drawable),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                contentScale = ContentScale.Crop,
                                colorFilter = when (source.icon.shouldTint) {
                                    true -> if (source.state == BrowserToolbarMenuIconButton.State.ACTIVE) {
                                        ColorFilter.tint(AcornTheme.colors.iconAccentViolet)
                                    } else {
                                        ColorFilter.tint(AcornTheme.colors.iconPrimary)
                                    }
                                    else -> null
                                },
                            )
                        }

                        is BrowserToolbarMenuIconButton.Icon.DrawableResIcon -> {
                            Icon(
                                painter = painterResource(source.icon.resourceId),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (source.state == BrowserToolbarMenuIconButton.State.ACTIVE) {
                                    AcornTheme.colors.iconAccentViolet
                                } else {
                                    AcornTheme.colors.iconPrimary
                                },
                            )
                        }
                    }
                }
            }
        }

        is BrowserToolbarMenuDivider -> {
            @Composable {
                Divider(
                    color = AcornTheme.colors.borderSecondary,
                )
            }
        }
    }
}

@Composable
@ReadOnlyComposable
private fun BrowserToolbarMenuButton.text() = when (text) {
    is StringText -> text.text
    is StringResText -> stringResource(text.resourceId)
}

@Composable
@ReadOnlyComposable
private fun BrowserToolbarMenuButton.badgeText() = when (badgeText) {
    is StringText -> badgeText.text
    is StringResText -> stringResource(badgeText.resourceId)
    null -> ""
}

@Composable
@ReadOnlyComposable
private fun BrowserToolbarMenuButton.contentDescription() = when (contentDescription) {
    is StringContentDescription -> contentDescription.text
    is StringResContentDescription -> stringResource(contentDescription.resourceId)
}

@Composable
@ReadOnlyComposable
private fun BrowserToolbarMenuIconButton.contentDescription() = when (contentDescription) {
    is BrowserToolbarMenuIconButton.ContentDescription.StringContentDescription -> contentDescription.text
    is BrowserToolbarMenuIconButton.ContentDescription.StringResContentDescription -> stringResource(contentDescription.resourceId)
}
