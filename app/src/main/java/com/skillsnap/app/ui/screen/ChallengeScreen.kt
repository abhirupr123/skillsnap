package com.skillsnap.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skillsnap.app.R
import com.skillsnap.app.data.model.Challenge

@Composable
fun ChallengeScreen(
    challenges: List<Challenge>,
    uiState: com.skillsnap.app.ui.viewmodel.ChallengeUiState,
    onMarkComplete: (Long) -> Unit,
    onBackToSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todayChallenge = challenges.find { !it.isCompleted } ?: challenges.firstOrNull()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SkillSnap",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            TextButton(onClick = onBackToSelection) {
                Text("New Skill")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Progress indicator
        if (uiState.totalChallenges > 0) {
            LinearProgressIndicator(
                progress = uiState.completedChallenges.toFloat() / uiState.totalChallenges.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            Text(
                text = "${uiState.completedChallenges}/${uiState.totalChallenges} challenges completed",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
        
        // Today's challenge card
        todayChallenge?.let { challenge ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.today_challenge),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "Day ${challenge.day}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Text(
                        text = challenge.challengeText,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    
                    if (!challenge.isCompleted) {
                        Button(
                            onClick = { onMarkComplete(challenge.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.mark_complete))
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = stringResource(R.string.challenge_completed),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
        
        // All challenges list
        Text(
            text = stringResource(R.string.progress),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyColumn {
            items(challenges) { challenge ->
                ChallengeItem(
                    challenge = challenge,
                    onMarkComplete = onMarkComplete
                )
            }
        }
    }
}

@Composable
fun ChallengeItem(
    challenge: Challenge,
    onMarkComplete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (challenge.isCompleted) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Outlined.RadioButtonUnchecked
                },
                contentDescription = if (challenge.isCompleted) "Completed" else "Pending",
                tint = if (challenge.isCompleted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(end = 12.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Day ${challenge.day}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = challenge.challengeText,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
            
            if (!challenge.isCompleted) {
                TextButton(
                    onClick = { onMarkComplete(challenge.id) }
                ) {
                    Text("Complete")
                }
            }
        }
    }
} 