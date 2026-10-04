package edu.feutech.redu.ui

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint as AndroidPaint
import android.graphics.RectF
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import edu.feutech.redu.R
import edu.feutech.redu.ui.theme.ReduButtonMinHeight
import edu.feutech.redu.ui.theme.ReduInlineIconSize
import edu.feutech.redu.ui.theme.ReduNavIconSize
import edu.feutech.redu.ui.theme.ReduPalette
import edu.feutech.redu.ui.theme.ReduPill
import edu.feutech.redu.ui.theme.ReduStatusPalette

@Composable
internal fun AdaptiveNavigationScaffold(
    primaryDestinations: List<ReduDestination>,
    selectedDestination: ReduDestination,
    showNavigation: Boolean,
    onDestinationSelected: (ReduDestination) -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.background,
    topScrim: Boolean = true,
    content: @Composable (PaddingValues) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val expanded = maxWidth >= 600.dp
        val showFloatingNav = !expanded && showNavigation
        var floatingNavHeight by remember { mutableStateOf(0.dp) }
        val density = LocalDensity.current
        val hazeState = rememberHazeState()
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (showFloatingNav) Modifier.hazeSource(hazeState) else Modifier),
            ) {
                if (expanded && showNavigation) {
                    ReduNavigationRail(
                        destinations = primaryDestinations,
                        selectedDestination = selectedDestination,
                        onDestinationSelected = onDestinationSelected,
                    )
                }

                Scaffold(
                    modifier = Modifier.weight(1f),
                    containerColor = containerColor,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    contentWindowInsets = WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                    ),
                ) { insets ->
                    val layoutDirection = LocalLayoutDirection.current
                    content(
                        PaddingValues(
                            start = insets.calculateStartPadding(layoutDirection),
                            top = insets.calculateTopPadding(),
                            end = insets.calculateEndPadding(layoutDirection),
                            bottom = if (showFloatingNav) floatingNavHeight else insets.calculateBottomPadding(),
                        ),
                    )
                }
            }
            if (topScrim) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(
                            Brush.verticalGradient(
                                0f to ReduPalette.Background,
                                1f to Color.Transparent,
                            ),
                        ),
                )
            }
            if (showFloatingNav) {
                ReduBottomNavigation(
                    destinations = primaryDestinations,
                    selectedDestination = selectedDestination,
                    onDestinationSelected = onDestinationSelected,
                    hazeState = hazeState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .onSizeChanged { size ->
                            val height = with(density) { size.height.toDp() }
                            if (floatingNavHeight != height) floatingNavHeight = height
                        },
                )
            }
        }
    }
}

@Composable
private fun ReduBottomNavigation(
    destinations: List<ReduDestination>,
    selectedDestination: ReduDestination,
    onDestinationSelected: (ReduDestination) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    val container = MaterialTheme.colorScheme.surfaceContainer
    val surround = 40.dp
    Layout(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 10.dp),
        content = {
            NavSurroundBlur(hazeState = hazeState, spread = surround)
            Surface(
                modifier = Modifier
                    .shadow(
                        elevation = 28.dp,
                        shape = ReduPill,
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.78f),
                        spotColor = Color.Black.copy(alpha = 0.62f),
                    )
                    .clip(ReduPill)
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle(
                            backgroundColor = container,
                            tint = HazeTint(container.copy(alpha = 0.5f)),
                            blurRadius = 48.dp,
                            noiseFactor = 0.1f,
                            fallbackTint = HazeTint(container.copy(alpha = 0.94f)),
                        ),
                    ),
                color = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = ReduPill,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    destinations.forEach { destination ->
                        ReduNavigationItem(
                            destination = destination,
                            selected = destination == selectedDestination,
                            onClick = { onDestinationSelected(destination) },
                            showLabel = false,
                        )
                    }
                }
            }
        },
    ) { measurables, constraints ->
        val pill = measurables[1].measure(constraints.copy(minWidth = 0, minHeight = 0))
        val spreadPx = surround.roundToPx()
        val surroundBlur = measurables[0].measure(
            Constraints.fixed(
                width = pill.width + spreadPx * 2,
                height = pill.height + spreadPx * 2,
            ),
        )
        // Report the pill size so content can scroll under the surrounding blur.
        layout(pill.width, pill.height) {
            surroundBlur.place(-spreadPx, -spreadPx)
            pill.place(0, 0)
        }
    }
}

