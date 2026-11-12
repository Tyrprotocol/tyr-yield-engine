// File: ReferralScreen.kt
package com.example.ui.screens

import android.content.Intent
import androidx.core.net.toUri
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReferralEndpoint
import com.example.data.model.ReferralRegistry
import com.example.ui.theme.CyanCore
import com.example.ui.theme.EmeraldYieldBright
import com.example.ui.theme.EmeraldYieldPrimary
import com.example.ui.theme.GoldAlert
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TerminalVoid
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ReferralScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val endpoints = ReferralRegistry.REFERRAL_CONFIG

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalVoid)
            .testTag("referral_screen")
    ) {
        // Scrollable List of 12 Referral Cards
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(EmeraldYieldPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = EmeraldYieldBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Verified Partner Hub & Tip Jar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "12 Brokerage welcome bonuses & tools (REF-01 to REF-12)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // 12 Referral Endpoints
            items(endpoints, key = { it.id }) { endpoint ->
                ReferralEndpointCard(
                    endpoint = endpoint,
                    onOpen = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, endpoint.directUrl.toUri())
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot open URL: ${endpoint.directUrl}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // MANDATORY FTC DISCLOSURE PINNED TO THE BOTTOM OF THE SCREEN
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalSurfaceVariant)
                .border(1.dp, TerminalCardBorder)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("pinned_ftc_disclosure_bottom")
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "FTC Disclosure",
                    tint = GoldAlert,
                    modifier = Modifier
                        .size(15.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = ReferralRegistry.PINNED_FTC_DISCLOSURE,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun ReferralEndpointCard(
    endpoint: ReferralEndpoint,
    onOpen: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TerminalSurface)
            .border(
                1.dp,
                if (endpoint.isPrimaryPartner) EmeraldYieldPrimary.copy(alpha = 0.6f) else TerminalCardBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onOpen)
            .padding(14.dp)
            .testTag("referral_card_${endpoint.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(CyanCore.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = endpoint.id,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanCore
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = endpoint.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .background(EmeraldYieldPrimary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = endpoint.tag,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldYieldBright
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = endpoint.category,
                fontSize = 11.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = endpoint.bonusDescription,
                fontSize = 12.sp,
                color = TextPrimary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Claim Welcome Offer",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldYieldPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = EmeraldYieldPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
