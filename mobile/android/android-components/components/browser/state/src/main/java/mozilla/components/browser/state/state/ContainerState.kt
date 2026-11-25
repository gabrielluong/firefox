/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.browser.state.state

/**
 * Value type that represents the state of a container also known as a contextual identity.
 *
 * @property contextId The session context ID also known as cookie store ID for the container.
 * @property label The [Label] of the container.
 * @property color The color for the container. This can be shown in tabs belonging to this container.
 * @property icon The icon for the container.
 */
data class ContainerState(
    val contextId: String,
    val label: Label,
    val color: Color,
    val icon: Icon,
) {
    /**
     * How a container gets its label. The shipped default containers carry a well-known label that
     * consumers localize, until the user renames them.
     */
    sealed interface Label {
        /** A container with a name of its own, either created or renamed by the user. */
        data class Custom(val name: String) : Label

        /** The shipped "Personal" container. */
        data object Personal : Label

        /** The shipped "Work" container. */
        data object Work : Label

        /** The shipped "Banking" container. */
        data object Banking : Label

        /** The shipped "Shopping" container. */
        data object Shopping : Label
    }

    /** Enum of container colors, in the order a container editor offers them. */
    enum class Color {
        GRAY,
        YELLOW,
        ORANGE,
        RED,
        PINK,
        PURPLE,
        VIOLET,
        BLUE,
        CYAN,
        GREEN,
    }

    /** Enum of container icons, in the order a container editor offers them. */
    enum class Icon {
        FINGERPRINT,
        BRIEFCASE,
        DOLLAR,
        CART,
        VACATION,
        GIFT,
        FOOD,
        FRUIT,
        PET,
        TREE,
        CHILL,
        CIRCLE,
        FENCE,
        ROBOT,
    }
}
