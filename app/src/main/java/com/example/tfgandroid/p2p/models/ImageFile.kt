package com.example.tfgandroid.p2p.models

import com.example.tfgandroid.database.models.Image

@kotlinx.serialization.Serializable
data class ImageFile(val image: Image, val files: ArrayList<Chunk>) {

}
