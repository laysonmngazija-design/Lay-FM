package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.Loan
import com.example.data.model.LoanStatus
import com.example.data.model.Payment
import com.example.ui.theme.*

@Composable
fun CustomerHistoryDialog(
    customer: Customer,
    loans: List<Loan>,
    payments: List<Payment>,
    onDismiss: () -> Unit,
    onRecordPaymentClick: ((Loan) -> Unit)? = null
) {
    val context = LocalContext.current
    val customerLoans = loans.filter { it.customerId == customer.id }
    val customerPayments = payments.filter { it.customerId == customer.id }

    val activeLoan = customerLoans.find {
        it.status == LoanStatus.ACTIVE || it.status == LoanStatus.OVERDUE ||
                it.status == LoanStatus.PENDING || it.status == LoanStatus.APPROVED
    }

    val totalBorrowed = customerLoans.sumOf { it.principalAmount }
    val totalRepaid = customerPayments.sumOf { it.amount }
    val currentOutstanding = activeLoan?.remainingBalance ?: 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(name = customer.fullName, size = 50, backgroundColor = LayNavyPrimary)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.fullName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = LayNavyDark
                    )
                    Text(
                        text = "Wakala: ${customer.agentName.ifBlank { "Ofisi Kuu" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LayTextSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Direct Call Action Bar for Customer
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = LayGreenContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Simu ya Mteja",
                                    fontSize = 11.sp,
                                    color = LayGreenOnContainer,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = customer.phone,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LayNavyDark
                                )
                            }
                            // Prominent Call Button
                            Button(
                                onClick = { CommunicationHelper.makePhoneCall(context, customer.phone) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LayGreenDark),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("call_customer_button")
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Piga Simu", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick SMS & WhatsApp actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    SmsHelper.launchSmsComposer(
                                        context,
                                        customer.phone,
                                        "Habari ${customer.fullName}, kutoka Lay Microfinance."
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.ChatBubble, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tuma SMS", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    CommunicationHelper.openWhatsApp(
                                        context,
                                        customer.phone,
                                        "Habari ${customer.fullName}, kutoka Lay Microfinance."
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp), tint = LayGreenDark)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp", fontSize = 11.sp, color = LayGreenDark)
                            }
                        }
                    }
                }

                // 2. Personal & Guarantor Details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Taarifa Binafsi & Mdhamini",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = LayNavyPrimary
                        )

                        Text("Makazi: ${customer.address}", fontSize = 12.sp, color = LayTextPrimary)

                        if (customer.guarantorName.isNotBlank()) {
                            Divider(modifier = Modifier.padding(vertical = 2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Mdhamini: ${customer.guarantorName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = LayTextPrimary
                                    )
                                    Text(
                                        text = customer.guarantorPhone,
                                        fontSize = 11.sp,
                                        color = LayTextSecondary
                                    )
                                }

                                if (customer.guarantorPhone.isNotBlank()) {
                                    IconButton(
                                        onClick = { CommunicationHelper.makePhoneCall(context, customer.guarantorPhone) },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(LayNavyPrimary.copy(alpha = 0.1f))
                                    ) {
                                        Icon(
                                            Icons.Default.Phone,
                                            contentDescription = "Piga Mdhamini",
                                            tint = LayNavyPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Financial Summary Portfolio
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = LaySurfaceSubtle)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Muhtasari wa Fedha (Financial Summary)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = LayNavyPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Jumla Iliyokopwa:", fontSize = 12.sp)
                            Text(Formatters.formatTsh(totalBorrowed), fontWeight = FontWeight.Bold, color = LayNavyDark, fontSize = 12.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Jumla Iliyolipwa:", fontSize = 12.sp)
                            Text(Formatters.formatTsh(totalRepaid), fontWeight = FontWeight.Bold, color = LayGreenDark, fontSize = 12.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Salio Lililobaki:", fontSize = 12.sp)
                            Text(
                                Formatters.formatTsh(currentOutstanding),
                                fontWeight = FontWeight.ExtraBold,
                                color = if (currentOutstanding > 0) StatusOverdue else LayGreenDark,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // 4. Active Loan Section with Lipa button
                activeLoan?.let { loan ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mkopo Unaofanya Kazi (${loan.loanNumber})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = LayNavyDark
                                )
                                LoanStatusBadge(status = loan.status)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Mkataba: ${Formatters.formatTsh(loan.totalContractualAmount)}", fontSize = 12.sp)
                            Text("Rejesho la Kila Siku: ${Formatters.formatTsh(loan.installmentAmount)}", fontSize = 12.sp, color = LayGreenDark)
                            Text("Salio Lililobaki: ${Formatters.formatTsh(loan.remainingBalance)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                            if (onRecordPaymentClick != null && (loan.status == LoanStatus.ACTIVE || loan.status == LoanStatus.OVERDUE)) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        onRecordPaymentClick(loan)
                                        onDismiss()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = LayGreenDark),
                                    modifier = Modifier.fillMaxWidth().testTag("history_lipa_button")
                                ) {
                                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Rekodi Malipo / Lipa", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 5. Full Loans History
                Text(
                    text = "Historia ya Mikopo Yote (${customerLoans.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = LayNavyDark
                )

                if (customerLoans.isEmpty()) {
                    Text("Hakuna mikopo iliyorekodiwa.", fontSize = 12.sp, color = LayTextTertiary)
                } else {
                    customerLoans.forEach { ln ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${ln.loanNumber} • ${Formatters.formatTsh(ln.principalAmount)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = LayNavyPrimary
                                    )
                                    LoanStatusBadge(status = ln.status)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Tarehe: ${Formatters.formatDate(ln.createdAt)} • Muda: ${ln.durationDays} siku",
                                    fontSize = 11.sp,
                                    color = LayTextSecondary
                                )
                                Text(
                                    text = "Jumla: ${Formatters.formatTsh(ln.totalContractualAmount)} | Imelipwa: ${Formatters.formatTsh(ln.totalPaid)}",
                                    fontSize = 11.sp,
                                    color = LayGreenDark
                                )
                            }
                        }
                    }
                }

                // 6. Full Payments History
                Text(
                    text = "Historia ya Marejesho Yote (${customerPayments.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = LayNavyDark
                )

                if (customerPayments.isEmpty()) {
                    Text("Hakuna stakabadhi za malipo zilizorekodiwa.", fontSize = 12.sp, color = LayTextTertiary)
                } else {
                    customerPayments.forEach { pay ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = pay.receiptNumber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = LayNavyPrimary
                                    )
                                    Text(
                                        text = Formatters.formatDateTime(pay.timestamp),
                                        fontSize = 11.sp,
                                        color = LayTextSecondary
                                    )
                                    Text(
                                        text = "Wakala: ${pay.agentName}",
                                        fontSize = 10.sp,
                                        color = LayTextTertiary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = Formatters.formatTsh(pay.amount),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = LayGreenDark,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Salio: ${Formatters.formatTsh(pay.balanceAfter)}",
                                        fontSize = 11.sp,
                                        color = LayTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = LayNavyPrimary)
            ) {
                Text("Funga")
            }
        }
    )
}
