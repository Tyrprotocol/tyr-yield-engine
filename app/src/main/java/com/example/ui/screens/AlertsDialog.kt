// File: AlertsDialog.kt
package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AlertNotification
import com.example.data.model.AlertRule
import com.example.data.model.AlertType
import com.example.ui.theme.CyanCore
import com.example.ui.theme.EmeraldYieldBright
import com.example.ui.theme.EmeraldYieldPrimary
import com.example.ui.theme.GoldAlert
import com.example.ui.theme.RedRisk
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TerminalVoid
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlertsDialog(
    rules: List<AlertRule>,
    notifications: List<AlertNotification>,
    onAddRule: (AlertRule) -> Unit,
    onToggleRule: (AlertRule) -> Unit,
    onDeleteRule: (AlertRule) -> Unit,
    onClearNotifications: () -> Unit,
    onDismiss: () -> Unit
) {
    var isAddingRule by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, TerminalCardBorder, RoundedCornerShape(20.dp)),
            color = TerminalVoid
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .testTag("alerts_dialog")
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(GoldAlert.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = GoldAlert,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Engine Alert System",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${rules.count { it.isEnabled }} active rules • ${notifications.size} events",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Triggered Notifications Feed
                    if (notifications.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "RECENT ALERTS FEED",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = TextSecondary
                                )
                                TextButton(onClick = onClearNotifications) {
                                    Text("Clear All", fontSize = 11.sp, color = CyanCore)
                                }
                            }
                        }

                        items(notifications) { notif ->
                            val dateStr = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(notif.timestamp))
                            val accentColor = when (notif.severity) {
                                "SUCCESS" -> EmeraldYieldBright
                                "WARNING" -> GoldAlert
                                "ALERT" -> RedRisk
                                else -> CyanCore
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(TerminalSurface)
                                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = notif.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor
                                        )
                                        Text(text = dateStr, fontSize = 10.sp, color = TextSecondary)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = notif.message,
                                        fontSize = 11.sp,
                                        color = TextPrimary,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    // Configured Alert Rules
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CONFIGURED ALERT RULES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = TextSecondary
                            )
                            TextButton(onClick = { isAddingRule = !isAddingRule }) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = EmeraldYieldPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Rule", fontSize = 11.sp, color = EmeraldYieldPrimary)
                            }
                        }
                    }

                    if (isAddingRule) {
                        item {
                            AddRuleCard(
                                onSave = { newRule ->
                                    onAddRule(newRule)
                                    isAddingRule = false
                                },
                                onCancel = { isAddingRule = false }
                            )
                        }
                    }

                    items(rules, key = { it.id }) { rule ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(TerminalSurface)
                                .border(1.dp, TerminalCardBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = rule.label,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${rule.type.name} • Threshold: ${rule.thresholdValue} (${rule.comparison})",
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = rule.isEnabled,
                                        onCheckedChange = { onToggleRule(rule) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = EmeraldYieldPrimary,
                                            checkedTrackColor = EmeraldYieldPrimary.copy(alpha = 0.3f),
                                            uncheckedThumbColor = TextSecondary,
                                            uncheckedTrackColor = TerminalSurfaceVariant
                                        )
                                    )
                                    IconButton(onClick = { onDeleteRule(rule) }, modifier = Modifier.size(32.dp)) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddRuleCard(
    onSave: (AlertRule) -> Unit,
    onCancel: () -> Unit
) {
    var selectedType by remember { mutableStateOf(AlertType.DEFENSE_RATIO_TARGET) }
    var targetTicker by remember { mutableStateOf("") }
    var thresholdInput by remember { mutableStateOf("1.25") }
    var labelInput by remember { mutableStateOf("Target Defense Ratio >= 1.25x") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TerminalSurfaceVariant)
            .border(1.dp, EmeraldYieldPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Create Trigger Rule", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldYieldBright)

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RuleTypeChip("Defense Ratio", selectedType == AlertType.DEFENSE_RATIO_TARGET) {
                    selectedType = AlertType.DEFENSE_RATIO_TARGET
                    thresholdInput = "1.25"
                    labelInput = "Defense Ratio >= 1.25x Reached"
                }
                RuleTypeChip("Portfolio +/- %", selectedType == AlertType.PORTFOLIO_CHANGE_PCT) {
                    selectedType = AlertType.PORTFOLIO_CHANGE_PCT
                    thresholdInput = "5.0"
                    labelInput = "Portfolio Shift 5%"
                }
                RuleTypeChip("Asset Price", selectedType == AlertType.ASSET_PRICE_MOVEMENT) {
                    selectedType = AlertType.ASSET_PRICE_MOVEMENT
                    targetTicker = "JEPQ"
                    thresholdInput = "56.00"
                    labelInput = "JEPQ Price Target $56.00"
                }
            }

            if (selectedType == AlertType.ASSET_PRICE_MOVEMENT) {
                OutlinedTextField(
                    value = targetTicker,
                    onValueChange = { targetTicker = it.uppercase() },
                    label = { Text("Ticker (e.g. JEPQ)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = thresholdInput,
                onValueChange = { thresholdInput = it },
                label = { Text("Threshold Value") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = labelInput,
                onValueChange = { labelInput = it },
                label = { Text("Alert Description") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) {
                    Text("Cancel", color = TextSecondary)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = {
                        val thresh = thresholdInput.toDoubleOrNull() ?: 1.0
                        onSave(
                            AlertRule(
                                type = selectedType,
                                targetTicker = if (selectedType == AlertType.ASSET_PRICE_MOVEMENT) targetTicker else null,
                                thresholdValue = thresh,
                                comparison = "GREATER_THAN",
                                label = labelInput.ifBlank { "Alert Rule" }
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldYieldPrimary, contentColor = TerminalVoid)
                ) {
                    Text("Add Rule")
                }
            }
        }
    }
}

@Composable
fun RuleTypeChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) EmeraldYieldPrimary.copy(alpha = 0.2f) else TerminalSurface)
            .border(1.dp, if (isSelected) EmeraldYieldPrimary else TerminalCardBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        TextButton(onClick = onClick, modifier = Modifier.height(28.dp)) {
            Text(text = text, fontSize = 10.sp, color = if (isSelected) EmeraldYieldBright else TextSecondary, fontWeight = FontWeight.Bold)
        }
    }
}