@Composable
private fun NavSurroundBlur(
    hazeState: HazeState,
    spread: Dp,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clearAndSetSemantics {},
    ) {
        if (maxWidth > 0.dp && maxHeight > 0.dp) {
            val mask = rememberCapsuleHaloMask(
                width = maxWidth,
                height = maxHeight,
                spread = spread,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle(
                            backgroundColor = Color.Transparent,
                            tint = HazeTint(Color.Black.copy(alpha = 0.16f)),
                            blurRadius = 48.dp,
                            noiseFactor = 0.06f,
                            fallbackTint = HazeTint(Color.Transparent),
                        ),
                    ) {
                        this.mask = mask
                    },
            )
        }
    }
}

@Composable
private fun rememberCapsuleHaloMask(
    width: Dp,
    height: Dp,
    spread: Dp,
): Brush {
    val density = LocalDensity.current
    return remember(width, height, spread, density.density, density.fontScale) {
        val widthPx = with(density) { width.roundToPx() }.coerceAtLeast(1)
        val heightPx = with(density) { height.roundToPx() }.coerceAtLeast(1)
        val spreadPx = with(density) { spread.roundToPx() }.coerceAtLeast(1)
        ImageMaskBrush(capsuleHaloMask(widthPx, heightPx, spreadPx))
    }
}

private class ImageMaskBrush(private val image: ImageBitmap) : ShaderBrush() {
    override fun createShader(size: Size): Shader = ImageShader(image, TileMode.Clamp, TileMode.Clamp)
}

@Suppress("DEPRECATION")
private fun capsuleHaloMask(widthPx: Int, heightPx: Int, spreadPx: Int): ImageBitmap {
    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.BLACK
        maskFilter = BlurMaskFilter(spreadPx * 0.7f, BlurMaskFilter.Blur.NORMAL)
    }
    val rect = RectF(
        spreadPx.toFloat(),
        spreadPx.toFloat(),
        (widthPx - spreadPx).toFloat(),
        (heightPx - spreadPx).toFloat(),
    )
    val radius = rect.height() / 2f
    canvas.drawRoundRect(rect, radius, radius, paint)
    return bitmap.asImageBitmap()
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
    showLabel: Boolean = true,
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
                if (!showLabel) contentDescription = destination.label
            }
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = if (largeText) 4.dp else 8.dp, vertical = if (showLabel) 8.dp else 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .width(if (largeText) 64.dp else 56.dp)
                .height(32.dp)
                .background(
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    shape = ReduPill,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(destination.icon),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(ReduNavIconSize),
            )
        }
        if (showLabel) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = destination.label,
                style = if (largeText) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                color = contentColor,
                maxLines = 1,
            )
        }
    }
}

internal class ReduListMotion {
    var scrolling by mutableStateOf(false)
        private set

    fun update(value: Boolean) {
        scrolling = value
    }
}

private val IdleListMotion = ReduListMotion()

internal val LocalReduListMotion = compositionLocalOf { IdleListMotion }

@Composable
internal fun ReduScreen(
    padding: PaddingValues,
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    wash: Boolean = true,
    quietTitle: Boolean = false,
    pinHeader: Boolean = false,
    backdrop: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: LazyListScope.() -> Unit,
) {
    val listState = rememberLazyListState()
    val motion = remember { ReduListMotion() }
    LaunchedEffect(listState, motion) {
        snapshotFlow { listState.isScrollInProgress }.collect(motion::update)
    }
    val layoutDirection = LocalLayoutDirection.current
    val topPadding = padding.calculateTopPadding()
    val startPadding = padding.calculateStartPadding(layoutDirection)
    val endPadding = padding.calculateEndPadding(layoutDirection)
    val bottomPadding = padding.calculateBottomPadding()
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        CompositionLocalProvider(LocalReduListMotion provides motion) {
            backdrop?.invoke()
            if (wash) {
                ReduHeroWash()
            }
            if (pinHeader) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 760.dp)
                        .padding(start = startPadding, top = topPadding, end = endPadding),
                ) {
                    ReduPageHeader(
                        title = title,
                        subtitle = subtitle,
                        onBack = onBack,
                        quietTitle = quietTitle,
                        actions = actions,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, top = 20.dp, end = 20.dp),
                    )
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        state = listState,
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 40.dp + bottomPadding),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        content()
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 760.dp)
                        .padding(start = startPadding, top = topPadding, end = endPadding),
                    state = listState,
                    contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 40.dp + bottomPadding),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    item {
                        ReduPageHeader(
                            title = title,
                            subtitle = subtitle,
                            onBack = onBack,
                            quietTitle = quietTitle,
                            actions = actions,
                        )
                    }
                    content()
                }
            }
        }
    }
}

