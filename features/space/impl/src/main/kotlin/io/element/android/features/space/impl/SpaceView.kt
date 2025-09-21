/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import android.widget.Toast
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.ui.components.DAOWalletCard
import io.element.android.libraries.matrix.ui.components.DAOWalletData
import io.element.android.libraries.matrix.ui.components.SpaceHeaderView
import io.element.android.libraries.matrix.ui.components.SpaceRoomItemView
import io.element.android.libraries.matrix.ui.components.WalletCreatedDialog
import io.element.android.libraries.matrix.ui.components.WalletCreatedData
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.cryptography.impl.MnemonicGenerator
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay

/**
 * Check if a space is a DAO by looking for GOV and DCA subspaces
 */
private fun isDAOSpace(space: SpaceRoom, children: List<SpaceRoom>): Boolean {
    val subspaces = children.filter { it.roomId != space.roomId }
    val hasGOV = subspaces.any { it.name == "GOV" }
    val hasDCA = subspaces.any { it.name == "DCA" }
    return hasGOV && hasDCA
}

@Composable
fun SpaceView(
    state: SpaceState,
    onBackClick: () -> Unit,
    onRoomClick: (roomId: RoomId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            SpaceViewTopBar(currentSpace = state.currentSpace, onBackClick = onBackClick)
        },
        content = { padding ->
            Box(
                modifier = Modifier.padding(padding)
            ) {
                SpaceViewContent(
                    state = state,
                    onRoomClick = onRoomClick
                )
            }
        },
    )
}

@Composable
private fun SpaceViewContent(
    state: SpaceState,
    onRoomClick: (roomId: RoomId) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier.fillMaxSize()) {
        val currentSpace = state.currentSpace
        if (currentSpace != null) {
            item {
                SpaceHeaderView(
                    avatarData = currentSpace.getAvatarData(AvatarSize.SpaceHeader),
                    name = currentSpace.name,
                    topic = currentSpace.topic,
                    joinRule = currentSpace.joinRule,
                    heroes = currentSpace.heroes.toImmutableList(),
                    numberOfMembers = currentSpace.numJoinedMembers,
                    numberOfRooms = currentSpace.childrenCount,
                )
            }
            
            // Check if this is a DAO space and add DAO wallet card
            if (isDAOSpace(currentSpace, state.children)) {
                item {
                    DAOWalletCardWithState(currentSpace = currentSpace)
                }
            }
        }
        state.children.forEach { spaceRoom ->
            item {
                val isInvitation = spaceRoom.state == CurrentUserMembership.INVITED
                SpaceRoomItemView(
                    spaceRoom = spaceRoom,
                    showUnreadIndicator = isInvitation && spaceRoom.roomId !in state.seenSpaceInvites,
                    hideAvatars = isInvitation && state.hideInvitesAvatar,
                    onClick = {
                        onRoomClick(spaceRoom.roomId)
                    },
                    onLongClick = {
                        // TODO
                    }
                )
            }
        }
        if (state.hasMoreToLoad) {
            item {
                LoadingMoreIndicator(eventSink = state.eventSink)
            }
        }
    }
}

@Composable
private fun LoadingMoreIndicator(
    eventSink: (SpaceEvents) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            strokeWidth = 2.dp,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        val latestEventSink by rememberUpdatedState(eventSink)
        LaunchedEffect(Unit) {
            latestEventSink(SpaceEvents.LoadMore)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpaceViewTopBar(
    currentSpace: SpaceRoom?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            BackButton(onClick = onBackClick)
        },
        title = {
            if (currentSpace != null) {
                SpaceAvatarAndNameRow(
                    name = currentSpace.name,
                    avatarData = currentSpace.getAvatarData(AvatarSize.TimelineRoom),
                )
            }
        },
        actions = {
        },
    )
}

