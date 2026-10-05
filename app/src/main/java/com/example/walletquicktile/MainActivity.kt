package com.example.walletquicktile

import android.annotation.SuppressLint
import android.app.Activity
import android.app.StatusBarManager
import android.content.ComponentName
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val addTileButton = findViewById<Button>(R.id.addTileButton)
        val compatibilityText = findViewById<TextView>(R.id.compatibilityText)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            addTileButton.setOnClickListener { requestAddTile() }
            compatibilityText.visibility = View.GONE
        } else {
            addTileButton.visibility = View.GONE
            compatibilityText.visibility = View.VISIBLE
        }
    }

    @SuppressLint("WrongConstant")
    private fun requestAddTile() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val statusBarManager = getSystemService(StatusBarManager::class.java)
        if (statusBarManager == null) {
            toast(R.string.tile_request_unavailable)
            return
        }

        try {
            statusBarManager.requestAddTileService(
                ComponentName(this, WalletTileService::class.java),
                getString(R.string.tile_label),
                Icon.createWithResource(this, R.drawable.ic_wallet),
                mainExecutor
            ) { result ->
                val message = when (result) {
                    StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED ->
                        R.string.tile_added
                    StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED ->
                        R.string.tile_already_added
                    StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED ->
                        R.string.tile_not_added
                    else -> R.string.tile_request_failed
                }
                toast(message)
            }
        } catch (_: IllegalArgumentException) {
            toast(R.string.tile_request_failed)
        } catch (_: SecurityException) {
            toast(R.string.tile_request_failed)
        } catch (_: RuntimeException) {
            toast(R.string.tile_request_failed)
        }
    }

    private fun toast(messageRes: Int) {
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
    }
}
