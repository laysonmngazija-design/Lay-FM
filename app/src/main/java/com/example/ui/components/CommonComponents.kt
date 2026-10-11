package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountStatus
import com.example.data.model.LoanStatus
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    fun formatTsh(amount: Long): String {
        return "TSh %,d".format(amount)
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp <= 0L) return "-"
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("sw", "TZ"))
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        if (timestamp <= 0L) return "-"
        val sdf = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH)
        return sdf.format(Date(timestamp))
    }

    fun formatTimeOnly(timestamp: Long): String {
        if (timestamp <= 0L) return "-"
        val sdf = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        return sdf.format(Date(timestamp))
    }
}

object SmsHelper {
    fun launchSmsComposer(context: Context, phoneNumber: String, messageText: String = "") {
        try {
            val cleanPhone = phoneNumber.replace("[^0-9+]".toRegex(), "")
            val uri = Uri.parse("smsto:$cleanPhone")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                if (messageText.isNotBlank()) {
                    putExtra("sms_body", messageText)
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Toast.makeText(context, "Kifungua ujumbe (SMS) kimefunguliwa", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Haikuweza kufungua SMS: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}

object CommunicationHelper {
    fun makePhoneCall(context: Context, phoneNumber: String) {
        try {
            val clean = phoneNumber.replace("[^0-9+]".toRegex(), "")
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Haikuweza kufungua simu: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWhatsApp(context: Context, phoneNumber: String, defaultMessage: String = "") {
        try {
            var clean = phoneNumber.replace("[^0-9]".toRegex(), "")
            if (clean.startsWith("0")) {
                clean = "255" + clean.substring(1)
            } else if (!clean.startsWith("255") && clean.length == 9) {
                clean = "255$clean"
            }
            val uriString = if (defaultMessage.isNotBlank()) {
                "https://api.whatsapp.com/send?phone=$clean&text=${Uri.encode(defaultMessage)}"
            } else {
                "https://api.whatsapp.com/send?phone=$clean"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp haikupatikana", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun QuickContactButtonsRow(
    phoneNumber: String,
    contactName: String,
    modifier: Modifier = Modifier,
    defaultMessage: String = ""
) {
    val context = LocalContext.current
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Call button
        IconButton(
            onClick = { CommunicationHelper.makePhoneCall(context, phoneNumber) },
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(LayNavyPrimary.copy(alpha = 0.1f))
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Piga Simu $contactName",
                tint = LayNavyPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        // SMS button
        IconButton(
            onClick = { SmsHelper.launchSmsComposer(context, phoneNumber, defaultMessage) },
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(LayNavyLight.copy(alpha = 0.1f))
        ) {
            Icon(
                imageVector = Icons.Default.ChatBubble,
                contentDescription = "Tuma SMS kwa $contactName",
                tint = LayNavyLight,
                modifier = Modifier.size(18.dp)
            )
        }

        // WhatsApp button
        IconButton(
            onClick = { CommunicationHelper.openWhatsApp(context, phoneNumber, defaultMessage) },
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(LayGreenDark.copy(alpha = 0.12f))
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "WhatsApp $contactName",
                tint = LayGreenDark,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color = LayGreenPrimary,
    bgColor: Color = Color.White,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = LayTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = LayTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun HeroDisbursedCard(
    amount: Long,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LayGreenDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Total Principal Disbursed",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = Formatters.formatTsh(amount),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .align(Alignment.CenterEnd)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
fun LoanStatusBadge(
    status: LoanStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        LoanStatus.PENDING -> Triple(StatusPendingBg, StatusPending, "Pending")
        LoanStatus.APPROVED -> Triple(StatusApprovedBg, StatusApproved, "Approved")
        LoanStatus.DISBURSED -> Triple(StatusDisbursedBg, StatusDisbursed, "Disbursed")
        LoanStatus.ACTIVE -> Triple(StatusActiveBg, StatusActive, "Active")
        LoanStatus.OVERDUE -> Triple(StatusOverdueBg, StatusOverdue, "Overdue")
        LoanStatus.DECLINED -> Triple(StatusDeclinedBg, StatusDeclined, "Declined")
        LoanStatus.COMPLETED -> Triple(StatusCompletedBg, StatusCompleted, "Completed")
        LoanStatus.DRAFT -> Triple(LaySurfaceSubtle, LayTextSecondary, "Draft")
        LoanStatus.CANCELLED -> Triple(LaySurfaceSubtle, LayTextSecondary, "Cancelled")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun StatusBadge(
    status: AccountStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        AccountStatus.ACTIVE -> Triple(StatusActiveBg, StatusActive, "Active")
        AccountStatus.INACTIVE -> Triple(StatusOverdueBg, StatusOverdue, "Inactive")
        AccountStatus.SUSPENDED -> Triple(StatusOverdueBg, StatusOverdue, "Suspended")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun UserAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 40,
    backgroundColor: Color = LayNavyPrimary,
    textColor: Color = Color.White
) {
    val initials = name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")
        .ifEmpty { "LM" }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = textColor,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = (size * 0.4).sp
            )
        )
    }
}
