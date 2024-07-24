/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.appbasics.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min


@Composable
fun SplitView (
    minHeightTop: Dp = 100.dp,
    minHeightBottom: Dp = 100.dp,
    topView: @Composable() ()->Unit,
    bottomView: @Composable() () ->Unit,
    initialTopHeightWeight: Float = 0.5f,
) {

    val density = LocalDensity.current

    BoxWithConstraints {

        val height = with(density) { constraints.maxHeight.toDp() }

        val maxPos = height - minHeightBottom

        var barPosition: Dp by remember { mutableStateOf(height * initialTopHeightWeight) }

        val barThickness = 15.dp
        val handleThickness = 12.dp
        val handleWidth = 50.dp
        val roundedCornerShape = 5.dp

        val background = Color(0xfff6f8fa)
        val border = Color(0xffe1e4e8)

        Box () {
            Column() {
                Box(
                    Modifier.fillMaxWidth().height(barPosition)) {
                    topView()
                }
                Spacer(
                    modifier = Modifier.shadow(elevation = 2.dp).height(1.dp).background(border)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height - barPosition - barThickness),
                ) {
                    bottomView()
                }
            }

            Column(modifier = Modifier.align(Alignment.Center)) {
                Spacer(
                    modifier = Modifier.height(
                        barPosition - (barThickness / 2) + 2.dp
                    )
                )
                Box(
                    Modifier
                    .height(handleThickness)
                    .width(handleWidth)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(roundedCornerShape))
                    .border(1.dp, border)
                    .shadow(5.dp)
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            val shiftedPos = barPosition + with(density) { delta.toDp() }
                            barPosition = max(min(shiftedPos, maxPos), minHeightTop)
                        },
                        startDragImmediately = true,
                    )
                    .background(background),
                )

                Spacer(modifier = Modifier.fillMaxHeight())
            }
        }
    }
}