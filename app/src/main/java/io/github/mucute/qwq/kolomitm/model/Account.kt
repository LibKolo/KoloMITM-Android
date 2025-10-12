package io.github.mucute.qwq.kolomitm.model

import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession

data class Account(
    val session: StepFullBedrockSession.FullBedrockSession,
    val deviceType: String
)