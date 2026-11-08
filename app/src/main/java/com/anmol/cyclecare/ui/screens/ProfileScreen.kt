package com.anmol.cyclecare.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.anmol.cyclecare.notifications.CareReminderScheduler
import com.anmol.cyclecare.notifications.CareScheduledReminderReceiver
import com.anmol.cyclecare.notifications.PeriodReminderReceiver
import com.anmol.cyclecare.notifications.PeriodReminderScheduler
import com.anmol.cyclecare.viewmodel.PeriodViewModel

// ---------------------------------------------------------------------------
// COLORS
// ---------------------------------------------------------------------------

private val ProfileBackground = Color(0xFFFFF7FA)
private val ProfilePink = Color(0xFFB83D68)
private val ProfileDarkPink = Color(0xFF963052)
private val ProfileRose = Color(0xFFD4527F)
private val ProfileDeepRose = Color(0xFF8E2A4F)
private val ProfileLightPink = Color(0xFFFCE8EF)
private val ProfileText = Color(0xFF30252B)
private val ProfileMuted = Color(0xFF796B73)
private val ProfileCard = Color.White
private val ProfileLavender = Color(0xFFF1EBFC)
private val ProfileGreen = Color(0xFF388B69)
private val ProfileLightGreen = Color(0xFFE9F6EF)

private val AccentWater = Color(0xFF3B82C4)
private val AccentFood = Color(0xFFD9822B)
private val AccentFruit = Color(0xFFD64545)

// ---------------------------------------------------------------------------
// MODELS
// ---------------------------------------------------------------------------

private data class ScheduleItem(
    val time: String,
    val emoji: String,
    val label: String,
    val accent: Color
)

private val careSchedule = listOf(
    ScheduleItem("9:00 AM", "💧", "Drink water", AccentWater),
    ScheduleItem("12:00 PM", "💧", "Drink water", AccentWater),
    ScheduleItem("3:00 PM", "💧", "Drink water", AccentWater),
    ScheduleItem("3:00 PM", "🍛", "Have some food", AccentFood),
    ScheduleItem("5:00 PM", "🍎", "Eat fruits", AccentFruit),
    ScheduleItem("6:00 PM", "💧", "Drink water", AccentWater)
)

private fun needsNotificationPermission(context: Context): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
}

// ---------------------------------------------------------------------------
// MAIN SCREEN
// ---------------------------------------------------------------------------

