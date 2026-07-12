package edu.feutech.redu.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.feutech.redu.ui.theme.ReduPalette
import edu.feutech.redu.ui.theme.ReduStatusPalette

@Composable
internal fun AdaptiveNavigationScaffold(
    primaryDestinations: List<ReduDestination>,
    selectedDestination: ReduDestination,
    showNavigation: Boolean,
    onDestinationSelected: (ReduDestination) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val expanded = maxWidth >= 600.dp
        Row(modifier = Modifier.fillMaxSize()) {
            if (expanded && showNavigation) {
                ReduNavigationRail(
                    destinations = primaryDestinations,
                    selectedDestination = selectedDestination,
                    onDestinationSelected = onDestinationSelected,
                )
            }

            Scaffold(
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                contentWindowInsets = WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                ),
                bottomBar = {
                    if (!expanded && showNavigation) {
                        ReduBottomNavigation(
                            destinations = primaryDestinations,
                            selectedDestination = selectedDestination,
                            onDestinationSelected = onDestinationSelected,
                        )
                    }
                },
                content = content,
            )
        }
    }
}

@Composable
private fun ReduBottomNavigation(
    destinations: List<ReduDestination>,
    selectedDestination: ReduDestination,
    onDestinationSelected: (ReduDestination) -> Unit,
) {
    val largeText = LocalDensity.current.fontScale >= 1.5f
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .heightIn(min = if (largeText) 88.dp else 72.dp),
        ) {
            destinations.forEach { destination ->
                ReduNavigationItem(
                    destination = destination,
                    selected = destination == selectedDestination,
                    onClick = { onDestinationSelected(destination) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ReduNavigationRail(
    destinations: List<ReduDestination>,
    selectedDestination: ReduDestination,
    onDestinationSelected: (ReduDestination) -> Unit,
) {
    val largeText = LocalDensity.current.fontScale >= 1.5f
    Surface(
        modifier = Modifier
            .width(if (largeText) 128.dp else 88.dp)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            destinations.forEach { destination ->
                ReduNavigationItem(
                    destination = destination,
                    selected = destination == selectedDestination,
                    onClick = { onDestinationSelected(destination) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = if (largeText) 92.dp else 76.dp),
                )
            }
        }
    }
}

@Composable
private fun ReduNavigationItem(
    destination: ReduDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(180),
        label = "navigation color",
    )
    val largeText = LocalDensity.current.fontScale >= 1.5f
    Column(
        modifier = modifier
            .semantics {
                role = Role.Tab
                this.selected = selected
            }
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = if (largeText) 4.dp else 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.height(3.dp).width(24.dp).background(
                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(2.dp),
            ),
        )
        Spacer(Modifier.height(7.dp))
        Icon(
            imageVector = destination.icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = destination.label,
            style = if (largeText) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1,
        )
    }
}

@Composable
internal fun ReduScreen(
    padding: PaddingValues,
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: LazyListScope.() -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().widthIn(max = 760.dp),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item {
                ReduPageHeader(
                    title = title,
                    subtitle = subtitle,
                    onBack = onBack,
                    actions = actions,
                )
            }
            content()
        }
    }
}

@Composable
private fun ReduPageHeader(
    title: String,
    subtitle: String?,
    onBack: (() -> Unit)?,
    actions: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 28.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
            }
        }
        Column(
            modifier = Modifier.weight(1f).padding(top = if (onBack == null) 0.dp else 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "REDU",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
            subtitle?.let { ReduSecondaryText(it) }
        }
        Row(content = actions)
    }
}

@Composable
internal fun ReduSectionHeader(
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val stackTrailing = trailing != null && LocalDensity.current.fontScale >= 1.3f
    val modifier = Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 12.dp)

    if (stackTrailing) {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            subtitle?.let { ReduSecondaryText(it) }
            trailing()
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                subtitle?.let { ReduSecondaryText(it) }
            }
            trailing?.invoke()
        }
    }
}

@Composable
internal fun ReduSecondaryText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
internal fun ReduCaption(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
internal fun ReduSection(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 0.dp,
    ) {
        Column(content = content)
    }
}

@Composable
internal fun ReduStatusLabel(label: String, tone: StatusTone) {
    val (container, content) = statusColors(tone)
    Surface(
        color = container,
        contentColor = content,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(6.dp).background(statusIndicatorColor(tone), CircleShape))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
internal fun ReduAttentionBanner(
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReduStatusPalette.AttentionContainer,
        contentColor = ReduStatusPalette.OnAttentionContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(body, style = MaterialTheme.typography.bodyMedium)
                TextButton(
                    onClick = onAction,
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = ReduStatusPalette.OnAttentionContainer),
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
internal fun ReduInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val largeText = LocalDensity.current.fontScale >= 1.5f
    if (largeText) {
        Column(
            modifier = modifier.fillMaxWidth().padding(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.SemiBold,
            )
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                value,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun ReduDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
internal fun ReduPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text)
    }
}

@Composable
internal fun ReduOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    destructive: Boolean = false,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Text(text)
    }
}

@Composable
internal fun ReduSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    label: String,
) {
    Switch(
        checked = checked,
        enabled = enabled,
        onCheckedChange = onCheckedChange,
        modifier = Modifier.semantics {
            contentDescription = label
            stateDescription = if (checked) "On" else "Off"
        },
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            checkedBorderColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            uncheckedBorderColor = MaterialTheme.colorScheme.outline,
        ),
    )
}

