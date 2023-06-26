package com.example.tfgandroid.p2p.models

import com.example.tfgandroid.database.models.Image

@kotlinx.serialization.Serializable
data class Chunk(var isSynchronized: Boolean, var bytes: ByteArray?) {

}
