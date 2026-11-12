// File: GeminiChatSheet.kt
package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.core.net.toUri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ActionableTradeRoute
import com.example.ai.ChatMessage
import com.example.ai.MessageSender
import com.example.ai.PortfolioStanding
import com.example.ui.components.ActionableRouteCard
import com.example.ui.components.AiStandingCard
import com.example.ui.theme.CyanCore
import com.example.ui.theme.EmeraldYieldBright
import com.example.ui.theme.EmeraldYieldPrimary
import com.example.ui.theme.GoldAlert
import com.example.ui.theme.PurpleTech
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TerminalVoid
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GeminiChatSheet(
    messages: List<ChatMessage>,
    isThinking: Boolean,
    standing: PortfolioStanding,
    onSendMessage: (String, Boolean) -> Unit,
    onExecuteRoute: (ActionableTradeRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var highThinkingEnabled by remember { mutableStateOf(true) } // Institutional high thinking default
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size)
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    val quickPrompts = remember(standing.unheldSectors) {
        val gapPrompt = if (standing.unheldSectors.isNotEmpty()) {
            "Fill my ${standing.unheldSectors.first().shortName} gap with highest-yield champion"
        } else {
            "Audit my multi-vector balance"
        }
        listOf(
            gapPrompt,
            "Audit yield sustainability & tax efficiency under Týr Protocol standard",
            "Audit my Defense-to-Risk ratio and stress test a 25% drop",
            "Calculate step-by-step sweep to maximize yield and defense",
            "Compare JEPQ vs SCHD for cash velocity",
            "How does DRIP=OFF maximize my capital efficiency?"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalVoid)
            .padding(horizontal = 14.dp)
            .testTag("gemini_chat_screen")
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        // Týr Protocol Institutional Financial Expert Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(TerminalSurface)
                .border(1.dp, EmeraldYieldPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .clickable {
                    try {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            "https://tyrprotocol.pro".toUri()
                        )
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("tyr_protocol_chat_banner")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Psychology,
                        contentDescription = null,
                        tint = EmeraldYieldBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TÝR PROTOCOL AUDITOR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${standing.financialExpertGrade}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldYieldBright
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "tyrprotocol.pro",
                        fontSize = 10.sp,
                        color = CyanCore,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = CyanCore,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Live Portfolio Standing Card at the top
        AiStandingCard(
            standing = standing,
            onQuickPromptClick = { prompt -> onSendMessage(prompt, highThinkingEnabled) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Model & High Thinking Toggle Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TerminalSurfaceVariant)
                .border(1.dp, TerminalCardBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                if (highThinkingEnabled) PurpleTech.copy(alpha = 0.2f) else EmeraldYieldPrimary.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (highThinkingEnabled) Icons.Default.Psychology else Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (highThinkingEnabled) PurpleTech else EmeraldYieldBright,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (highThinkingEnabled) "Gemini 4 Argon (High Thinking)" else "Gemini 3.8 Flash (Conversational)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (highThinkingEnabled) "Deep multi-vector standing & route inference" else "Fast response mode",
                            fontSize = 9.sp,
                            color = if (highThinkingEnabled) PurpleTech else EmeraldYieldBright
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Think",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = highThinkingEnabled,
                        onCheckedChange = { highThinkingEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PurpleTech,
                            checkedTrackColor = PurpleTech.copy(alpha = 0.3f),
                            uncheckedThumbColor = EmeraldYieldPrimary,
                            uncheckedTrackColor = TerminalSurface
                        ),
                        modifier = Modifier.testTag("high_thinking_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Suggestion Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickPrompts) { prompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TerminalSurface)
                        .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
                        .clickable { onSendMessage(prompt, highThinkingEnabled) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = prompt,
                        fontSize = 11.sp,
                        color = CyanCore,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Message Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(
                    message = msg,
                    onExecuteRoute = onExecuteRoute
                )
            }

            if (isThinking) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(TerminalSurfaceVariant)
                            .border(1.dp, PurpleTech.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = if (highThinkingEnabled) PurpleTech else EmeraldYieldPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (highThinkingEnabled) "Gemini 4 Argon is conducting high-thinking deep analysis..." else "Gemini is formulating response...",
                                fontSize = 11.sp,
                                color = if (highThinkingEnabled) PurpleTech else EmeraldYieldBright,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask about routes, standing, or gaps...", fontSize = 12.sp, color = TextSecondary) },
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (highThinkingEnabled) PurpleTech else EmeraldYieldPrimary,
                    unfocusedBorderColor = TerminalCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("gemini_chat_input")
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val text = inputText
                        inputText = ""
                        onSendMessage(text, highThinkingEnabled)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(if (highThinkingEnabled) PurpleTech else EmeraldYieldPrimary, RoundedCornerShape(12.dp))
                    .testTag("gemini_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = TerminalVoid
                )
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    onExecuteRoute: (ActionableTradeRoute) -> Unit
) {
    val isUser = message.sender == MessageSender.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bgColor = when (message.sender) {
        MessageSender.USER -> EmeraldYieldPrimary.copy(alpha = 0.15f)
        MessageSender.GEMINI_ARGON -> PurpleTech.copy(alpha = 0.12f)
        MessageSender.GEMINI_FLASH -> TerminalSurfaceVariant
        MessageSender.SYSTEM -> GoldAlert.copy(alpha = 0.1f)
    }
    val borderColor = when (message.sender) {
        MessageSender.USER -> EmeraldYieldPrimary.copy(alpha = 0.4f)
        MessageSender.GEMINI_ARGON -> PurpleTech.copy(alpha = 0.35f)
        MessageSender.GEMINI_FLASH -> TerminalCardBorder
        MessageSender.SYSTEM -> GoldAlert.copy(alpha = 0.3f)
    }
    val headerLabel = when (message.sender) {
        MessageSender.USER -> "You"
        MessageSender.GEMINI_ARGON -> "Gemini 4 Argon (High Thinking)"
        MessageSender.GEMINI_FLASH -> "Gemini 3.8 Flash"
        MessageSender.SYSTEM -> "System Advisory"
    }
    val headerColor = when (message.sender) {
        MessageSender.USER -> EmeraldYieldBright
        MessageSender.GEMINI_ARGON -> PurpleTech
        MessageSender.GEMINI_FLASH -> CyanCore
        MessageSender.SYSTEM -> GoldAlert
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.85f else 0.98f)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = headerLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = headerColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.text,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    lineHeight = 17.sp
                )

                // Optional Actionable Route Card if attached
                if (message.actionableRoute != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ActionableRouteCard(
                        route = message.actionableRoute,
                        onExecuteRoute = onExecuteRoute
                    )
                }
            }
        }
    }
}
