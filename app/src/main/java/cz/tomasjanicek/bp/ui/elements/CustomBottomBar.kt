package cz.tomasjanicek.bp.ui.elements

import androidx.compose.ui.graphics.painter.Painter

data class BottomNavigationItem(
    val title: String,
    val icon: Painter,
    val hasNews: Boolean = false,
    val badgeCount: Int? = null
)
