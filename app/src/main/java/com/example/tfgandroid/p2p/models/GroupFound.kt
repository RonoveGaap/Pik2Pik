package com.example.tfgandroid.p2p.models

import com.example.tfgandroid.database.models.Image

data class GroupFound(
    val groupName: String, val groupOwner: String, val size: String, var endpointId: String, val groupPrefix: String, val status: Int
)