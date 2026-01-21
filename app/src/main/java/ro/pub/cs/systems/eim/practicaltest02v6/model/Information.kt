package ro.pub.cs.systems.eim.practicaltest02v6.model

import java.time.Instant
import java.time.LocalDateTime

data class Information (
    val value: String,
    val updated: Instant
)