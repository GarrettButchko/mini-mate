package com.garrettbutchko.minimate.content.helper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.colorResource
import com.garrettbutchko.minimate.dataModels.playerModels.Player
import com.garrettbutchko.minimate.shared.R

/**
 * Watches a lobby's player list and reports arrivals/departures as a
 * [StatusMessage]. Both the host and the joining player show these, differing
 * only in whose game it is and which ids don't count as churn.
 *
 * @param possessive how the game is referred to in the subtitle ("your game" / "the game")
 * @param ignoredIds player ids that never produce a notice (e.g. the host themselves)
 * @param ignoreLocalGuests skip locally-added guests, which have 6-character ids —
 *        the host adds and removes those directly, so they're not news
 * @param selfId when set, notices are suppressed until this player is already in
 *        the list; the initial load right after joining otherwise floods it with everyone
 */
@Composable
fun LobbyChurnNotifier(
    players: List<Player>,
    enabled: Boolean,
    possessive: String,
    ignoredIds: Set<String> = emptySet(),
    ignoreLocalGuests: Boolean = false,
    selfId: String? = null,
    onNotice: (StatusMessage) -> Unit
) {
    val previous = remember { mutableStateOf(players) }
    val joinedTint = colorResource(id = R.color.swiftGreen)

    LaunchedEffect(players, enabled) {
        val old = previous.value
        previous.value = players
        if (!enabled) return@LaunchedEffect

        val oldIds = old.map { it.userId }.toSet()
        val newIds = players.map { it.userId }.toSet()

        if (selfId != null && selfId !in oldIds) return@LaunchedEffect

        fun isNewsworthy(player: Player): Boolean =
            player.userId !in ignoredIds &&
                player.userId != selfId &&
                !(ignoreLocalGuests && player.userId.length == 6)

        val joined = players.filter { it.userId !in oldIds && isNewsworthy(it) }
        val left = old.filter { it.userId !in newIds && isNewsworthy(it) }

        when {
            joined.isNotEmpty() -> onNotice(
                churnMessage(
                    names = joined.map { it.name },
                    verb = "Joined",
                    subtitleVerb = "joined",
                    possessive = possessive,
                    icon = Icons.Filled.PersonAdd,
                    tint = joinedTint
                )
            )

            left.isNotEmpty() -> onNotice(
                churnMessage(
                    names = left.map { it.name },
                    verb = "Left",
                    subtitleVerb = "left",
                    possessive = possessive,
                    icon = Icons.Filled.PersonRemove,
                    tint = null
                )
            )
        }
    }
}

private fun churnMessage(
    names: List<String>,
    verb: String,
    subtitleVerb: String,
    possessive: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color?
): StatusMessage = when (names.size) {
    1 -> StatusMessage(
        title = "${names[0]} $verb",
        subtitle = "${names[0]} $subtitleVerb $possessive.",
        icon = icon,
        tint = tint
    )

    2 -> StatusMessage(
        title = "2 $verb",
        subtitle = "${names[0]} & ${names[1]} $subtitleVerb $possessive.",
        icon = icon,
        tint = tint
    )

    else -> StatusMessage(
        title = "${names.size} $verb",
        subtitle = "${names.size} people $subtitleVerb $possessive.",
        icon = icon,
        tint = tint
    )
}
