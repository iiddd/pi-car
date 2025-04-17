package server.data.status

import kotlinx.serialization.Serializable

@Serializable
data class StatusResponse(
    val status: String,
    val mockMode: Boolean
)