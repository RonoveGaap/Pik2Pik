package com.example.tfgandroid.p2p

import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.database.models.GroupMember
import com.example.tfgandroid.database.models.Image
import com.example.tfgandroid.helpers.EnumHelper

@kotlinx.serialization.Serializable
data class P2PMessage(
    val messageType: Int,
    val groupName: String?,
    val guestName: String?,
    val fullGroupInfo: Group?,
    val fullMemberList: ArrayList<GroupMember>?,
    val memberEndpointName: String?,
    val sessionStatus: Int?,
    val image: Image?,
    val file: ByteArray?,
    val chunkIndex: Int?,
    val totalChunks: Int?
) {
    companion object {
        fun groupInfoMessage(groupName: String) = P2PMessage(
            EnumHelper.P2PMessageType.SEND_GROUP_INFO.type, groupName, null, null, null, null, null, null, null, null, null
        )

        fun guestInfoMessage(guestName: String) = P2PMessage(
            EnumHelper.P2PMessageType.SEND_GUEST_INFO.type, null, guestName, null, null, null, null, null, null, null, null
        )

        fun groupJoinedConfirmationMessage(group: Group, memberList: ArrayList<GroupMember>, endpointName: String) = P2PMessage(
            EnumHelper.P2PMessageType.GROUP_JOINED_CONFIRMATION.type, null, null, group, memberList, endpointName, null, null, null, null, null
        )

        fun notifySessionStatusChanged(status: Int) = P2PMessage(
            EnumHelper.P2PMessageType.SESSION_STATUS_CHANGED.type, null, null, null, null, null, status, null, null, null, null
        )

        fun notifyDisconnectMessage() = P2PMessage(
            EnumHelper.P2PMessageType.DISCONNECT.type, null, null, null, null, null, null, null, null, null, null
        )

        fun requestPhotosMessage() = P2PMessage(
            EnumHelper.P2PMessageType.REQUEST_NEW_PHOTOS.type, null, null, null, null, null, null, null, null, null, null
        )

        fun sendPhotosMessage(image: Image, file: ByteArray, cIndex: Int, tChunks: Int) = P2PMessage(
            EnumHelper.P2PMessageType.SEND_NEW_PHOTOS.type, null, null, null, null, null, null, image, file, cIndex, tChunks
        )

        fun sendChunkReceivedSuccessfullyMessage(image: Image, cIndex: Int) = P2PMessage(
            EnumHelper.P2PMessageType.CHUNK_RECEIVED_SUCCESSFULLY.type, null, null, null, null, null, null, image, null, cIndex, null
        )

        fun sendIntentionToLeaveMessage() = P2PMessage(
            EnumHelper.P2PMessageType.NOTIFY_INTENTION_TO_LEAVE.type, null, null, null, null, null, null, null, null, null, null
        )

        fun sendGroupMembersUpdateMessage(memberList: ArrayList<GroupMember>) = P2PMessage(
            EnumHelper.P2PMessageType.UPDATE_GROUP_MEMBERS.type, null, null, null, memberList, null, null, null, null, null, null
        )

        fun sendGroupMembersUpdateReceivedMessage() = P2PMessage(
            EnumHelper.P2PMessageType.UPDATE_GROUP_MEMBERS_RECEIVED.type, null, null, null, null, null, null, null, null, null, null
        )

        fun sendChooseNewHostMessage() = P2PMessage(
            EnumHelper.P2PMessageType.CHOOSE_NEW_HOST.type, null, null, null, null, null, null, null, null, null, null
        )

        fun sendChooseNewHostReceivedMessage() = P2PMessage(
            EnumHelper.P2PMessageType.CHOOSE_NEW_HOST_RECEIVED.type, null, null, null, null, null, null, null, null, null, null
        )

        fun sendCloseGroupMessage() = P2PMessage(
            EnumHelper.P2PMessageType.CLOSE_GROUP.type, null, null, null, null, null, null, null, null, null, null
        )

    }
}