@Composable
internal fun ReduHeroWash(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                Brush.verticalGradient(
                    0f to ReduPalette.Figure.copy(alpha = 0.16f),
                    0.42f to ReduPalette.Figure.copy(alpha = 0.05f),
                    1f to Color.Transparent,
                ),
            ),
    )
}

@Composable
private fun ReduPageHeader(
    title: String,
    subtitle: String?,
    onBack: (() -> Unit)?,
    quietTitle: Boolean,
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(bottom = if (quietTitle) 16.dp else 28.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(painterResource(R.drawable.ic_back), contentDescription = "Back")
            }
        }
        Column(
            modifier = Modifier.weight(1f).padding(top = if (onBack == null) 0.dp else 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = if (quietTitle) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.headlineSmall,
                color = if (quietTitle) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
            subtitle?.let { ReduSecondaryText(it) }
        }
        Row(content = actions)
    }
}

@Composable
internal fun ReduSectionHeader(
    title: String,
    subtitle: String? = null,
    quiet: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
) {
    val stackTrailing = trailing != null && LocalDensity.current.fontScale >= 1.3f
    val modifier = Modifier.fillMaxWidth().padding(top = if (quiet) 22.dp else 28.dp, bottom = if (quiet) 8.dp else 12.dp)
    val titleStyle = if (quiet) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium
    val titleColor = if (quiet) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface

    if (stackTrailing) {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = titleStyle, color = titleColor)
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
                Text(title, style = titleStyle, color = titleColor)
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
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 0.dp,
    ) {
        Column(content = content)
    }
}

@Composable
internal fun ReduChip(
    label: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    containerColor: Color = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    },
    contentColor: Color = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    },
    leading: (@Composable () -> Unit)? = null,
) {
    val clickableModifier = if (onClick != null) {
        Modifier
            .heightIn(min = 40.dp)
            .semantics {
                role = Role.Button
                this.selected = selected
            }
            .clickable(role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }
    Surface(
        modifier = modifier.then(clickableModifier),
        color = containerColor,
        contentColor = contentColor,
        shape = ReduPill,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (onClick != null) 14.dp else 10.dp, vertical = if (onClick != null) 8.dp else 5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leading?.invoke()
            Text(
                label,
                style = if (onClick != null) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        }
    }
}

@Composable
internal fun ReduStatusLabel(label: String, tone: StatusTone) {
    val (container, content) = statusColors(tone)
    ReduChip(
        label = label,
        containerColor = container,
        contentColor = content,
        leading = {
            Box(Modifier.size(6.dp).background(statusIndicatorColor(tone), CircleShape))
        },
    )
}

@Composable
internal fun ReduAttentionBanner(
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    body: String? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReduStatusPalette.AttentionContainer,
        contentColor = ReduStatusPalette.OnAttentionContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        val largeText = LocalDensity.current.fontScale >= 1.3f
        val action = @Composable {
            ReduTextButton(
                text = actionLabel,
                onClick = onAction,
                contentColor = ReduStatusPalette.OnAttentionContainer,
            )
        }
        if (largeText) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(painterResource(R.drawable.ic_info), contentDescription = null, modifier = Modifier.size(ReduInlineIconSize))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(title, style = MaterialTheme.typography.titleSmall)
                        body?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    }
                }
                action()
            }
        } else {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.ic_info), contentDescription = null, modifier = Modifier.size(ReduInlineIconSize))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, style = MaterialTheme.typography.titleSmall)
                    body?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
                action()
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
    @DrawableRes icon: Int? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = ReduButtonMinHeight),
        enabled = enabled,
        shape = ReduPill,
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 14.dp),
    ) {
        if (icon != null) {
            Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(ReduInlineIconSize))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
internal fun ReduTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = ReduButtonMinHeight),
        enabled = enabled,
        shape = ReduPill,
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 14.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = contentColor),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
internal fun ReduTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = ReduPalette.SurfaceHighest,
            unfocusedContainerColor = ReduPalette.SurfaceHighest,
            disabledContainerColor = ReduPalette.Surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = ReduPalette.Figure,
            focusedLabelColor = ReduPalette.TextSecondary,
            unfocusedLabelColor = ReduPalette.TextSecondary,
            disabledLabelColor = ReduPalette.TextSecondary,
            focusedTextColor = ReduPalette.TextPrimary,
            unfocusedTextColor = ReduPalette.TextPrimary,
            disabledTextColor = ReduPalette.TextSecondary,
        ),
    )
}