@Composable
internal fun ReduSettingRow(
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }
    val containerModifier = modifier.fillMaxWidth().then(clickableModifier)
        .padding(horizontal = 16.dp, vertical = 14.dp)
    val stackTrailing = trailing != null && LocalDensity.current.fontScale >= 1.5f
    if (stackTrailing) {
        Column(modifier = containerModifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            subtitle?.let { ReduCaption(it) }
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                trailing()
            }
        }
    } else {
        Row(
            modifier = containerModifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                subtitle?.let { ReduCaption(it) }
            }
            when {
                trailing != null -> trailing()
                onClick != null -> Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun ActivityPatternMeter(
    score: Double,
    modifier: Modifier = Modifier,
) {
    val normalized = score.coerceIn(0.0, 100.0).toFloat()
    val markerColor = when {
        normalized < 33.33f -> ReduStatusPalette.Normal
        normalized < 66.67f -> ReduStatusPalette.Elevated
        else -> ReduStatusPalette.Extended
    }
    val markerOutline = MaterialTheme.colorScheme.background
    Canvas(
        modifier = modifier.fillMaxWidth().height(18.dp).semantics {
            contentDescription = "Activity pattern score ${normalized.toInt()} out of 100"
            progressBarRangeInfo = ProgressBarRangeInfo(normalized, 0f..100f)
        },
    ) {
        val trackHeight = 6.dp.toPx()
        val trackTop = (size.height - trackHeight) / 2f
        val gap = 3.dp.toPx()
        val segmentWidth = (size.width - gap * 2f) / 3f
        drawRoundRect(
            color = ReduPalette.SeaGlassContainer,
            topLeft = Offset(0f, trackTop),
            size = androidx.compose.ui.geometry.Size(segmentWidth, trackHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2f),
        )
        drawRoundRect(
            color = ReduPalette.WarningContainer,
            topLeft = Offset(segmentWidth + gap, trackTop),
            size = androidx.compose.ui.geometry.Size(segmentWidth, trackHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2f),
        )
        drawRoundRect(
            color = ReduPalette.HighContainer,
            topLeft = Offset((segmentWidth + gap) * 2f, trackTop),
            size = androidx.compose.ui.geometry.Size(segmentWidth, trackHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2f),
        )
        val markerX = (normalized / 100f) * size.width
        drawCircle(
            color = markerOutline,
            radius = 7.dp.toPx(),
            center = Offset(markerX.coerceIn(7.dp.toPx(), size.width - 7.dp.toPx()), size.height / 2f),
        )
        drawCircle(
            color = markerColor,
            radius = 4.dp.toPx(),
            center = Offset(markerX.coerceIn(7.dp.toPx(), size.width - 7.dp.toPx()), size.height / 2f),
        )
    }
}

@Composable
internal fun ReduEmptyState(
    title: String,
    body: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.width(36.dp).height(3.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
        Text(title, style = MaterialTheme.typography.titleLarge)
        ReduSecondaryText(body, Modifier.widthIn(max = 520.dp))
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp)) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
internal fun ReduLoadingScreen() {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp, vertical = 24.dp).widthIn(max = 760.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SkeletonBar(widthFraction = 0.14f, height = 12.dp)
            SkeletonBar(widthFraction = 0.34f, height = 34.dp)
            SkeletonBar(widthFraction = 0.48f, height = 16.dp)
            Spacer(Modifier.height(12.dp))
            SkeletonBar(widthFraction = 1f, height = 150.dp)
            SkeletonBar(widthFraction = 0.28f, height = 20.dp)
            SkeletonBar(widthFraction = 1f, height = 170.dp)
        }
    }
}

@Composable
private fun SkeletonBar(widthFraction: Float, height: androidx.compose.ui.unit.Dp) {
    val transition = rememberInfiniteTransition(label = "loading")
    val alpha by transition.animateFloat(
        initialValue = 0.42f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse),
        label = "loading alpha",
    )
    Box(
        modifier = Modifier.fillMaxWidth(widthFraction).height(height).clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = alpha)),
    )
}

@Composable
internal fun statusColors(tone: StatusTone): Pair<Color, Color> = when (tone) {
    StatusTone.NORMAL,
    StatusTone.SUCCESS -> ReduStatusPalette.NormalContainer to ReduStatusPalette.OnNormalContainer
    StatusTone.ELEVATED -> ReduStatusPalette.ElevatedContainer to ReduStatusPalette.OnElevatedContainer
    StatusTone.EXTENDED -> ReduStatusPalette.ExtendedContainer to ReduStatusPalette.OnExtendedContainer
    StatusTone.ATTENTION -> ReduStatusPalette.AttentionContainer to ReduStatusPalette.OnAttentionContainer
    StatusTone.ERROR -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    StatusTone.NEUTRAL -> MaterialTheme.colorScheme.surfaceContainerHighest to MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
internal fun statusIndicatorColor(tone: StatusTone): Color = when (tone) {
    StatusTone.NORMAL,
    StatusTone.SUCCESS -> ReduStatusPalette.Normal
    StatusTone.ELEVATED -> ReduStatusPalette.Elevated
    StatusTone.EXTENDED,
    StatusTone.ERROR -> ReduStatusPalette.Extended
    StatusTone.ATTENTION -> ReduStatusPalette.Attention
    StatusTone.NEUTRAL -> MaterialTheme.colorScheme.outline
}
