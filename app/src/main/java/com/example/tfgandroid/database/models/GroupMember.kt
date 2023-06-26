package com.example.tfgandroid.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.tfgandroid.p2p.models.ImageFile

@kotlinx.serialization.Serializable
@Entity(tableName = "group_member")
data class GroupMember(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "endpoint_name") val endpointName: String,
    @ColumnInfo(name = "is_host") val isHost: Boolean,
    @ColumnInfo(name = "group_id") var groupId: Long,
    @ColumnInfo(name = "endpoint_id") var endpointId: String?,
    @ColumnInfo(name = "connected_at") var connectedAt: String?,
    @ColumnInfo(name = "disconnected_at") var disconnectedAt: String?,
    @Ignore var images: ArrayList<ImageFile>
) {
    constructor(name: String, endpointName: String, isHost: Boolean, groupId: Long, endpointId: String?, connectedAt: String?):
            this(null, name, endpointName, isHost, groupId, endpointId, connectedAt, null, ArrayList())
}
