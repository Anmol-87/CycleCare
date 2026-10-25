package com.anmol.cyclecare.ui.screens

import android.content.Intent
import com.anmol.cyclecare.notifications.PeriodReminderReceiver
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.anmol.cyclecare.notifications.PeriodReminderScheduler
import com.anmol.cyclecare.viewmodel.PeriodViewModel

private val ProfileBackground = Color(0xFFFFF7FA)
private val ProfilePink = Color(0xFFB83D68)
private val ProfileLightPink = Color(0xFFFCE8EF)
private val ProfileText = Color(0xFF30252B)
private val ProfileMuted = Color(0xFF796B73)
private val ProfileCard = Color.White
private val ProfileLavender = Color(0xFFF1EBFC)

@Composable
fun ProfileScreen(
    periodViewModel: PeriodViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ProfileBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Priyanshi's Little World 🌷",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = ProfilePink,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "A little space filled with care, comfort and love.",
            fontSize = 14.sp,
            color = ProfileMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = ProfileLightPink
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Heart",
                    tint = ProfilePink,
                    modifier = Modifier.size(42.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Priyanshi, You Are So Special 💗",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfileText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "You don't have to be perfect every day. " +
                            "You deserve rest when you're tired, " +
                            "comfort when things feel difficult, " +
                            "and love simply for being yourself. 🌸",
                    fontSize = 15.sp,
                    lineHeight = 23.sp,
                    color = ProfileMuted,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeading("A Little Love Note 💌")

        Card(
            onClick = { showLoveLetter = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = ProfileCard
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .background(
                            ProfileLightPink,
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

                Spacer(modifier = Modifier.size(16.dp))

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

                Text(text = "💌", fontSize = 25.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Period reminder settings
        SectionHeading("Gentle Period Reminders 🔔")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = ProfileCard
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
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
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = ProfilePink,
                        modifier = Modifier.size(30.dp)
                    )

                    Spacer(modifier = Modifier.size(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Period reminders",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = ProfileText
                        )

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
                        onCheckedChange = { enable ->
                            if (enable) {
                                if (Build.VERSION.SDK_INT >= 33 &&
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
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

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Remind me before my predicted period:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ProfileText
                )

                Spacer(modifier = Modifier.height(8.dp))

                listOf(
                    1 to "1 day before",
                    2 to "2 days before",
                    3 to "3 days before"
                ).forEach { (days, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
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

                        Text(
                            text = label,
                            fontSize = 14.sp,
                            color = ProfileText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfilePink
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    Text(
                        text = "Send Test Notification 💗",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (showReminderMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = showReminderMessage!!,
                        fontSize = 13.sp,
                        color = ProfilePink
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeading("Your Cycle & Your Care 🌷")

        ProfileInfoCard(
            icon = {
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null,
                    tint = ProfilePink,
                    modifier = Modifier.size(28.dp)
                )
            },
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
            title = "A Gentle Reminder",
            description = "It's okay to slow down, take a break, " +
                    "drink some water and do something kind for yourself. " +
                    "You deserve care on every kind of day."
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeading("Your Privacy 🔒")

        ProfileInfoCard(
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = ProfilePink,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = "Your Data Is Personal",
            description = "Your period records are stored using " +
                    "the app's local database on this device. This screen " +
                    "does not upload your records to a server. Keep your " +
                    "device secure to help protect your information."
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeading("About CycleCare 🌸")

        ProfileInfoCard(
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = ProfilePink,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = "Made with Care",
            description = "CycleCare helps you record your periods, " +
                    "view cycle patterns and see estimated upcoming " +
                    "period dates. Predictions are estimates, not " +
                    "medical advice or a guarantee."
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = ProfileLavender
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "One Last Thing... 💕",
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

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "You are loved, Priyanshi. Always. 🌷",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfilePink,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Made with love, just for you 💗",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = ProfilePink,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showLoveLetter) {
        AlertDialog(
            onDismissRequest = {
                showLoveLetter = false
            },
            containerColor = ProfileBackground,
            shape = RoundedCornerShape(28.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Heart",
                    tint = ProfilePink,
                    modifier = Modifier.size(38.dp)
                )
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

                    Spacer(modifier = Modifier.height(12.dp))

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

@Composable
private fun SectionHeading(
    title: String
) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
        color = ProfileText
    )
}

@Composable
private fun ProfileInfoCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = ProfileCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = ProfileLightPink,
                        shape = RoundedCornerShape(15.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.size(14.dp))

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