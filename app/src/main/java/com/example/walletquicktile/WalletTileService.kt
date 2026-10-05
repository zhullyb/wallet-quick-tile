package com.example.walletquicktile

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

class WalletTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.apply {
            label = getString(R.string.tile_label)
            state = Tile.STATE_INACTIVE
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            launchWithPendingIntent()
        } else {
            launchWithIntent()
        }
    }

    @TargetApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun launchWithPendingIntent() {
        if (!tryLaunchWithPendingIntent(
                WalletSettingsIntents.bestEntry(this),
                REQUEST_CODE_WALLET_SETTINGS
            )) {
            showUnavailableMessage()
        }
    }

    @TargetApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun tryLaunchWithPendingIntent(intent: Intent, requestCode: Int): Boolean =
        try {
            val pendingIntent = PendingIntent.getActivity(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        } catch (_: RuntimeException) {
            // Keep vendor-specific framework failures from crashing the service process.
            false
        }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    @Suppress("DEPRECATION")
    private fun launchWithIntent() {
        for (intent in WalletSettingsIntents.candidates()) {
            try {
                startActivityAndCollapse(intent)
                return
            } catch (_: ActivityNotFoundException) {
                // Try the next system destination.
            } catch (_: SecurityException) {
                // Some ROMs contain the activity but don't export it to third-party apps.
            } catch (_: RuntimeException) {
                // Continue to the direct compatibility entry for vendor framework failures.
            }
        }
        showUnavailableMessage()
    }

    private fun showUnavailableMessage() {
        Toast.makeText(this, R.string.wallet_settings_unavailable, Toast.LENGTH_LONG).show()
    }

    private companion object {
        const val REQUEST_CODE_WALLET_SETTINGS = 100
    }
}
