package org.ncgroup.kscan

import androidx.compose.runtime.Composable

@Composable
fun App() = RegionOfInterestUI()

expect fun getPlatformName(): String