@Composable
private fun SpaceAvatarAndNameRow(
    name: String?,
    avatarData: AvatarData,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            avatarData = avatarData,
            avatarType = AvatarType.Space(),
        )
        Text(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .semantics {
                    heading()
                },
            text = name ?: stringResource(CommonStrings.common_no_room_name),
            style = ElementTheme.typography.fontBodyLgMedium,
            fontStyle = FontStyle.Italic.takeIf { name == null },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DAOWalletCardWithState(currentSpace: SpaceRoom) {
    var walletData by remember { mutableStateOf<DAOWalletData?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var createWalletTrigger by remember { mutableStateOf(0) }
    var restoreWalletTrigger by remember { mutableStateOf(0) }
    var restoreMnemonic by remember { mutableStateOf("") }
    var showWalletCreatedDialog by remember { mutableStateOf(false) }
    var createdWalletData by remember { mutableStateOf<WalletCreatedData?>(null) }
    val context = LocalContext.current

    // Handle wallet creation
    LaunchedEffect(createWalletTrigger) {
        if (createWalletTrigger > 0) {
            isLoading = true
            error = null
            
            try {
                delay(2000) // Simulate network delay
                
                val mnemonic = MnemonicGenerator.generateMnemonic()
                val address = MnemonicGenerator.generateAddressFromMnemonic(mnemonic)
                
                // Create mock wallet data
                walletData = DAOWalletData(
                    address = address,
                    balance = 1000000,
                    currency = "B",
                    mnemonic = mnemonic
                )
                
                // Show wallet created dialog
                createdWalletData = WalletCreatedData(
                    address = address,
                    mnemonic = mnemonic
                )
                showWalletCreatedDialog = true
                
            } catch (e: Exception) {
                error = "Failed to create wallet: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Handle wallet restoration
    LaunchedEffect(restoreWalletTrigger) {
        if (restoreWalletTrigger > 0) {
            isLoading = true
            error = null
            
            try {
                delay(1500) // Simulate network delay
                
                // Validate mnemonic
                if (!MnemonicGenerator.isValidMnemonic(restoreMnemonic)) {
                    error = "Invalid mnemonic phrase"
                    return@LaunchedEffect
                }
                
                // Create mock wallet data from mnemonic
                val address = MnemonicGenerator.generateAddressFromMnemonic(restoreMnemonic)
                walletData = DAOWalletData(
                    address = address,
                    balance = 500000,
                    currency = "B",
                    mnemonic = restoreMnemonic
                )
                
                Toast.makeText(context, "Wallet restored successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                error = "Failed to restore wallet: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    DAOWalletCard(
        space = currentSpace,
        walletData = walletData,
        isLoading = isLoading,
        error = error,
        onCreateWallet = {
            createWalletTrigger++
        },
        onRestoreWallet = { mnemonic ->
            restoreMnemonic = mnemonic
            restoreWalletTrigger++
        },
        onSendClick = {
            Toast.makeText(context, "Send functionality - Coming soon!", Toast.LENGTH_SHORT).show()
        },
        onHistoryClick = {
            Toast.makeText(context, "Transaction history - Coming soon!", Toast.LENGTH_SHORT).show()
        },
        onExportWallet = {
            Toast.makeText(context, "Export wallet - Coming soon!", Toast.LENGTH_SHORT).show()
        },
        onDeleteWallet = {
            walletData = null
            Toast.makeText(context, "Wallet deleted", Toast.LENGTH_SHORT).show()
        },
        onShowQRCode = {
            Toast.makeText(context, "QR Code - Coming soon!", Toast.LENGTH_SHORT).show()
        }
    )

    // Show wallet created dialog
    if (showWalletCreatedDialog && createdWalletData != null) {
        WalletCreatedDialog(
            walletData = createdWalletData!!,
            onDismiss = {
                showWalletCreatedDialog = false
                createdWalletData = null
            }
        )
    }
}

@PreviewsDayNight
@Composable
internal fun SpaceViewPreview(
    @PreviewParameter(SpaceStateProvider::class) state: SpaceState
) = ElementPreview {
    SpaceView(
        state = state,
        onRoomClick = {},
        onBackClick = {},
    )
}
