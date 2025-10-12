package io.github.mucute.qwq.kolomitm.manager

import com.google.gson.JsonParser
import io.github.mucute.qwq.kolomitm.application.AppContext
import io.github.mucute.qwq.kolomitm.model.Account
import io.github.mucute.qwq.kolomitm.util.createHttpClient
import io.github.mucute.qwq.kolomitm.util.fetchBedrockAuthByDeviceType
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode
import java.io.File

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
                    val nameWithoutExtension = child.nameWithoutExtension
                    val displayName = nameWithoutExtension.substringBefore('_')
                    val deviceType = nameWithoutExtension.substringAfter('_')
                    accountList.add(parseAccount(displayName, deviceType))
                }
            }

            _accounts.update { accountList }

            val selectedAccountFile = File(accountsFolder, "selectedAccount.txt")
            if (selectedAccountFile.exists()) {
                val content = selectedAccountFile.readText()
                val displayName = content.substringBefore('_')
                val deviceType = content.substringAfter('_')
                _selectedAccount.update {
                    accountList.find { it.session.mcChain.displayName == displayName && it.deviceType == deviceType }
                }
            }
        }
    }

    private fun parseAccount(displayName: String, deviceType: String): Account {
        val accountsFolder = File(AppContext.instance.filesDir, "accounts")
        accountsFolder.mkdirs()

        val accountFile = File(accountsFolder, "${displayName}_${deviceType}.json")
        val session = fetchBedrockAuthByDeviceType(deviceType).fromJson(JsonParser.parseString(accountFile.readText()).asJsonObject)

        return Account(
            session,
            deviceType
        )
    }

    fun addAccount(
        deviceType: String,
        msaDeviceCodeCallback: StepMsaDeviceCode.MsaDeviceCodeCallback,
        callback: (Throwable?) -> Unit
    ) {
        coroutineScope.launch(CoroutineExceptionHandler { _, throwable ->
            callback(throwable)
        }) {
            val stepFullBedrockSession = fetchBedrockAuthByDeviceType(deviceType)

            val session = stepFullBedrockSession.getFromInput(
                createHttpClient(),
                msaDeviceCodeCallback
            )

            val account = Account(session, deviceType)

            _accounts.update { it + account }

            val accountsFolder = File(AppContext.instance.filesDir, "accounts")
            accountsFolder.mkdirs()

            val accountFile = File(accountsFolder, "${account.session.mcChain.displayName}_${deviceType}.json")
            accountFile.writeText(stepFullBedrockSession.toJson(account.session).toString())

            callback(null)
        }
    }

    fun removeAccount(account: Account) {
        coroutineScope.launch {
            _accounts.update {
                it.minus(account)
            }

            val accountsFolder = File(AppContext.instance.filesDir, "accounts")
            accountsFolder.mkdirs()

            _selectedAccount.update {
                if (it != account) {
                    it
                } else {
                    val selectedAccountFile = File(accountsFolder, "selectedAccount.txt")
                    if (selectedAccountFile.exists()) {
                        selectedAccountFile.delete()
                    }
                    null
                }
            }

            val accountFile = File(accountsFolder, "${account.session.mcChain.displayName}_${account.deviceType}.json")
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
            selectedAccountFile.writeText("${account.session.mcChain.displayName}_${account.deviceType}")
        } else if (selectedAccountFile.exists()) {
            selectedAccountFile.delete()
        }
    }

    fun saveAccount(account: Account) {
        coroutineScope.launch {
            val stepFullBedrockSession = fetchBedrockAuthByDeviceType(account.deviceType)

            val accountsFolder = File(AppContext.instance.filesDir, "accounts")
            accountsFolder.mkdirs()

            val accountFile = File(accountsFolder, "${account.session.mcChain.displayName}_${account.deviceType}.json")
            accountFile.writeText(stepFullBedrockSession.toJson(account.session).toString())
        }
    }

}