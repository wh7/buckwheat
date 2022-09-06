package com.danilkinkin.buckwheat.base

import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.ui.BuckwheatTheme

@Composable
fun CheckedRow(
    checked: Boolean,
    onValueChange: (isChecked: Boolean) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
) {
    TextRow(
        modifier = modifier
            .toggleable(
                value = checked,
                onValueChange = { onValueChange(!checked) },
                role = Role.Checkbox
            ),
        icon = if (checked) painterResource(R.drawable.ic_apply) else null,
        text = text,
    )
}

@Preview
@Composable
fun PreviewCheckedRow() {
    val (checkedState, onStateChange) = remember { mutableStateOf(false) }

    BuckwheatTheme {
        CheckedRow(
            checked = checkedState,
            onValueChange = { onStateChange(it) },
            text = "Option selection",
        )
    }
}

@Preview
@Composable
fun PreviewCheckedRowChekecd() {
    val (checkedState, onStateChange) = remember { mutableStateOf(true) }

    BuckwheatTheme {
        CheckedRow(
            checked = checkedState,
            onValueChange = { onStateChange(it) },
            text = "Option selection",
        )
    }
}