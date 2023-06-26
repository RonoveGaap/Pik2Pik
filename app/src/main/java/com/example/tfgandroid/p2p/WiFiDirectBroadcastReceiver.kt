package com.example.tfgandroid.p2p

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.tfgandroid.activities.mainActivity.MainActivity

class WiFiDirectBroadcastReceiver(
    private val manager: WifiP2pManager?,
    private val channel: WifiP2pManager.Channel
): BroadcastReceiver() {

    companion object {
        const val TAG = "WiFiDirectBroadcastRec"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive")
        when (intent.action.toString()) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                // Check to see if Wi-Fi is enabled and notify appropriate activity
                when (intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)) {
                    WifiP2pManager.WIFI_P2P_STATE_ENABLED -> {
                        // Wifi P2P is enabled
                        // Toast.makeText(context, "Wifi P2P is enabled", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // Wi-Fi P2P is not enabled
                        // Toast.makeText(context, "Wifi P2P is not enabled", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                Log.d(TAG, "asda")
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(context, "Permission not granted", Toast.LENGTH_LONG).show()
                    return
                }
                manager?.requestPeers(channel) { peers: WifiP2pDeviceList? ->
                    Log.d(TAG, "Peer devices list ready")
//                    (context as MainActivity).showDevicesList(peers!!)
                }
            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                // Respond to new connection or disconnections
            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                // Respond to this device's wifi state changing
            }
        }

    }
}