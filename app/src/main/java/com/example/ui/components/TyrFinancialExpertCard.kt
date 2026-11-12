// File: TyrFinancialExpertCard.kt
package com.example.ui.components

import android.content.Intent
import androidx.core.net.toUri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.PortfolioStanding
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
import java.util.Locale

private const val TYR_PROTOCOL_URL = "https://tyrprotocol.pro"

/**
 * Prominent Financial Expert & Deep Portfolio Auditor Card powered by Týr Protocol.
 * Features live sustainability & tax scores, expert grade, and direct launch to https://tyrprotocol.pro.
 */
@Composable
fun TyrFinancialExpertCard(
    standing: PortfolioStanding,
    onConsultAdvisor: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val openTyrIntent = remember {
        Intent(Intent.ACTION_VIEW, TYR_PROTOCOL_URL.toUri())
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TerminalSurface)
            .border(1.dp, EmeraldYieldPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("tyr_financial_expert_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // Header: Protocol Brand & Verified Institutional Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(EmeraldYieldPrimary.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .border(1.dp, EmeraldYieldPrimary.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Týr Shield",
                            tint = EmeraldYieldBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "TÝR PROTOCOL",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PurpleTech.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "FINANCIAL EXPERT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PurpleTech
                                )
                            }
                        }
                        Text(
                            text = "Closed-Loop Sovereign Yield Auditor",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // External Protocol Link
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TerminalSurfaceVariant)
                        .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            try {
                                context.startActivity(openTyrIntent)
                            } catch (_: Exception) {}
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("tyr_protocol_link_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "tyrprotocol.pro",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanCore
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open Tyr Protocol",
                        tint = CyanCore,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // Expert Grade & Verdict Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TerminalSurfaceVariant)
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = EmeraldYieldBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PORTFOLIO STANDING RATING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = standing.financialExpertGrade,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = EmeraldYieldBright
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = standing.financialExpertVerdict,
                        fontSize = 11.sp,
                        color = TextPrimary,
                        lineHeight = 15.sp
                    )
                }
            }

            // Core Analytical Focus Metrics: Sustainability, Tax Efficiency, Defense Ratio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExpertMetricTile(
                    label = "YIELD SUSTAINABILITY",
                    value = "${standing.yieldSustainabilityScore}/100",
                    sub = if (standing.yieldSustainabilityScore >= 90) "Low Payout Drag" else "Moderate Drag",
                    accentColor = EmeraldYieldBright,
                    modifier = Modifier.weight(1f)
                )

                ExpertMetricTile(
                    label = "TAX EFFICIENCY",
                    value = String.format(Locale.US, "%.0f%%", standing.taxEfficiencyScore),
                    sub = "Sec 1256 / ROC / SGOV",
                    accentColor = CyanCore,
                    modifier = Modifier.weight(1f)
                )

                ExpertMetricTile(
                    label = "DEFENSE RATIO",
                    value = String.format(Locale.US, "%.2fx", standing.defenseToRiskRatio),
                    sub = if (standing.defenseToRiskRatio >= 1.25) "Target Satisfied" else "Requires Sweep",
                    accentColor = if (standing.defenseToRiskRatio >= 1.25) EmeraldYieldBright else GoldAlert,
                    modifier = Modifier.weight(1f)
                )
            }

            // Consult Týr Financial Expert CTA Button
            Button(
                onClick = onConsultAdvisor,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldYieldPrimary,
                    contentColor = TerminalVoid
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("consult_tyr_expert_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Consult Týr Financial Expert & AI Auditor",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpertMetricTile(
    label: String,
    value: String,
    sub: String,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(TerminalSurfaceVariant)
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = TextSecondary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sub,
                fontSize = 9.sp,
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}
