/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class WalletCreatedData(
    val address: String,
    val mnemonic: String
)

@Composable
fun WalletCreatedDialog(
    walletData: WalletCreatedData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var copyButtonText by remember { mutableStateOf("Copy All Information") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Wallet Created Successfully",
                style = ElementTheme.typography.fontHeadingMdBold,
                color = ElementTheme.colors.textPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "MyWallet has been created!",
                    style = ElementTheme.typography.fontBodyLgMedium,
                    color = ElementTheme.colors.textPrimary
                )
                
                Text(
                    text = "You can use the same wallet across all DAOs.",
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = ElementTheme.colors.textSecondary
                )

                // Wallet Address Section
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = ElementTheme.colors.bgSubtleSecondary
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Wallet Address:",
                            style = ElementTheme.typography.fontBodySmMedium,
                            color = ElementTheme.colors.textSecondary
                        )
                        Text(
                            text = walletData.address,
                            style = ElementTheme.typography.fontBodySmRegular,
                            color = ElementTheme.colors.textPrimary,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    ElementTheme.colors.bgCanvasDefault,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(12.dp)
                        )
                    }
                }

                // Mnemonic Section
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = ElementTheme.colors.bgSubtleSecondary
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Mnemonic:",
                            style = ElementTheme.typography.fontBodySmMedium,
                            color = ElementTheme.colors.textSecondary
                        )
                        Text(
                            text = walletData.mnemonic,
                            style = ElementTheme.typography.fontBodySmRegular,
                            color = ElementTheme.colors.textPrimary,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    ElementTheme.colors.bgCanvasDefault,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(12.dp)
                        )
                    }
                }

                Text(
                    text = "Please keep your mnemonic phrase in a safe place.",
                    style = ElementTheme.typography.fontBodySmRegular,
                    color = ElementTheme.colors.textSecondary,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val backupData = "Mnemonic: ${walletData.mnemonic}\nAddress: ${walletData.address}"
                    clipboardManager.setText(AnnotatedString(backupData))
                    copyButtonText = "Copied!"
                    // Reset button text after 2 seconds
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(2000)
                        copyButtonText = "Copy All Information"
                    }
                }
            ) {
                Text(copyButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        modifier = modifier
    )
}

@PreviewsDayNight
@Composable
internal fun WalletCreatedDialogPreview() = ElementPreview {
    WalletCreatedDialog(
        walletData = WalletCreatedData(
            address = "0x1234567890abcdef1234567890abcdef12345678",
            mnemonic = "word1 word2 word3 word4 word5 word6 word7 word8 word9 word10 word11 word12"
        ),
        onDismiss = {}
    )
}
