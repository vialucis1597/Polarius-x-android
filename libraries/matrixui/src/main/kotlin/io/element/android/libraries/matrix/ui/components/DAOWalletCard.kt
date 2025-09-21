/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomType
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.ui.strings.CommonStrings

data class DAOWalletData(
    val address: String,
    val balance: Long,
    val currency: String = "B",
    val mnemonic: String = ""
)

@Composable
fun DAOWalletCard(
    space: SpaceRoom,
    walletData: DAOWalletData?,
    isLoading: Boolean = false,
    error: String? = null,
    onCreateWallet: () -> Unit,
    onRestoreWallet: (String) -> Unit,
    onSendClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onExportWallet: () -> Unit,
    onDeleteWallet: () -> Unit,
    onShowQRCode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = ElementTheme.colors.bgCanvasDefault
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (walletData == null) {
                DAOWalletSetupSection(
                    space = space,
                    isLoading = isLoading,
                    error = error,
                    onCreateWallet = onCreateWallet,
                    onRestoreWallet = onRestoreWallet
                )
            } else {
                DAOWalletInfoSection(
                    space = space,
                    walletData = walletData,
                    onSendClick = onSendClick,
                    onHistoryClick = onHistoryClick,
                    onExportWallet = onExportWallet,
                    onDeleteWallet = onDeleteWallet,
                    onShowQRCode = onShowQRCode
                )
            }
        }
    }
}

@Composable
private fun DAOWalletSetupSection(
    space: SpaceRoom,
    isLoading: Boolean,
    error: String?,
    onCreateWallet: () -> Unit,
    onRestoreWallet: (String) -> Unit
) {
    var showMnemonicInput by remember { mutableStateOf(false) }
    var mnemonic by remember { mutableStateOf("") }
    var mnemonicError by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Header
        Text(
            text = "DAO Wallet",
            style = ElementTheme.typography.fontHeadingMdBold,
            color = ElementTheme.colors.textPrimary
        )
        Text(
            text = "Create or restore your dedicated DAO wallet",
            style = ElementTheme.typography.fontBodyMdRegular,
            color = ElementTheme.colors.textSecondary
        )

        // Error message
        error?.let { errorMsg ->
            Text(
                text = errorMsg,
                style = ElementTheme.typography.fontBodySmRegular,
                color = ElementTheme.colors.textCriticalPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        ElementTheme.colors.bgCriticalSubtle,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(8.dp)
            )
        }

        if (!showMnemonicInput) {
            // Create and Restore buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCreateWallet,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Create New Wallet")
                    }
                }

                OutlinedButton(
                    onClick = { showMnemonicInput = true },
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Restore Existing Wallet")
                }
            }
        } else {
            // Mnemonic input section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = mnemonic,
                    onValueChange = { 
                        mnemonic = it
                        mnemonicError = null
                    },
                    label = { Text("Mnemonic Phrase (12 words)") },
                    placeholder = { Text("abandon ability able about above absent...") },
                    isError = mnemonicError != null,
                    supportingText = mnemonicError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (mnemonic.trim().isBlank()) {
                                mnemonicError = "Please enter a mnemonic phrase"
                            } else {
                                onRestoreWallet(mnemonic.trim())
                            }
                        },
                        enabled = !isLoading && mnemonic.trim().isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Restore")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            showMnemonicInput = false
                            mnemonic = ""
                            mnemonicError = null
                        },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
private fun DAOWalletInfoSection(
    space: SpaceRoom,
    walletData: DAOWalletData,
    onSendClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onExportWallet: () -> Unit,
    onDeleteWallet: () -> Unit,
    onShowQRCode: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Header
        Text(
            text = "Balance: ${space.name} DAO",
            style = ElementTheme.typography.fontHeadingMdBold,
            color = ElementTheme.colors.textPrimary
        )

        // Address section
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Address",
                style = ElementTheme.typography.fontBodySmMedium,
                color = ElementTheme.colors.textSecondary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        ElementTheme.colors.bgCanvasDefault,
                        RoundedCornerShape(4.dp)
                    )
                    .border(
                        1.dp,
                        ElementTheme.colors.borderInteractiveSecondary,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = walletData.address,
                    style = ElementTheme.typography.fontBodySmRegular,
                    color = ElementTheme.colors.textPrimary,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(walletData.address))
                    }
                ) {
                    Text("Copy")
                }
            }
        }

        // Balance section
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Balance",
                style = ElementTheme.typography.fontBodySmMedium,
                color = ElementTheme.colors.textSecondary
            )
            Text(
                text = "${formatCurrency(walletData.balance)} ${walletData.currency}",
                style = ElementTheme.typography.fontHeadingLgBold,
                color = ElementTheme.colors.textSuccessPrimary
            )
        }


        // Action buttons - First row (utility buttons)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(onClick = onExportWallet) {
                Text("Backup")
            }
            TextButton(
                onClick = onDeleteWallet,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ElementTheme.colors.textCriticalPrimary
                )
            ) {
                Text("Delete")
            }
            TextButton(onClick = onShowQRCode) {
                Text("QR")
            }
            Spacer(modifier = Modifier.weight(1f))
        }

        // Action buttons - Second row (main actions)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onHistoryClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("History")
            }
            Button(
                onClick = onSendClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Send")
            }
        }
    }
}

private fun formatCurrency(amount: Long): String {
    return String.format("%,d", amount)
}

@PreviewsDayNight
@Composable
internal fun DAOWalletCardPreview() = ElementPreview {
    val mockSpace = SpaceRoom(
        name = "Test DAO",
        avatarUrl = null,
        canonicalAlias = null,
        childrenCount = 0,
        guestCanJoin = false,
        heroes = emptyList(),
        joinRule = null,
        numJoinedMembers = 0,
        roomId = RoomId("!test:example.com"),
        roomType = RoomType.Space,
        state = null,
        topic = "Test DAO description",
        worldReadable = false
    )

    Column {
        // Setup state
        DAOWalletCard(
            space = mockSpace,
            walletData = null,
            isLoading = false,
            error = null,
            onCreateWallet = {},
            onRestoreWallet = {},
            onSendClick = {},
            onHistoryClick = {},
            onExportWallet = {},
            onDeleteWallet = {},
            onShowQRCode = {}
        )

        Spacer(modifier = Modifier.height(16.dp))

        // With wallet data
        DAOWalletCard(
            space = mockSpace,
            walletData = DAOWalletData(
                address = "0x1234567890abcdef1234567890abcdef12345678",
                balance = 1000000,
                currency = "B",
                mnemonic = "abandon ability able about above absent absorb abstract absurd abuse access accident"
            ),
            isLoading = false,
            error = null,
            onCreateWallet = {},
            onRestoreWallet = {},
            onSendClick = {},
            onHistoryClick = {},
            onExportWallet = {},
            onDeleteWallet = {},
            onShowQRCode = {}
        )
    }
}
