/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.containers

import mozilla.appservices.fxcontainers.Container
import mozilla.appservices.fxcontainers.ContainerColor
import mozilla.appservices.fxcontainers.ContainerIcon
import mozilla.appservices.fxcontainers.ContainerLabel
import mozilla.components.browser.state.state.ContainerState

/**
 * The context ID of the container with the given user context ID. GeckoView reports it to WebExtensions as the
 * `firefox-container-<contextId>` cookie store ID, so a numeric context ID matches the one Firefox Desktop uses.
 */
internal fun UInt.toContextId(): String = toString()

internal fun String.toUserContextId(): UInt? = toUIntOrNull()

internal fun Container.toContainerState() =
    ContainerState(
        contextId = userContextId.toContextId(),
        label = label.toLabel(),
        color = color?.toColor() ?: ContainerState.Color.GRAY,
        icon = icon?.toIcon() ?: ContainerState.Icon.CIRCLE,
    )

private fun ContainerLabel.toLabel(): ContainerState.Label =
    when (this) {
        is ContainerLabel.Name -> ContainerState.Label.Custom(name)
        ContainerLabel.Personal -> ContainerState.Label.Personal
        ContainerLabel.Work -> ContainerState.Label.Work
        ContainerLabel.Banking -> ContainerState.Label.Banking
        ContainerLabel.Shopping -> ContainerState.Label.Shopping
    }

private fun ContainerColor.toColor(): ContainerState.Color =
    when (this) {
        ContainerColor.GRAY -> ContainerState.Color.GRAY
        ContainerColor.YELLOW -> ContainerState.Color.YELLOW
        ContainerColor.ORANGE -> ContainerState.Color.ORANGE
        ContainerColor.RED -> ContainerState.Color.RED
        ContainerColor.PINK -> ContainerState.Color.PINK
        ContainerColor.PURPLE -> ContainerState.Color.PURPLE
        ContainerColor.VIOLET -> ContainerState.Color.VIOLET
        ContainerColor.BLUE -> ContainerState.Color.BLUE
        ContainerColor.CYAN -> ContainerState.Color.CYAN
        ContainerColor.GREEN -> ContainerState.Color.GREEN
    }

private fun ContainerIcon.toIcon(): ContainerState.Icon =
    when (this) {
        ContainerIcon.FINGERPRINT -> ContainerState.Icon.FINGERPRINT
        ContainerIcon.BRIEFCASE -> ContainerState.Icon.BRIEFCASE
        ContainerIcon.DOLLAR -> ContainerState.Icon.DOLLAR
        ContainerIcon.CART -> ContainerState.Icon.CART
        ContainerIcon.VACATION -> ContainerState.Icon.VACATION
        ContainerIcon.GIFT -> ContainerState.Icon.GIFT
        ContainerIcon.FOOD -> ContainerState.Icon.FOOD
        ContainerIcon.FRUIT -> ContainerState.Icon.FRUIT
        ContainerIcon.PET -> ContainerState.Icon.PET
        ContainerIcon.TREE -> ContainerState.Icon.TREE
        ContainerIcon.CHILL -> ContainerState.Icon.CHILL
        ContainerIcon.CIRCLE -> ContainerState.Icon.CIRCLE
        ContainerIcon.FENCE -> ContainerState.Icon.FENCE
        ContainerIcon.ROBOT -> ContainerState.Icon.ROBOT
    }

internal fun ContainerState.Color.toContainerColor(): ContainerColor =
    when (this) {
        ContainerState.Color.GRAY -> ContainerColor.GRAY
        ContainerState.Color.YELLOW -> ContainerColor.YELLOW
        ContainerState.Color.ORANGE -> ContainerColor.ORANGE
        ContainerState.Color.RED -> ContainerColor.RED
        ContainerState.Color.PINK -> ContainerColor.PINK
        ContainerState.Color.PURPLE -> ContainerColor.PURPLE
        ContainerState.Color.VIOLET -> ContainerColor.VIOLET
        ContainerState.Color.BLUE -> ContainerColor.BLUE
        ContainerState.Color.CYAN -> ContainerColor.CYAN
        ContainerState.Color.GREEN -> ContainerColor.GREEN
    }

internal fun ContainerState.Icon.toContainerIcon(): ContainerIcon =
    when (this) {
        ContainerState.Icon.FINGERPRINT -> ContainerIcon.FINGERPRINT
        ContainerState.Icon.BRIEFCASE -> ContainerIcon.BRIEFCASE
        ContainerState.Icon.DOLLAR -> ContainerIcon.DOLLAR
        ContainerState.Icon.CART -> ContainerIcon.CART
        ContainerState.Icon.VACATION -> ContainerIcon.VACATION
        ContainerState.Icon.GIFT -> ContainerIcon.GIFT
        ContainerState.Icon.FOOD -> ContainerIcon.FOOD
        ContainerState.Icon.FRUIT -> ContainerIcon.FRUIT
        ContainerState.Icon.PET -> ContainerIcon.PET
        ContainerState.Icon.TREE -> ContainerIcon.TREE
        ContainerState.Icon.CHILL -> ContainerIcon.CHILL
        ContainerState.Icon.CIRCLE -> ContainerIcon.CIRCLE
        ContainerState.Icon.FENCE -> ContainerIcon.FENCE
        ContainerState.Icon.ROBOT -> ContainerIcon.ROBOT
    }
