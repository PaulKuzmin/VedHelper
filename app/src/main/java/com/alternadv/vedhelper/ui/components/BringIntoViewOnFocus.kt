package com.alternadv.vedhelper.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalDensity

/**
 * Держит поле в фокусе видимым над клавиатурой: при фокусе и на каждом шаге анимации
 * клавиатуры (меняется высота IME, а с ней — imePadding формы) просит прокручиваемого
 * родителя показать поле целиком. Без этого поле внизу формы (например «Мощность»)
 * оставалось под клавиатурой: фокус приходит раньше, чем форма успевает сжаться.
 */
@Composable
fun Modifier.bringIntoViewOnFocus(): Modifier {
    val requester = remember { BringIntoViewRequester() }
    var focused by remember { mutableStateOf(false) }
    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)

    LaunchedEffect(focused, imeBottom) {
        if (focused) requester.bringIntoView()
    }

    return this
        .bringIntoViewRequester(requester)
        .onFocusEvent { focused = it.isFocused }
}
