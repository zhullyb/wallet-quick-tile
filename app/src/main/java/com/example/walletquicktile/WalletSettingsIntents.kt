package com.example.walletquicktile

import android.content.ComponentName
import android.content.Context
import android.content.Intent

/** Builds the system intents used by both the app screen and the Quick Settings tile. */
object WalletSettingsIntents {
    const val ROLE_NAME_EXTRA = "android.intent.extra.ROLE_NAME"
    const val WALLET_ROLE = "android.app.role.WALLET"

    private const val PERMISSION_CONTROLLER_PACKAGE = "com.android.permissioncontroller"
    private const val DEFAULT_APP_ACTIVITY =
        "com.android.permissioncontroller.role.ui.DefaultAppActivity"
    private const val WALLET_ROLE_TRAMPOLINE_ACTIVITY =
        "com.android.permissioncontroller.role.ui.v35.ChangeDefaultCardEmulationActivity"
    private const val ACTION_CHANGE_DEFAULT_CARD_EMULATION =
        "android.nfc.cardemulation.action.ACTION_CHANGE_DEFAULT"

    /**
     * Public Android 15+ system entry point. PermissionController forwards this to
     * DefaultAppActivity with ROLE_WALLET from inside its own privileged process.
     */
    fun walletRoleTrampoline(): Intent = Intent(ACTION_CHANGE_DEFAULT_CARD_EMULATION).apply {
        component = ComponentName(
            PERMISSION_CONTROLLER_PACKAGE,
            WALLET_ROLE_TRAMPOLINE_ACTIVITY
        )
        addCategory(Intent.CATEGORY_DEFAULT)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun primary(): Intent = Intent().apply {
        component = ComponentName(PERMISSION_CONTROLLER_PACKAGE, DEFAULT_APP_ACTIVITY)
        putExtra(ROLE_NAME_EXTRA, WALLET_ROLE)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    /** Prefer the exported system trampoline when this PermissionController provides it. */
    @Suppress("DEPRECATION")
    fun bestEntry(context: Context): Intent {
        val trampoline = walletRoleTrampoline()
        return if (context.packageManager.resolveActivity(trampoline, 0) != null) {
            trampoline
        } else {
            primary()
        }
    }

    fun candidates(): List<Intent> = listOf(walletRoleTrampoline(), primary())
}
