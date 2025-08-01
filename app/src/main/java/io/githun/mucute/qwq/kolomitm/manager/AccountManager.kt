package io.githun.mucute.qwq.kolomitm.manager

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.githun.mucute.qwq.kolomitm.application.AppContext
import io.githun.mucute.qwq.kolomitm.model.Account
import io.githun.mucute.qwq.kolomitm.util.BedrockAndroidAuth
import io.githun.mucute.qwq.kolomitm.util.BedrockIosAuth
import io.githun.mucute.qwq.kolomitm.util.BedrockNintendoAuth
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode
import java.io.File
import kotlin.collections.emptyList

object AccountManager {

    private val coroutineScope =
        CoroutineScope(Dispatchers.IO + CoroutineName("AccountManagerCoroutine") + SupervisorJob())

    private val _accounts: MutableStateFlow<List<Account>> = MutableStateFlow(emptyList())

    private val _selectedAccount: MutableStateFlow<Account?> = MutableStateFlow(null)

    val accounts = _accounts.asStateFlow()

    val selectedAccount = _selectedAccount.asStateFlow()

    fun fetchAccounts() {
        coroutineScope.launch(Dispatchers.Main) {
            val accountList = ArrayList<Account>()
            val accountsFolder = File(AppContext.instance.filesDir, "accounts")
            accountsFolder.mkdirs()

            val children = accountsFolder.listFiles() ?: emptyArray()
            for (child in children) {
                if (child.name.endsWith(".json")) {
                    accountList.add(parseAccount(child.nameWithoutExtension))
                }
            }

            _accounts.update { accountList }

            val selectedAccountFile = File(accountsFolder, "selectedAccount.txt")
            if (selectedAccountFile.exists()) {
                val displayName = selectedAccountFile.readText()
                _selectedAccount.update {
                    accountList.find { it.session.mcChain.displayName == displayName }
                }
            }
        }
    }

    private fun parseAccount(displayName: String): Account {
        val accountsFolder = File(AppContext.instance.filesDir, "accounts")
        accountsFolder.mkdirs()

        val accountFile = File(accountsFolder, "${displayName}.json")
        val jsonObject = JsonParser.parseString(accountFile.readText()).asJsonObject
        val deviceType = jsonObject["deviceType"].asInt
        val stepFullBedrockSession = when (deviceType) {
            0 -> BedrockAndroidAuth
            1 -> BedrockIosAuth
            else -> BedrockNintendoAuth
        }

        val session = stepFullBedrockSession.fromJson(jsonObject["session"].asJsonObject)
        return Account(
            session,
            when (deviceType) {
                0 -> "Android"
                1 -> "iOS"
                else -> "Nintendo"
            }
        )
    }

    fun addAccount(
        deviceType: Int,
        msaDeviceCodeCallback: StepMsaDeviceCode.MsaDeviceCodeCallback,
        callback: (Throwable?) -> Unit
    ) {
        coroutineScope.launch(CoroutineExceptionHandler { _, throwable ->
            callback(throwable)
        }) {
            val stepFullBedrockSession = when (deviceType) {
                0 -> BedrockAndroidAuth
                1 -> BedrockIosAuth
                else -> BedrockNintendoAuth
            }

            val session = stepFullBedrockSession.getFromInput(
                MinecraftAuth.createHttpClient(),
                msaDeviceCodeCallback
            )

            val account = Account(
                session, when (deviceType) {
                    0 -> "Android"
                    1 -> "iOS"
                    else -> "Nintendo"
                }
            )

            _accounts.update { it + account }

            val accountsFolder = File(AppContext.instance.filesDir, "accounts")
            accountsFolder.mkdirs()

            val accountFile = File(accountsFolder, "${account.session.mcChain.displayName}.json")
            accountFile.writeText(JsonObject().apply {
                addProperty("deviceType", deviceType)
                add("session", stepFullBedrockSession.toJson(account.session))
            }.toString())

            callback(null)
        }
    }

    fun removeAccount(account: Account) {
        coroutineScope.launch {
            _accounts.update {
                it.minus(account)
            }

            _selectedAccount.update {
                if (it != account) {
                    it
                } else {
                    null
                }
            }

            val accountsFolder = File(AppContext.instance.filesDir, "accounts")
            accountsFolder.mkdirs()

            val accountFile = File(accountsFolder, "${account.session.mcChain.displayName}.json}")
            accountFile.delete()
        }
    }

    fun selectAccount(account: Account?) {
        coroutineScope.launch {
            _selectedAccount.update { account }
        }

        val accountsFolder = File(AppContext.instance.filesDir, "accounts")
        accountsFolder.mkdirs()

        val selectedAccountFile = File(accountsFolder, "selectedAccount.txt")
        if (account != null) {
            selectedAccountFile.writeText(account.session.mcChain.displayName)
        } else {
            selectedAccountFile.delete()
        }
    }

}