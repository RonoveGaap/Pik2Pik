package com.example.tfgandroid.p2p

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.tfgandroid.activities.mainActivity.MainActivity
import com.example.tfgandroid.database.daos.GroupDao
import com.example.tfgandroid.database.daos.GuestDao
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.database.models.Guest
import com.example.tfgandroid.helpers.DateHelper
import com.example.tfgandroid.helpers.EnumHelper
import com.example.tfgandroid.repositories.P2PRepository
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class NearbyConnectionsManager(
    private val context: MainActivity,
    private val groupDao: GroupDao,
    private val guestDao: GuestDao
) {

//    private lateinit var mGroup: Group
//    private var groupsLivedata = MutableLiveData<ArrayList<Pair<String, String>>>()
//    private var groupsFound = ArrayList<Pair<String, String>>()
//
//    private lateinit var advertisingOptions: AdvertisingOptions
//    private lateinit var discoveryOptions: DiscoveryOptions
//    private var nearbyRole = EnumHelper.NearbyRole.UNDEFINED.role
//
//    private val mPayloadCallback: PayloadCallback = object : PayloadCallback() {
//        override fun onPayloadReceived(endpointId: String, payload: Payload) {
//            Log.d(P2PRepository.TAG, "Payload received")
//            val messageReceived = Json.decodeFromString<P2PMessage>(String(payload.asBytes()!!))
//            when (messageReceived.messageType) {
//                EnumHelper.P2PMessageType.SEND_GROUP_INFO.type -> {
//                    Log.d(P2PRepository.TAG, "Received information from a group with the name ${messageReceived.groupName!!}")
//                    if (nearbyRole == EnumHelper.NearbyRole.GUEST.role) {
//                        groupsFound.add(Pair(messageReceived.groupName, endpointId))
//                        groupsLivedata.value = groupsFound
//                    }
//                }
//                // TODO Pasar de GUEST a GROUP MEMBER Y PASAR LA INFORMACION EN EL MENSAJE DE CONFIRMACION. DESPUES ESTABLECER LA POLITICA DE ACTUALIZADO
//                EnumHelper.P2PMessageType.SEND_GUEST_INFO.type -> {
//                    Log.d(P2PRepository.TAG, "Received information from a guest with the name ${messageReceived.guestName!!}")
//                    if (nearbyRole == EnumHelper.NearbyRole.HOST.role) {
//                        CoroutineScope(Dispatchers.IO).launch {
//                            guestDao.insertGuest(Guest(null, messageReceived.guestName!!, endpointId, mGroup.id!!, DateHelper.getCurrentDateString(), null))
//                        }
//                        val messageToSend = Json.encodeToString(P2PMessage(EnumHelper.P2PMessageType.GROUP_JOINED_CONFIRMATION.type, null, null, null, null))
//                        val bytesPayload = Payload.fromBytes(messageToSend.toByteArray())
//                        Nearby.getConnectionsClient(context).sendPayload(endpointId, bytesPayload)
//                    }
//                }
//                EnumHelper.P2PMessageType.GROUP_JOINED_CONFIRMATION.type -> {
//                    Log.d(P2PRepository.TAG, "Received group join confirmation")
//                    if (nearbyRole == EnumHelper.NearbyRole.GUEST.role) {
//                        Nearby.getConnectionsClient(context).stopDiscovery()
//                    }
//                }
//                else -> {
//
//                }
//            }
//        }
//        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
//            Log.d(P2PRepository.TAG, "Payload transfer updated")
//        }
//    }
//
//    private val endpointDiscoveryCallback: EndpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
//        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
//            // An endpoint was found. We request a connection to it.
//            Nearby.getConnectionsClient(context)
//                .requestConnection("Pepe", endpointId, connectionLifecycleCallback)
//                .addOnSuccessListener {
//                    Log.d(P2PRepository.TAG, "Success from discovery endpoint. ID: $endpointId")
//                }
//                .addOnFailureListener {
//                    Log.d(P2PRepository.TAG, "Failure from discovery endpoint. ID: $endpointId")
//                }
//        }
//
//        override fun onEndpointLost(endpointId: String) {
//            Log.d(P2PRepository.TAG, "Endpoint lost. ID: $endpointId")
//        }
//    }
//
//    private val connectionLifecycleCallback: ConnectionLifecycleCallback = object : ConnectionLifecycleCallback() {
//        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
//            Log.d(P2PRepository.TAG, "Connection initiated. Endpoint ID: $endpointId")
//            Nearby.getConnectionsClient(context).acceptConnection(endpointId, mPayloadCallback)
//        }
//
//        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
//
//            when (result.status.statusCode) {
//                ConnectionsStatusCodes.STATUS_OK -> {
//                    Log.d(P2PRepository.TAG, "Connection result OK")
//                    if (nearbyRole == EnumHelper.NearbyRole.HOST.role) {
//                        val messageToSend = Json.encodeToString(P2PMessage(EnumHelper.P2PMessageType.SEND_GROUP_INFO.type, mGroup.name, null, null, null))
//                        val bytesPayload = Payload.fromBytes(messageToSend.toByteArray())
//                        Nearby.getConnectionsClient(context).sendPayload(endpointId, bytesPayload)
//                    }
//                }
//                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {Log.d(P2PRepository.TAG, "Connection result REJECTED")}
//                ConnectionsStatusCodes.STATUS_ERROR -> {Log.d(P2PRepository.TAG, "Connection result ERROR")}
//                else -> {}
//            }
//        }
//
//        override fun onDisconnected(endpointId: String) {
//            // We've been disconnected from this endpoint. No more data can be
//            // sent or received.
//            Log.d(P2PRepository.TAG, "Disconnected. Endpoint: $endpointId")
//            groupsFound.removeAll {
//                it.second == endpointId
//            }
//        }
//    }
//
//    private fun disconnectFromEndpoint(endpointId: String) {
//        Nearby.getConnectionsClient(context).disconnectFromEndpoint(endpointId)
//    }
//
//    fun createGroup(group: Group) {
//        CoroutineScope(Dispatchers.IO).launch {
//            val groupId = groupDao.insertGroup(group)
//            mGroup = groupDao.getGroupById(groupId)
//        }
//        advertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
//        Nearby.getConnectionsClient(context)
//            .startAdvertising("Pepe", context.packageName, connectionLifecycleCallback, advertisingOptions)
//            .addOnSuccessListener {
//                Log.d(P2PRepository.TAG, "Start advertising succeeded")
//                nearbyRole = EnumHelper.NearbyRole.HOST.role
//            }
//            .addOnFailureListener {
//                Log.d(P2PRepository.TAG, "Start advertising failed")
//            }
//    }
//
//    fun deleteGroup() {
//        Nearby.getConnectionsClient(context).stopAdvertising()
//        CoroutineScope(Dispatchers.IO).launch {
//            groupDao.closeGroup(mGroup.id!!, DateHelper.getCurrentDateString())
//        }
//    }
//
//    fun searchGroup() {
//        discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
//        Nearby.getConnectionsClient(context)
//            .startDiscovery(context.packageName, endpointDiscoveryCallback, discoveryOptions)
//            .addOnSuccessListener {
//                Log.d(P2PRepository.TAG, "Start discovery succeeded")
//                nearbyRole = EnumHelper.NearbyRole.GUEST.role
//            }
//            .addOnFailureListener {
//                Log.d(P2PRepository.TAG, "Start discovery failed")
//            }
//    }
//
//    fun joinGroup(endpointId: String) {
//        val messageToSend = Json.encodeToString(P2PMessage(EnumHelper.P2PMessageType.SEND_GUEST_INFO.type, null, "Benito Camelas", null, null))
//        val bytesPayload = Payload.fromBytes(messageToSend.toByteArray())
//        Nearby.getConnectionsClient(context).sendPayload(endpointId, bytesPayload)
//    }


}