@Composable
fun ProfileScreen(
    periodViewModel: PeriodViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var careRemindersEnabled by rememberSaveable {
        mutableStateOf(CareReminderScheduler.isEnabled(context))
    }

    var careReminderMessage by remember {
        mutableStateOf<String?>(null)
    }

    val predictedPeriodDate by
    periodViewModel.nextPredictedPeriodDate.collectAsState()

    var showLoveLetter by rememberSaveable {
        mutableStateOf(false)
    }

    var remindersEnabled by rememberSaveable {
        mutableStateOf(
            PeriodReminderScheduler.areRemindersEnabled(context)
        )
    }

    var daysBefore by rememberSaveable {
        mutableStateOf(
            PeriodReminderScheduler.getDaysBefore(context)
        )
    }

    var showReminderMessage by remember {
        mutableStateOf<String?>(null)
    }

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                PeriodReminderScheduler.setReminderPreferences(
                    context = context,
                    enabled = true,
                    daysBefore = daysBefore,
                    predictedPeriodDate = predictedPeriodDate
                )

                remindersEnabled = true

                showReminderMessage =
                    if (predictedPeriodDate != null) {
                        "Your reminder is scheduled, Priyanshi! 🌷"
                    } else {
                        "Reminder enabled. Add a period start date to schedule it. 💗"
                    }
            } else {
                remindersEnabled = false

                PeriodReminderScheduler.setReminderPreferences(
                    context = context,
                    enabled = false,
                    daysBefore = daysBefore,
                    predictedPeriodDate = predictedPeriodDate
                )

                showReminderMessage =
                    "Notifications are disabled. You can allow them in your phone settings."
            }
        }

    val careNotificationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                CareReminderScheduler.setEnabled(context, true)
                careRemindersEnabled = true

                careReminderMessage =
                    "Precious Feature is enabled! ❤️ Your daily care reminders are scheduled."
            } else {
                careRemindersEnabled = false
                CareReminderScheduler.setEnabled(context, false)

                careReminderMessage =
                    "Please allow notifications in your phone settings to receive care reminders."
            }
        }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF0F5),
                        ProfileBackground,
                        Color(0xFFFFF9FB)
                    )
                )
            )
    ) {
        // Subtle background texture
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = ProfilePink.copy(alpha = 0.035f),
                radius = 150.dp.toPx(),
                center = Offset(size.width, size.height * 0.30f)
            )
            drawCircle(
                color = ProfilePink.copy(alpha = 0.035f),
                radius = 110.dp.toPx(),
                center = Offset(0f, size.height * 0.60f)
            )
            drawCircle(
                color = Color(0xFFE9A8C0).copy(alpha = 0.07f),
                radius = 75.dp.toPx(),
                center = Offset(size.width * 0.9f, size.height * 0.88f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // -----------------------------------------------------------
            // HERO
            // -----------------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(32.dp),
                        ambientColor = ProfilePink.copy(alpha = 0.25f),
                        spotColor = ProfilePink.copy(alpha = 0.25f)
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                ProfileRose,
                                ProfilePink,
                                ProfileDeepRose
                            )
                        )
                    )
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.10f),
                        radius = 110.dp.toPx(),
                        center = Offset(size.width, 0f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = 70.dp.toPx(),
                        center = Offset(0f, size.height)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.06f),
                        radius = 36.dp.toPx(),
                        center = Offset(size.width * 0.82f, size.height * 0.62f)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(78.dp)
                            .shadow(
                                elevation = 10.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.2f)
                            )
                            .background(Color.White, CircleShape)
                            .border(
                                width = 3.dp,
                                color = Color.White.copy(alpha = 0.55f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌸", fontSize = 38.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Priyanshi's Little World 🌷",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "A little space filled with care, comfort and love.",
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatusTile(
                            modifier = Modifier.weight(1f),
                            emoji = "💗",
                            label = "Care reminders",
                            value = if (careRemindersEnabled) "On" else "Off"
                        )

                        StatusTile(
                            modifier = Modifier.weight(1f),
                            emoji = "🌷",
                            label = "Period alerts",
                            value = if (remindersEnabled) {
                                "$daysBefore ${if (daysBefore == 1) "day" else "days"} before"
                            } else {
                                "Off"
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // -----------------------------------------------------------
            // AFFIRMATION CARD
            // -----------------------------------------------------------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = ProfilePink.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(26.dp)
                    ),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = ProfileCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    Text(
                        text = "“",
                        fontSize = 52.sp,
                        lineHeight = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ProfilePink.copy(alpha = 0.35f)
                    )

                    Text(
                        text = "Priyanshi, You Are So Special 💗",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfileText
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "You don't have to be perfect every day. " +
                                "You deserve rest when you're tired, " +
                                "comfort when things feel difficult, " +
                                "and love simply for being yourself. 🌸",
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = ProfileMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // -----------------------------------------------------------
            // LOVE NOTE
            // -----------------------------------------------------------
            SectionHeading(
                title = "A Little Love Note 💌",
                subtitle = "Just for you, whenever you need it"
            )

            Card(
                onClick = { showLoveLetter = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = ProfilePink.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ProfileCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        ProfileLightPink,
                                        Color(0xFFF8D6E4)
                                    )
                                ),
                                RoundedCornerShape(18.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MailOutline,
                            contentDescription = "Love letter",
                            tint = ProfilePink,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "A Little Letter for You",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = ProfileText
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = "Tap here whenever you need a little love. ❤️",
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = ProfileMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .background(ProfileLightPink, RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "Open 💌",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ProfilePink
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // -----------------------------------------------------------
            // ANMOL FEATURE: DAILY CARE REMINDERS
            // -----------------------------------------------------------
            SectionHeading(
                title = "Precious Feature ❤️",
                subtitle = "Small daily nudges to take care of yourself"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = ProfilePink.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ProfileCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconBadge(background = ProfileLightPink) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Care reminders",
                                tint = ProfilePink,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Care Reminders 💗",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = ProfileText
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (careRemindersEnabled) {
                                    "Your daily care reminders are enabled."
                                } else {
                                    "Gentle reminders for water, food, and fruits."
                                },
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = ProfileMuted
                            )
                        }

                        Switch(
                            checked = careRemindersEnabled,
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = ProfilePink,
                                checkedThumbColor = Color.White
                            ),
                            onCheckedChange = { enable ->
                                if (enable) {
                                    if (needsNotificationPermission(context)) {
                                        careNotificationPermissionLauncher.launch(
                                            Manifest.permission.POST_NOTIFICATIONS
                                        )
                                    } else {
                                        CareReminderScheduler.setEnabled(
                                            context,
                                            true
                                        )

                                        careRemindersEnabled = true

                                        careReminderMessage =
                                            "Precious Feature is enabled! ❤️ Daily reminders are scheduled."
                                    }
                                } else {
                                    CareReminderScheduler.setEnabled(
                                        context,
                                        false
                                    )

                                    careRemindersEnabled = false

                                    careReminderMessage =
                                        "Precious Feature reminders have been turned off."
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "YOUR DAILY SCHEDULE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = ProfileMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ScheduleTimeline(items = careSchedule)

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Tap Haan or Nahi on a reminder notification to save your response. ❤️",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = ProfileMuted
                    )

                    // Temporary Precious Feature notification test button
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (needsNotificationPermission(context)) {
                                careNotificationPermissionLauncher.launch(
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                            } else {
                                val testIntent = Intent(
                                    context,
                                    CareScheduledReminderReceiver::class.java
                                ).apply {
                                    action =
                                        "com.anmol.cyclecare.action.CARE_REMINDER"

                                    putExtra(
                                        "reminder_id",
                                        "water_manual_test_${System.currentTimeMillis()}"
                                    )

                                    putExtra(
                                        "reminder_type",
                                        "WATER"
                                    )
                                }

                                context.sendBroadcast(testIntent)

                                careReminderMessage =
                                    "Precious Feature test triggered. Check your notifications. ❤️"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfileLightPink,
                            contentColor = ProfilePink
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Test Precious Notification ❤️",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    AnimatedVisibility(
                        visible = careReminderMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            MessageBanner(careReminderMessage.orEmpty())
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // -----------------------------------------------------------
            // PERIOD REMINDERS
            // -----------------------------------------------------------
            SectionHeading(
                title = "Gentle Period Reminders 🔔",
                subtitle = "A soft heads-up before your predicted period"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = ProfilePink.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ProfileCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconBadge(background = ProfileLavender) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = ProfilePink,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Period reminders",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = ProfileText
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (remindersEnabled) {
                                    "Your gentle reminders are enabled."
                                } else {
                                    "Get a gentle reminder before your predicted period."
                                },
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = ProfileMuted
                            )
                        }

                        Switch(
                            checked = remindersEnabled,
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = ProfilePink,
                                checkedThumbColor = Color.White
                            ),
                            onCheckedChange = { enable ->
                                if (enable) {
                                    if (needsNotificationPermission(context)) {
                                        notificationPermissionLauncher.launch(
                                            Manifest.permission.POST_NOTIFICATIONS
                                        )
                                    } else {
                                        PeriodReminderScheduler.setReminderPreferences(
                                            context = context,
                                            enabled = true,
                                            daysBefore = daysBefore,
                                            predictedPeriodDate = predictedPeriodDate
                                        )

                                        remindersEnabled = true

                                        showReminderMessage =
                                            if (predictedPeriodDate != null) {
                                                "Your reminder is scheduled, Priyanshi! 🌷"
                                            } else {
                                                "Reminder enabled. Add a period start date to schedule it. 💗"
                                            }
                                    }
                                } else {
                                    remindersEnabled = false

                                    PeriodReminderScheduler.setReminderPreferences(
                                        context = context,
                                        enabled = false,
                                        daysBefore = daysBefore,
                                        predictedPeriodDate = predictedPeriodDate
                                    )

                                    showReminderMessage =
                                        "Period reminders have been turned off."
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "REMIND ME BEFORE MY PREDICTED PERIOD",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = ProfileMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            1 to "1 day",
                            2 to "2 days",
                            3 to "3 days"
                        ).forEach { (days, label) ->
                            DaysChip(
                                modifier = Modifier.weight(1f),
                                label = label,
                                selected = daysBefore == days,
                                onClick = {
                                    daysBefore = days

                                    if (remindersEnabled) {
                                        PeriodReminderScheduler.setReminderPreferences(
                                            context = context,
                                            enabled = true,
                                            daysBefore = daysBefore,
                                            predictedPeriodDate = predictedPeriodDate
                                        )

                                        showReminderMessage =
                                            if (predictedPeriodDate != null) {
                                                "Reminder timing updated. 🌸"
                                            } else {
                                                "Timing saved. Add a period start date to schedule a reminder."
                                            }
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Reminders are scheduled for 9:00 AM. " +
                                "Period predictions are estimates and may not match your actual cycle.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = ProfileMuted
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val testIntent = Intent(
                                context,
                                PeriodReminderReceiver::class.java
                            ).apply {
                                putExtra("is_test_notification", true)
                            }

                            context.sendBroadcast(testIntent)

                            showReminderMessage =
                                "Test notification requested! Check your notifications. 💗"
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfileLightPink,
                            contentColor = ProfilePink
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Send Test Notification 💗",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    AnimatedVisibility(
                        visible = showReminderMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            MessageBanner(showReminderMessage.orEmpty())
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // -----------------------------------------------------------
            // CYCLE & CARE
            // -----------------------------------------------------------
            SectionHeading(
                title = "Your Cycle & Your Care 🌷",
                subtitle = "Gentle thoughts for every kind of day"
            )

            ProfileInfoCard(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = null,
                        tint = ProfilePink,
                        modifier = Modifier.size(28.dp)
                    )
                },
                iconBackground = ProfileLightPink,
                title = "Your Well-being Matters",
                description = "Your cycle is personal, and every body " +
                        "is different. Use CycleCare to record your period " +
                        "and understand your own patterns over time."
            )

            Spacer(modifier = Modifier.height(14.dp))

            ProfileInfoCard(
                icon = {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = ProfilePink,
                        modifier = Modifier.size(28.dp)
                    )
                },
                iconBackground = ProfileLightPink,
                title = "A Gentle Reminder",
                description = "It's okay to slow down, take a break, " +
                        "drink some water and do something kind for yourself. " +
                        "You deserve care on every kind of day."
            )

            Spacer(modifier = Modifier.height(32.dp))

            // -----------------------------------------------------------
            // PRIVACY
            // -----------------------------------------------------------
            SectionHeading(
                title = "Your Privacy 🔒",
                subtitle = "Your records stay with you"
            )

            ProfileInfoCard(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = ProfileGreen,
                        modifier = Modifier.size(28.dp)
                    )
                },
                iconBackground = ProfileLightGreen,
                title = "Your Data Is Personal",
                description = "Your period records are stored using " +
                        "the app's local database on this device. This screen " +
                        "does not upload your records to a server. Keep your " +
                        "device secure to help protect your information."
            )

            Spacer(modifier = Modifier.height(32.dp))

            // -----------------------------------------------------------
            // ABOUT
            // -----------------------------------------------------------
            SectionHeading(
                title = "About CycleCare 🌸",
                subtitle = "What this app is, and isn't"
            )

            ProfileInfoCard(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = ProfilePink,
                        modifier = Modifier.size(28.dp)
                    )
                },
                iconBackground = ProfileLavender,
                title = "Made with Care",
                description = "CycleCare helps you record your periods, " +
                        "view cycle patterns and see estimated upcoming " +
                        "period dates. Predictions are estimates, not " +
                        "medical advice or a guarantee."
            )

            Spacer(modifier = Modifier.height(32.dp))

            // -----------------------------------------------------------
            // ONE LAST THING
            // -----------------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = ProfilePink.copy(alpha = 0.12f)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                ProfileLavender,
                                Color(0xFFF8E6F0)
                            )
                        )
                    )
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.40f),
                        radius = 70.dp.toPx(),
                        center = Offset(size.width, 0f)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "💕", fontSize = 32.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "One Last Thing...",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfileText
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Priyanshi, no matter what kind of day " +
                                "you're having, I hope you always remember " +
                                "how precious you are to me. If I could make " +
                                "one wish, it would be to see you smiling, " +
                                "feeling loved, and knowing that you don't " +
                                "have to face the difficult days alone. ❤️",
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        color = ProfileMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Box(
                        modifier = Modifier
                            .shadow(
                                elevation = 3.dp,
                                shape = RoundedCornerShape(50),
                                ambientColor = ProfilePink.copy(alpha = 0.15f)
                            )
                            .background(Color.White, RoundedCornerShape(50))
                            .padding(horizontal = 20.dp, vertical = 11.dp)
                    ) {
                        Text(
                            text = "You are loved, Priyanshi. Always. 🌷",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ProfilePink,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // -----------------------------------------------------------
            // FOOTER
            // -----------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(ProfilePink.copy(alpha = 0.15f))
                )

                Text(
                    text = "  💗  ",
                    fontSize = 14.sp
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(ProfilePink.copy(alpha = 0.15f))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Made with love, just for you",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = ProfilePink,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // ---------------------------------------------------------------
    // LOVE LETTER DIALOG
    // ---------------------------------------------------------------
    if (showLoveLetter) {
        AlertDialog(
            onDismissRequest = {
                showLoveLetter = false
            },
            containerColor = ProfileBackground,
            shape = RoundedCornerShape(28.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(ProfileLightPink, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Heart",
                        tint = ProfilePink,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "For My Priyanshi 💌",
                    color = ProfilePink,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(20.dp))
                            .border(
                                width = 1.dp,
                                color = ProfilePink.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "My Dear Priyanshi, ❤️",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = ProfileText
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "I made this little space with you in " +
                                    "mind, hoping it can bring you a little " +
                                    "comfort and a smile whenever you open it.\n\n" +
                                    "I wish I could make every difficult day " +
                                    "easier for you. Even when I can't fix " +
                                    "everything, I want you to know that your " +
                                    "feelings matter, your happiness matters, " +
                                    "and you matter so much to me.\n\n" +
                                    "Please be gentle with yourself. You don't " +
                                    "always have to be strong, and you never " +
                                    "have to hide how you're feeling. Take your " +
                                    "time, get some rest when you need it, " +
                                    "and remember that you deserve all the " +
                                    "kindness you give to others.\n\n" +
                                    "Among all the little things in life, " +
                                    "your smile is one I hope to keep seeing. " +
                                    "I hope this app reminds you to care for " +
                                    "yourself as much as you deserve to be cared for.\n\n" +
                                    "Thank you for being you, Priyanshi. " +
                                    "You make my world a little brighter, " +
                                    "and I'm grateful to have you in my life.\n\n" +
                                    "With all my love, ❤️\n" +
                                    "Your favourite person 🌷",
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            color = ProfileText,
                            textAlign = TextAlign.Start
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Sending you the biggest hug. 🫂💕",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ProfilePink,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLoveLetter = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfilePink
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Sending love 💗")
                }
            }
        )
    }
}

// ---------------------------------------------------------------------------
// REUSABLE COMPONENTS
// ---------------------------------------------------------------------------

@Composable
private fun SectionHeading(
    title: String,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(if (subtitle != null) 38.dp else 20.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(ProfileRose, ProfileDeepRose)
                    ),
                    RoundedCornerShape(50)
                )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = ProfileText
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = ProfileMuted
                )
            }
        }
    }
}

