package com.example.sfsinstaller.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sfsinstaller.BuildConfig
import com.example.sfsinstaller.R
import com.example.sfsinstaller.models.InfoLevel
import com.example.sfsinstaller.models.InfoMsg
import com.example.sfsinstaller.ui.components.AboutDialog
import com.example.sfsinstaller.ui.components.ToolbarMenu
import com.example.sfsinstaller.ui.viewmodels.AppState
import com.example.sfsinstaller.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(mainViewModel: MainViewModel) {
    val appState = mainViewModel.appState.collectAsState().value
    val scrollState = rememberScrollState()
    var aboutDialogShow by remember { mutableStateOf(false) }
    val context = LocalContext.current
    if (aboutDialogShow)
        AboutDialog(closeDialog = { aboutDialogShow = false })
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Text(context.getString(R.string.app_name))
                    },
                    actions = {
                        ToolbarMenu(
                            openAboutDialog = {
                                aboutDialogShow = true
                            }
                        )
                    }
                )
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues),
            contentPadding = PaddingValues(
                16.dp,
                24.dp,
                16.dp,
                24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ExecuteCard(
                    onButtonClick = { mainViewModel.fetchInfomation(context) },
                    crackChipChecked = appState.crackPatchChecked,
                    translationChipChecked = appState.translationChecked,
                    toggleCrackPatch = { mainViewModel.toggleCrackPatch() },
                    toggleTranslation = { mainViewModel.toggleTranslation() }
                )
            }
            item {
                var isInfoCardShow = appState.infoList.size > 0
                AnimatedVisibility(
                    visible = isInfoCardShow,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    InfoCard(
                        infoSource = appState.infoList,
                        closeButtoClick = { mainViewModel.clearInfoText() }
                    )
                }
            }
            if (BuildConfig.IS_DEBUG)
                item {
                    AppStateCard(
                        appState = appState
                    )
                }
        }
    }
}

@Composable
fun ExecuteCard(
    onButtonClick: () -> Unit,
    crackChipChecked: Boolean,
    translationChipChecked: Boolean,
    toggleCrackPatch: () -> Unit,
    toggleTranslation: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.box_add_24px),
                    contentDescription = "install icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "安装",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = "请确保你有良好的网络连接",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { toggleCrackPatch() },
                    label = { Text("破解补丁") },
                    selected = crackChipChecked,
                    leadingIcon = {
                        if (crackChipChecked)
                            Icon(
                                painter = painterResource(R.drawable.check_24px),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                    }
                )

                FilterChip(
                    onClick = { toggleTranslation() },
                    label = { Text("汉化包") },
                    selected = translationChipChecked,
                    leadingIcon = {
                        if (translationChipChecked)
                            Icon(
                                painter = painterResource(R.drawable.check_24px),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                    }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onButtonClick
                ) {
                    Icon(
                        painter = painterResource(R.drawable.add_24px),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("执行")
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoCard(
    infoSource: List<InfoMsg>,
    closeButtoClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Box() {
            IconButton(
                modifier = Modifier.align(Alignment.TopEnd),
                onClick = {
                    closeButtoClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(R.drawable.close_24px),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                infoSource.map { msg ->
                    SelectionContainer() {
                        Text(
                            text = msg.info,
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            color = when (msg.level) {
                                InfoLevel.LEVEL_INFO -> MaterialTheme.colorScheme.onBackground
                                InfoLevel.LEVEL_WARNING -> Color(0xFFFFA000)
                                InfoLevel.LEVEL_ERROR -> MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppStateCard(appState: AppState) {
    val context = LocalContext.current
    OutlinedCard() {
        Text(appState.toString())
    }
}