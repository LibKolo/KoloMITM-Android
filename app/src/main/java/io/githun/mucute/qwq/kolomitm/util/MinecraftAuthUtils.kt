package io.githun.mucute.qwq.kolomitm.util

import net.lenni0451.commons.httpclient.HttpClient
import net.lenni0451.commons.httpclient.RetryHandler
import net.lenni0451.commons.httpclient.constants.ContentTypes
import net.lenni0451.commons.httpclient.constants.Headers
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession
import net.raphimc.minecraftauth.util.MicrosoftConstants
import net.raphimc.minecraftauth.util.OAuthEnvironment

const val DeviceTypeAndroid = "Android"
val BedrockAndroidAuth = createBedrockAuth(MicrosoftConstants.BEDROCK_ANDROID_TITLE_ID, DeviceTypeAndroid)

const val DeviceTypeIos = "iOS"
val BedrockIosAuth = createBedrockAuth(MicrosoftConstants.BEDROCK_IOS_TITLE_ID, DeviceTypeIos)

const val DeviceTypeNintendo = "Nintendo"
val BedrockNintendoAuth = createBedrockAuth(MicrosoftConstants.BEDROCK_NINTENDO_TITLE_ID, DeviceTypeNintendo)

fun fetchBedrockAuthByDeviceType(deviceType: String): StepFullBedrockSession {
    return when (deviceType) {
        DeviceTypeAndroid -> BedrockAndroidAuth
        DeviceTypeIos -> BedrockIosAuth
        else -> BedrockNintendoAuth
    }
}

fun fetchDeviceTypeByBedrockAuth(bedrockAuth: StepFullBedrockSession): String {
    return if (bedrockAuth === BedrockAndroidAuth) {
        DeviceTypeAndroid
    } else if (bedrockAuth === BedrockIosAuth) {
        DeviceTypeIos
    } else {
        DeviceTypeNintendo
    }
}

private const val TIMEOUT = 1000

fun createHttpClient(): HttpClient {
    return HttpClient()
        .setConnectTimeout(TIMEOUT)
        .setReadTimeout(TIMEOUT * 2)
        .setCookieManager(null)
        .setFollowRedirects(false)
        .setRetryHandler(RetryHandler(0, 200))
        .setHeader(Headers.ACCEPT, ContentTypes.APPLICATION_JSON.toString())
        .setHeader(Headers.ACCEPT_LANGUAGE, "en-US,en")
        .setHeader(Headers.USER_AGENT, MinecraftAuth.USER_AGENT)
}

private fun createBedrockAuth(
    clientId: String,
    deviceType: String
): StepFullBedrockSession = MinecraftAuth.builder()
    .withClientId(clientId)
    .withScope(MicrosoftConstants.SCOPE_TITLE_AUTH)
    .deviceCode()
    .withDeviceToken(deviceType)
    .sisuTitleAuthentication(MicrosoftConstants.BEDROCK_XSTS_RELYING_PARTY)
    .buildMinecraftBedrockChainStep(true, true)