@Composable
internal fun ReduOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    destructive: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = ReduButtonMinHeight),
        enabled = enabled,
        shape = ReduPill,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            disabledElevation = 0.dp,
        ),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (destructive) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            },
            contentColor = if (destructive) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
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
            checkedThumbColor = ReduPalette.OnAction,
            checkedTrackColor = ReduPalette.Action,
            checkedBorderColor = ReduPalette.Action,
            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            uncheckedBorderColor = MaterialTheme.colorScheme.surfaceContainerHighest,
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
                    painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    modifier = Modifier.size(ReduInlineIconSize),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun ReduLinearProgress(
    modifier: Modifier = Modifier,
    progress: (() -> Float)? = null,
) {
    val barModifier = modifier.fillMaxWidth().height(6.dp)
    val color = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    if (progress == null) {
        LinearProgressIndicator(
            modifier = barModifier,
            color = color,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round,
        )
    } else {
        LinearProgressIndicator(
            progress = progress,
            modifier = barModifier,
            color = color,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round,
        )
    }
}

@Composable
internal fun ActivityPatternMeter(
    score: Double,
    modifier: Modifier = Modifier,
    ringColor: Color = MaterialTheme.colorScheme.background,
    onLight: Boolean = false,
) {
    val normalized = score.coerceIn(0.0, 100.0).toFloat()
    val markerColor = when {
        normalized < 33.33f -> ReduStatusPalette.Normal
        normalized < 66.67f -> ReduStatusPalette.Elevated
        else -> ReduStatusPalette.Extended
    }
    val lowTrack = if (onLight) ReduPalette.Sage else ReduPalette.SageContainer
    val midTrack = if (onLight) ReduPalette.Warning else ReduPalette.WarningContainer
    val highTrack = if (onLight) ReduPalette.High else ReduPalette.HighContainer
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
            color = lowTrack,
            topLeft = Offset(0f, trackTop),
            size = androidx.compose.ui.geometry.Size(segmentWidth, trackHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2f),
        )
        drawRoundRect(
            color = midTrack,
            topLeft = Offset(segmentWidth + gap, trackTop),
            size = androidx.compose.ui.geometry.Size(segmentWidth, trackHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2f),
        )
        drawRoundRect(
            color = highTrack,
            topLeft = Offset((segmentWidth + gap) * 2f, trackTop),
            size = androidx.compose.ui.geometry.Size(segmentWidth, trackHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2f),
        )
        val markerX = (normalized / 100f) * size.width
        drawCircle(
            color = ringColor,
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
internal fun ActivityScoreRing(
    score: Double,
    modifier: Modifier = Modifier,
) {
    val normalized = score.coerceIn(0.0, 100.0).toFloat()
    val arcColor = when {
        normalized < 33.33f -> ReduStatusPalette.Normal
        normalized < 66.67f -> ReduStatusPalette.Elevated
        else -> ReduStatusPalette.Extended
    }
    Box(
        modifier = modifier.size(76.dp).semantics {
            contentDescription = "Activity pattern score ${normalized.toInt()} out of 100"
            progressBarRangeInfo = ProgressBarRangeInfo(normalized, 0f..100f)
        },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize().padding(4.dp)) {
            val stroke = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = ReduPalette.SurfaceHighest,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke,
            )
            if (normalized > 0f) {
                drawArc(
                    color = arcColor,
                    startAngle = -90f,
                    sweepAngle = 360f * (normalized / 100f),
                    useCenter = false,
                    style = stroke,
                )
            }
        }
        Text(
            text = normalized.toInt().toString(),
            style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
        )
    }
}

@Composable
internal fun ReduEmptyState(
    title: String,
    body: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ReduBlobatar(
            expression = BlobatarExpression.Sleepy,
            modifier = Modifier.size(120.dp),
        )
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        body?.let {
            Text(
                text = it,
                modifier = Modifier.widthIn(max = 420.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        if (actionLabel != null && onAction != null) {
            ReduPrimaryButton(text = actionLabel, onClick = onAction)
        }
    }
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
