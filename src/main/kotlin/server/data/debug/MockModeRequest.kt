package server.data.debug

import kotlinx.serialization.Serializable

@Serializable
data class MockModeRequest(val mockMode: Boolean)