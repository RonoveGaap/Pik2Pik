package com.example.tfgandroid.helpers

class EnumHelper {
    enum class NearbyRole(val role: Int) {
        UNDEFINED(0),
        HOST(1),
        GUEST(2)
    }

    enum class SessionStatus(val status: Int) {
        UNDEFINED(0),
        STARTING(1),
        STARTED(2),
        FINISHED(3),
        LEAVING(4),
        LEFT(5),
    }

    enum class JoinGroupCode(val code: Int) {
        SUCCESS(0),
        ERROR(1)
    }

    enum class P2PMessageType(val type: Int) {
        UNDEFINED(0),
        SEND_GROUP_INFO(1),
        SEND_GUEST_INFO(2),
        GROUP_JOINED_CONFIRMATION(3),
        SESSION_STATUS_CHANGED(4),
        DISCONNECT(5),
        REQUEST_NEW_PHOTOS(6),
        SEND_NEW_PHOTOS(7),
        CHUNK_RECEIVED_SUCCESSFULLY(8),
        NOTIFY_INTENTION_TO_LEAVE(9),
        UPDATE_GROUP_MEMBERS(10),
        UPDATE_GROUP_MEMBERS_RECEIVED(11),
        CHOOSE_NEW_HOST(12),
        CHOOSE_NEW_HOST_RECEIVED(13),
        CLOSE_GROUP(14)
    }
}