@Composable
private fun StatusTile(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    value: String
) {
    Column(
        modifier = modifier
            .background(
                Color.White.copy(alpha = 0.18f),
                RoundedCornerShape(18.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.28f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = "$emoji  $label",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.85f)
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun IconBadge(
    background: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(background, RoundedCornerShape(15.dp)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun ScheduleTimeline(items: List<ScheduleItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ProfileBackground, RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = ProfilePink.copy(alpha = 0.08f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        items.forEachIndexed { index, item ->
            val isLast = index == items.lastIndex

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // Time
                Text(
                    text = item.time,
                    modifier = Modifier
                        .width(66.dp)
                        .padding(top = 1.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfileMuted
                )

                // Dot + connecting line
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(item.accent.copy(alpha = 0.18f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(item.accent, CircleShape)
                        )
                    }

                    if (!isLast) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .weight(1f)
                                .background(ProfilePink.copy(alpha = 0.15f))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Activity
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = if (isLast) 0.dp else 16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = item.emoji, fontSize = 16.sp)

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = item.label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ProfileText
                    )
                }
            }
        }
    }
}

@Composable
private fun DaysChip(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background by animateColorAsState(
        targetValue = if (selected) ProfilePink else ProfileBackground,
        label = "chipBackground"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) ProfilePink else ProfilePink.copy(alpha = 0.18f),
        label = "chipBorder"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else ProfileText,
        label = "chipText"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun MessageBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ProfileLightPink, RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = ProfilePink.copy(alpha = 0.12f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "💬", fontSize = 16.sp)

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = message,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = ProfileDarkPink
        )
    }
}

@Composable
private fun ProfileInfoCard(
    icon: @Composable () -> Unit,
    iconBackground: Color,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = ProfilePink.copy(alpha = 0.08f),
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = ProfileCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            IconBadge(background = iconBackground) {
                icon()
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfileText
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = description,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = ProfileMuted
                )
            }
        }
    }
}