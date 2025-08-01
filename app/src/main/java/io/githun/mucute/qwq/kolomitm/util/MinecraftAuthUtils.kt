package io.githun.mucute.qwq.kolomitm.util

import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession
import net.raphimc.minecraftauth.util.MicrosoftConstants

val BedrockAndroidAuth = createBedrockAuth(MicrosoftConstants.BEDROCK_ANDROID_TITLE_ID, "Android")

val BedrockIosAuth = createBedrockAuth(MicrosoftConstants.BEDROCK_IOS_TITLE_ID, "iOS")

val BedrockNintendoAuth = createBedrockAuth(MicrosoftConstants.BEDROCK_NINTENDO_TITLE_ID, "Nintendo")

private fun createBedrockAuth(
    clientId: String,
    deviceType: String
): StepFullBedrockSession = MinecraftAuth.builder()
    .withClientId(clientId)
    .withScope(MicrosoftConstants.SCOPE_TITLE_AUTH)
    .deviceCode()
    .withDeviceToken(deviceType)
    .sisuTitleAuthentication(MicrosoftConstants.BEDROCK_XSTS_RELYING_PARTY)
    .buildMinecraftBedrockChainStep(true, false)