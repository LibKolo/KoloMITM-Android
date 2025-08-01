package io.githun.mucute.qwq.kolomitm.model

import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession

@Suppress("ConstPropertyName")
data class Account(
    val session: StepFullBedrockSession.FullBedrockSession,
    val deviceType: String
) {

    companion object {

        const val Android = "Android"

        const val iOS = "iOS"

        const val Nintendo = "Nintendo"

    }

}