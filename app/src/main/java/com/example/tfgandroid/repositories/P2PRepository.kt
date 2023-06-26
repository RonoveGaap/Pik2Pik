package com.example.tfgandroid.repositories

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tfgandroid.activities.mainActivity.MainActivity
import com.example.tfgandroid.database.daos.GroupDao
import com.example.tfgandroid.database.daos.GroupMemberDao
import com.example.tfgandroid.database.daos.ImageDao
import com.example.tfgandroid.database.daos.VideoDao
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.database.models.GroupMember
import com.example.tfgandroid.database.models.Video
import com.example.tfgandroid.helpers.DateHelper
import com.example.tfgandroid.helpers.EnumHelper
import com.example.tfgandroid.helpers.GroupHelper
import com.example.tfgandroid.helpers.SharedPreferencesHelper
import com.example.tfgandroid.models.MyGroup
import com.example.tfgandroid.p2p.P2PMessage
import com.example.tfgandroid.p2p.models.Chunk
import com.example.tfgandroid.p2p.models.GroupFound
import com.example.tfgandroid.p2p.models.ImageFile
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.*
import java.math.BigInteger
import java.security.MessageDigest

/**
 * Clase encargada de la gestión de toda la lógica P2P.
 *
 * @param context Contexto de la actividad principal.
 * @param groupDao Interfaz con la definición de funciones para el acceso a datos de la tabla "group".
 * @param groupMemberDao Interfaz con la definición de funciones para el acceso a datos de la tabla "group_member".
 * @param imageDao Interfaz con la definición de funciones para el acceso a datos de la tabla "image".
 */
class P2PRepository(
    private val context: MainActivity,
    private val groupDao: GroupDao,
    private val groupMemberDao: GroupMemberDao,
    private val imageDao: ImageDao,
    private val videoDao: VideoDao
) {

    /**
     * Companion con constantes y métodos auxiliares.
     */
    companion object {
        /**
         * Nombre de la clase actual, para su uso en logs.
         */
        const val TAG = "P2PRepository"

        /**
         * Tamaño máximo de cada uno de los array de bytes en los que se divide una imagen.
         */
        const val MAX_PAYLOAD_SIZE = 100000

        /**
         * Devuelve un TAG formateado con fecha para logs.
         */
        fun getCurrentTimestamp(): String
        {
            return "$TAG at ${DateHelper.getCurrentDateVerbose()}"
        }
    }

    /**
     * Role que desempeña el dispositivo actual dentro del grupo.
     */
    private var nearbyRole = EnumHelper.NearbyRole.UNDEFINED.role

    /**
     * Estado en el que se encuentra la sesión actual.
     */
    private var sessionStatus = EnumHelper.SessionStatus.UNDEFINED.status

    /**
     * Livedata para comunicar el cambio del estado de sesión a la capa del ViewModel.
     */
    private var sessionStatusLivedata = MutableLiveData<Int>()

    /**
     * Estado de unión a un grupo.
     */
    private var groupJoinCode = MutableLiveData<Int>()

    /**
     * Nombre del dispositivo actual.
     */
    private var myNameIs = ""

    /**
     * Nombre del endpoint actual.
     */
    private var myEndpointName = ""

    /**
     * Handler para la ejecución de métodos de manera reiterada.
     */
    private var mainHandler = Handler(Looper.getMainLooper())

    /**
     * Instancia del grupo actual.
     */
    private lateinit var mGroup: Group

    /**
     * Livedata para la publicación de los grupos encontrados en la búsqueda a la capa del ViewModel.
     */
    private var groupsLivedata = MutableLiveData<ArrayList<GroupFound>>()

    /**
     * Array con los grupos encontrados en la búsqueda de grupos a los que unirse.
     */
    private var groupsFound = ArrayList<GroupFound>()

    /**
     * Array con todos los miembros que existen en el grupo actual.
     */
    private var groupMembers = ArrayList<GroupMember>()

    /**
     * Array que indica si cada uno de los miembros del grupo ha sido o no actualizado en la última oleada de actualización.
     */
    private var membersUpdated = ArrayList<Boolean>()

    /**
     * Array que indica si cada uno de los miembros tiene el listado de miembros actualizado.
     */
    private var updatingMembersChange = ArrayList<Boolean>()

    /**
     * Array que indica si cada uno de los miembros ha sido notificado de la finalización del grupo.
     */
    private var closingGroupMembersNotified = ArrayList<Boolean>()

    /**
     * Livedata para notificar la adición de una nueva imagen.
     */
    private var imageAdded = MutableLiveData<Int>()

    /**
     * Booleano que determina si el dispositivo actual está buscando dispositivos.
     */
    private var isDiscovering = false

    /**
     * Booleano que determina si el dispositivo actual se está anunciando.
     */
    private var isAdvertising = false

    /**
     * Booleano que determina si es necesario mandar el listado de miembros actualizados en la próxima petición de imágenes.
     */
    private var updateMembersInNextPicturesFetch = false

    /**
     * Nombre del endpoint del que será nuevo HOST del grupo.
     */
    private var newHostEndpointName = ""

    /**
     * Variable que contiene el nombre del endpoint en el que se ha realizado la última actualización.
     */
    private var lastUpdateWasSentToEndpointWithName = ""

    /**
     * Opciones de anuncio en Nearby.
     */
    private lateinit var advertisingOptions: AdvertisingOptions

    /**
     * Opciones de búsqueda en Nearby.
     */
    private lateinit var discoveryOptions: DiscoveryOptions

    /**
     * Ejecutable que inicia la actualización de imágenes.
     */
    private val requestPicturesUpdate = Runnable { updatePictures() }

    private var alreadyWentToMainActivity = false

    /**
     * Inicialización de valores.
     */
    init {
        myNameIs = SharedPreferencesHelper.getUsername(context)
        membersUpdated.add(true)
        updatingMembersChange.add(true)
    }

    /*******************************
     * FUNCIONES AUXILIARES VARIAS *
     *******************************/

    /**
     * Devuelve el nombre del dispositivo actual.
     */
    fun getMyName(): String{
        return myNameIs
    }

    /************************************************************
     * FUNCIONES PARA LA GESTIÓN DE ARRAYS DE MIEMBROS DE GRUPO *
     ************************************************************/

    /**
     * Devuelve el índice del array de miembros correspondiente al usuario actual.
     */
    private fun whichGroupMemberAmI(): Int
    {
        return groupMembers.indexOfFirst { member ->
            member.endpointName == myEndpointName
        }
    }

    /**
     * Devuelve las imágenes del usuario actual.
     */
    private fun imagesInGroup(): ArrayList<ImageFile>
    {
        return groupMembers[whichGroupMemberAmI()].images
    }

    /**
     * Devuelve el índice asociado al miembro con el "endpointName" indicado.
     *
     * @param name String sobre el que se quiere realizar la búsqueda de "endpointName".
     */
    private fun getMemberIndexFromEndpointName(name: String): Int
    {
        return groupMembers.indexOfFirst { member ->
            member.endpointName == name
        }
    }

    /**
     * Devuelve el índice asociado al miembro con el "endpointId" indicado.
     *
     * @param id String sobre el que se quiere realizar la búsqueda de "endpointId".
     */
    private fun getMemberIndexFromEndpointId(id: String): Int
    {
        return groupMembers.indexOfFirst { member ->
            member.endpointId == id
        }
    }

    /**
     * Devuelve el índice dentro del array de imágenes del usuario actual de la imagen con el hash especificado en parámetro.
     *
     * @param hash String sobre el que se quiere realizar la búsqueda de imágenes por hash.
     */
    private fun getIndexOfImage(hash: String): Int
    {
        return imagesInGroup().indexOfFirst { image ->
            image.image.hashCode == hash
        }
    }

    /**
     * Restablece el "endpointId" del miembro indicado por su "endpointId".
     *
     * Adicionalmente, establece a true la posición del array de actualización relativa al miembro del grupo, indicando que ya ha sido actualizado.
     *
     * @param endpointId String utilizado para encontrar al miembro del grupo.
     */
    private fun resetEndpointId(endpointId: String)
    {
        val index = groupMembers.indexOfFirst { member ->
            member.endpointId == endpointId
        }
        if (index != -1) {
            membersUpdated[index] = true
            groupMembers[index].endpointId = null
        }
    }

    /**
     * Devuelve el índice del host.
     */
    private fun getIndexOfHost(): Int
    {
        return groupMembers.indexOfFirst { el ->
            el.isHost
        }
    }

    /***********************************************************
     * FUNCIONES PARA LA GESTIÓN DE FOTOS DE MIEMBROS DE GRUPO *
     ***********************************************************/

    /**
     * Marca todas las fotos del miembro actual como no sincronizadas.
     */
    private fun markAllPicturesAsNotSynchronized()
    {
        groupMembers[whichGroupMemberAmI()].images.forEach { image ->
            image.image.isSynchronized = 0
            image.files.forEach { file ->
                file.isSynchronized = false
            }
        }
    }

    /**
     * Reconstruye una imagen a partir del array de arrays de bytes recibido.
     */
    private fun reconstructPhoto(arrays: ArrayList<ByteArray?>, image: com.example.tfgandroid.database.models.Image)
    {
        val directory = File(context.filesDir, "group_${mGroup.id!!}")
        if (!directory.exists()) {
            directory.mkdir()
        }
        var finalByteArray = arrays.first()!!
        var index = 1
        val limit = arrays.size
        while (index < limit) {
            finalByteArray = finalByteArray.plus(arrays[index]!!)
            index++
        }
        val bitmapImage = BitmapFactory.decodeByteArray(finalByteArray, 0, finalByteArray.size, null)
        val imageFile = File(directory, image.name)
        val os: OutputStream
        try {
            os = FileOutputStream(imageFile)
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, os)
            os.flush()
            os.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error")
        }
        CoroutineScope(Dispatchers.IO).launch {
            val nImage = imageDao.getImageByHash(image.hashCode)[0]
            imageDao.updateImagePath(imageFile.absolutePath, nImage.id!!)
            imagesInGroup().add(ImageFile(nImage, ArrayList()))
        }
    }

    /**
     * Rota la imagen los grados indicados.
     *
     * @param rotation Rotación que se quiere aplicar a la imagen.
     * @param srcBitmap Bitmap con la imagen que se desea rotar.
     *
     */
    private fun rotateImage(rotation: Int, srcBitmap: Bitmap): Bitmap
    {
        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())
        return Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.width, srcBitmap.height, matrix, true)
    }

    /**
     * Pasa la rotación obtenida en los sensores del dispositivo a un valor determinado.
     *
     * @param rotation Rotación del sensor X en grados.
     */
    private fun getRealRotation(rotation: Int): Int
    {
        return when (rotation) {
            in -45..45 -> 0
            in 45..135 -> 90
            in 135..180, in -180..-135 -> 180
            in -135..-45 -> 270
            else -> 0
        }
    }

    /**
     * Guarda la imagen.
     *
     * @param image Imagen tomada mediante la cámara.
     * @param rotation Rotación del sensor en el momento de capturar la imagen.
     */
    fun saveImage(image: Image, rotation: Int)
    {
        val directory = mGroup.getDirectory(context)
        if (!directory.exists()) {
            directory.mkdir()
        }
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        val md = MessageDigest.getInstance("MD5")
        val hashCode = BigInteger(md.digest(bytes)).toString(16).padStart(32, '0')
        val bitmapImageAux = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
        var realRotation = getRealRotation(rotation)
        if (realRotation == 0 || realRotation == 180) {
            realRotation += 90
        } else {
            realRotation -= 90
        }
        val bitmapImage = rotateImage(realRotation, bitmapImageAux)
        val imageName = "${formatImageName()}.jpg"
        val imageFile = File(directory, imageName)
        var os: OutputStream
        try {
            os = FileOutputStream(imageFile)
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, os)
            os.flush()
            os.close()
        } catch (e: Exception) {

        }

        val lowQualityImage = saveBitmapToFile(imageFile)


        lowQualityImage?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val nImage = com.example.tfgandroid.database.models.Image(imageName, imageFile.absolutePath, mGroup.id!!, myNameIs, hashCode)
                val imageByteArray = parseImageToBytes(nImage)

                nImage.id = imageDao.insertImage(nImage)
                imagesInGroup().add(ImageFile(nImage, imageByteArray))
            }
            imageAdded.postValue(imagesInGroup().size)
        }
    }

    /**
     * Guarda el vídeo del grupo actual.
     */
    fun saveVideo()
    {
        CoroutineScope(Dispatchers.IO).launch {
            videoDao.insertVideo(Video(mGroup.id!!))
        }
    }

    /**
     * Devuelve el nombre que se le va a dar a la nueva imagen.
     */
    private fun formatImageName(): String
    {
        var name = imagesInGroup().size.toString()
        while (name.length < 4) {
            name = "0$name"
        }
        return "${myEndpointName}_$name"
    }

    /**
     * Convierte la imagen a un array en el que cada posición es un array de bytes, cada uno de ellos con una longitud máxima de MAX_PAYLOAD_SIZE bytes.
     *
     * @param image Imagen que se quiere convertir.
     */
    private fun parseImageToBytes(image: com.example.tfgandroid.database.models.Image): ArrayList<Chunk>
    {
        val finalParsedFile = ArrayList<Chunk>()
        val imageFile = File(image.path)
        val auxParsedFile = ByteArray(imageFile.length().toInt())
        try {
            val buffer = BufferedInputStream(FileInputStream(imageFile))
            buffer.read(auxParsedFile, 0, auxParsedFile.size)
            buffer.close()
            if (auxParsedFile.size < MAX_PAYLOAD_SIZE) {
                finalParsedFile.add(Chunk(false, auxParsedFile.copyOfRange(0, auxParsedFile.size)))
            } else {
                var cont = 0
                var nextCont = MAX_PAYLOAD_SIZE
                while (auxParsedFile.size > nextCont) {
                    finalParsedFile.add(Chunk(false, auxParsedFile.copyOfRange(cont, nextCont)))
                    cont = nextCont
                    nextCont += MAX_PAYLOAD_SIZE
                }
                finalParsedFile.add(Chunk(false, auxParsedFile.copyOfRange(cont, auxParsedFile.size)))
            }
        } catch (e: Exception) {

        }
        return finalParsedFile
    }

    /**
     * Convierte un bitmap en un fichero.
     */
    private fun saveBitmapToFile(file: File): File?
    {
        return try {
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            o.inSampleSize = 6
            var inputStream = FileInputStream(file)
            BitmapFactory.decodeStream(inputStream, null, o)
            inputStream.close()

            val REQUIRED_SIZE = 75

            var scale = 1
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                o.outHeight / scale / 2 >= REQUIRED_SIZE
            ) {
                scale *= 2
            }
            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            inputStream = FileInputStream(file)
            val selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2)
            Log.d(TAG, "El ancho del fichero es ${selectedBitmap!!.width}")
            Log.d(TAG, "El alto del fichero es ${selectedBitmap.height}")
            inputStream.close()

            file.createNewFile()
            val outputStream = FileOutputStream(file)
            selectedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            file
        } catch (e: java.lang.Exception) {
            null
        }
    }

    /**************************************************
     * FUNCIONES AUXILIARES PARA LA GESTIÓN DE NEARBY *
     **************************************************/

    /**
     * Codifica y manda un mensaje al endpoint indicado.
     */
    private fun sendMessageToEndpoint(message: P2PMessage, endpointId: String)
    {
        val encondedMessage = Json.encodeToString(message).toByteArray()
        val bytesPayload = Payload.fromBytes(encondedMessage)
        Log.d(TAG, "Enviando mensaje desde $myNameIs a las ${DateHelper.getCurrentDateVerbose()}")
        getNearbyClient().sendPayload(endpointId, bytesPayload)
    }

    /**
     * Devuelve la instancia del cliente de Nearby.
     */
    private fun getNearbyClient(): ConnectionsClient {
        return Nearby.getConnectionsClient(context)
    }

    /**
     * Extrae la información sobre el grupo encontrado a partir del string proporcionado.
     *
     * @param text Texto sobre el que se quiere realizar el parse.
     * @param endpointId Id del endpoint que ha mandado la cadena de texto.
     */
    private fun extractGroupDataFromString(text: String, endpointId: String)
    {
        val groupName = text.substringAfter("_N_").substringBefore("_EN_")
        val owner = text.substringAfter("_O_").substringBefore("_EO_")
        val size = text.substringAfter("_SI_").substringBefore("_ESI_")
        val prefix = text.substringAfter("_P_").substringBefore("_EP")
        val state = text.substringAfter("_ST_").substringBefore("_EST")
        val existingIndex = groupsFound.map { groupFound -> groupFound.groupPrefix }.indexOfFirst { el -> el == prefix }
        if (existingIndex != -1) {
            groupsFound.removeAt(existingIndex)
            groupsLivedata.value = groupsFound
        }
        groupsFound.add(GroupFound(groupName, owner, size, endpointId, prefix, state.toInt()))
        groupsLivedata.value = groupsFound
    }

    /**
     * Realiza la desconexión del endpoint indicado.
     *
     * @param endpointId Id del endpoint del que se quiere desconectar.
     */
    private fun disconnectFromEndpoint(endpointId: String)
    {
        getNearbyClient().disconnectFromEndpoint(endpointId)
        if (sessionStatus != EnumHelper.SessionStatus.STARTED.status) {
            getNearbyClient().stopDiscovery()
            isDiscovering = false
            groupMemberAdvertisingForHost()
        }
    }

    /**
     * Establece el nombre usado para el anuncio del dispositivo actual.
     *
     * @param group Instancia del grupo que se quiere anunciar.
     */
    private fun setHostAdvertisingName(group: Group): String
    {
        val size = if (groupMembers.size == 0) {
            1
        } else {
            groupMembers.size
        }
        return "${context.packageName}_N_${group.name}_EN_O_${myNameIs}_EO_SI_$size/32_ESI_P_${group.groupCommonPrefix}_EP_ST_${sessionStatus}_EST"
    }

    /**
     * Cierra el grupo actual.
     */
    fun deleteGroup()
    {
        Nearby.getConnectionsClient(context).stopAdvertising()
        CoroutineScope(Dispatchers.IO).launch {
            groupDao.closeGroup(mGroup.id!!, DateHelper.getCurrentDateString())
        }
    }

    /**
     * Inicia el proceso de actualización de imágenes.
     */
    private fun updatePictures()
    {
        notifyAllMembers()
    }

    /**
     * Inicia el proceso para abandonar un grupo.
     */
    fun sendIntentionToLeave()
    {
        if (nearbyRole == EnumHelper.NearbyRole.GUEST.role) {
            sessionStatus = EnumHelper.SessionStatus.LEAVING.status
            getNearbyClient().stopAdvertising()
            searchGroup()
        } else if (nearbyRole == EnumHelper.NearbyRole.HOST.role && groupMembers.size > 1) {
            decideNewHost()
            sessionStatus = EnumHelper.SessionStatus.LEAVING.status
            getNearbyClient().stopAllEndpoints()
            updatePictures()
        }
    }

    /**
     * Determina que endpoint desempeñará la función de nuevo HOST.
     */
    private fun decideNewHost()
    {
        val membersSortedByImages = groupMembers.filter { excludeSelf -> excludeSelf.endpointName != myEndpointName }.sortedByDescending { el -> el.images.size }
        newHostEndpointName = membersSortedByImages[0].endpointName
    }

    /**
     * Cambia el estado de la sesión actual.
     */
    private fun changeSessionStatus(status: Int)
    {
        sessionStatus = status
        sessionStatusLivedata.value = sessionStatus
    }

    /*********************************************************
     * FUNCIONES PARA INICIAR PROCESOS DE BÚSQUEDA Y ANUNCIO *
     *********************************************************/

    /**
     * Comienza el anuncio de un grupo tras su creación.
     *
     * @param group Instancia del grupo creado.
     */
    private fun startAdvertisingOnGroupCreation(group: Group)
    {
        val advertisingName = setHostAdvertisingName(group)
        getNearbyClient()
            .startAdvertising(advertisingName, context.packageName, connectionLifecycleCallback, advertisingOptions)
            .addOnSuccessListener {
                isAdvertising = true
                nearbyRole = EnumHelper.NearbyRole.HOST.role
            }
            .addOnFailureListener {

            }
    }

    /**
     * Procesa la creación de un nuevo grupo.
     *
     * @param group Instancia del grupo creado.
     */
    fun createGroup(group: Group)
    {
        myNameIs = SharedPreferencesHelper.getUsername(context)
        CoroutineScope(Dispatchers.IO).launch {
            mGroup = group
            mGroup.id = groupDao.insertGroup(group)
            val endpointName = "${mGroup.groupCommonPrefix}_${GroupHelper.generateRandomPrefix(8)}"
            myEndpointName = endpointName
            val newMember = GroupMember(myNameIs, endpointName, true, mGroup.id!!, null, DateHelper.getCurrentDateString())
            newMember.id = groupMemberDao.insertGroupMember(newMember)
            groupMembers.add(newMember)
        }
        advertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        startAdvertisingOnGroupCreation(group)
        updatePictures()
    }

    /**
     * Comienza el anuncio de un miembro del grupo tras finalizar su proceso de unión al grupo.
     */
    private fun groupMemberAdvertisingForHost()
    {
        advertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        getNearbyClient()
            .startAdvertising(myEndpointName, context.packageName, connectionLifecycleCallbackAfterGroupSetup, advertisingOptions)
            .addOnSuccessListener {
                isAdvertising = true
            }
            .addOnFailureListener {
            }
    }

    /**
     * Comienza la búsqueda de grupos.
     */
    fun searchGroup()
    {
        myNameIs = SharedPreferencesHelper.getUsername(context)
        if (isDiscovering) {
            isDiscovering = false
            getNearbyClient().stopDiscovery()
        }
        discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        getNearbyClient()
            .startDiscovery(context.packageName, endpointDiscoveryCallback, discoveryOptions)
            .addOnSuccessListener {
                if (sessionStatus != EnumHelper.SessionStatus.LEAVING.status) {
                    nearbyRole = EnumHelper.NearbyRole.GUEST.role
                    isDiscovering = true
                }
            }
            .addOnFailureListener {

            }
    }

    /**
     * Para la búsqueda de grupos.
     */
    fun stopSearchingGroups()
    {
        getNearbyClient().stopDiscovery()
    }

    /**
     * Hace una petición de conexión al grupo indicado.
     *
     * @param endpointId Id del endpoint con el que se quiere realizar la conexión.
     */
    fun joinGroup(endpointId: String)
    {
        getNearbyClient().requestConnection(myNameIs, endpointId, connectionLifecycleCallback)
            .addOnSuccessListener {

            }
            .addOnFailureListener {

            }
    }

    /**
     * Inicia la sesión para el grupo actual (solo puede realizarse siendo host).
     */
    fun startSession()
    {
        sessionStatus = EnumHelper.SessionStatus.STARTING.status
        getNearbyClient().stopAllEndpoints()
        startAdvertisingOnGroupCreation(mGroup)
        membersUpdated.fill(false)
        membersUpdated[whichGroupMemberAmI()] = true
        if (membersUpdated.size > 1) {
            notifyAllMembers()
        } else {
            changeSessionStatus(EnumHelper.SessionStatus.STARTED.status)
        }
    }

    /**
     * Inicial el proceso de notificación al resto de integrantes del grupo (por parte del HOST).
     */
    private fun notifyAllMembers()
    {
        discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        if (isDiscovering) {
            getNearbyClient().stopDiscovery()
            isDiscovering = false
        }
        getNearbyClient()
            .startDiscovery(context.packageName, endpointDiscoveryCallback, discoveryOptions)
            .addOnSuccessListener {

                isDiscovering = true
            }
            .addOnFailureListener {

            }
    }

    fun closeGroup()
    {
        getNearbyClient().stopDiscovery()
        getNearbyClient().stopAllEndpoints()
        Log.d(TAG, "GroupMembers tiene un tamaño de ${groupMembers.size}")
        for (i in 0 until groupMembers.size) {
            closingGroupMembersNotified.add(false)
        }
        closingGroupMembersNotified[whichGroupMemberAmI()] = true
        sessionStatus = EnumHelper.SessionStatus.FINISHED.status
        notifyAllMembers()
    }

    private fun resetAfterFinishedGroup()
    {
        getNearbyClient().stopDiscovery()
        getNearbyClient().stopAdvertising()
        getNearbyClient().stopAllEndpoints()
        nearbyRole = EnumHelper.NearbyRole.UNDEFINED.role
        myEndpointName = ""
        groupsFound.clear()
        groupMembers.clear()
        membersUpdated.clear()
        updatingMembersChange.clear()
        isDiscovering = false
        isAdvertising = false
        updateMembersInNextPicturesFetch = false
        newHostEndpointName = ""
        mainHandler.removeCallbacks(requestPicturesUpdate)
        changeSessionStatus(EnumHelper.SessionStatus.FINISHED.status)
    }

    /*************
     * CALLBACKS *
     *************/

    /**
     * Define el comportamiento al recibir un mensaje desde otro endpoint.
     */
    private val mPayloadCallback: PayloadCallback = object : PayloadCallback() {

        /**
         * Se ejecuta cada vez que se recibe un nuevo mensaje desde otro endpoint.
         *
         * @param endpointId Id del endpoint que ha enviado el mensaje.
         * @param payload Contenido del mensaje enviado.
         */
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            processMessageReceived(endpointId, payload)
        }

        /**
         * EN DESUSO
         */
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {

        }
    }

    /**
     * Define el comportamiento al encontrar un endpoint en la búsqueda de peers.
     */
    private val endpointDiscoveryCallback: EndpointDiscoveryCallback = object : EndpointDiscoveryCallback()
    {
        /**
         * Función que se ejecuta cada vez que se encuentra un endpoint que se está anunciando.
         */
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo)
        {
            if (nearbyRole == EnumHelper.NearbyRole.GUEST.role) {
                if (sessionStatus == EnumHelper.SessionStatus.UNDEFINED.status) {
                    if (info.endpointName.contains(context.packageName)) {
                        extractGroupDataFromString(info.endpointName, endpointId)
                    }
                } else {
                    if (info.endpointName.contains(mGroup.groupCommonPrefix)) {
                        getNearbyClient().requestConnection(myEndpointName, endpointId, connectionLifecycleCallback)
                    }
                }
            } else if (nearbyRole == EnumHelper.NearbyRole.HOST.role) {
                val memberIndex = getMemberIndexFromEndpointName(info.endpointName)
                if (memberIndex != -1 && sessionStatus != EnumHelper.SessionStatus.UNDEFINED.status) {
                    val disconnectTime = groupMembers[memberIndex].disconnectedAt
                    disconnectTime?.let {
                        if (DateHelper.hasTimePassed(disconnectTime, 10) || sessionStatus != EnumHelper.SessionStatus.STARTED.status || sessionStatus != EnumHelper.SessionStatus.FINISHED.status) {
                            getNearbyClient().requestConnection(myEndpointName, endpointId, connectionLifecycleCallbackAfterGroupSetup)
                        } else {
                            getNearbyClient().stopDiscovery()
                            mainHandler.postDelayed(requestPicturesUpdate, 5000)
                        }

                    } ?: run {
                        getNearbyClient().requestConnection(myEndpointName, endpointId, connectionLifecycleCallbackAfterGroupSetup)
                    }
                } else if (memberIndex != -1 && !updatingMembersChange[memberIndex]) {
                    getNearbyClient().requestConnection(myEndpointName, endpointId, connectionLifecycleCallbackAfterGroupSetup)
                } else {
                    getNearbyClient().stopDiscovery()
                    mainHandler.postDelayed(requestPicturesUpdate, 5000)
                }
            } else {
                if (nearbyRole != EnumHelper.NearbyRole.HOST.role) {

                }
                if (getMemberIndexFromEndpointName(info.endpointName) != -1) {

                }
                if (membersUpdated[getMemberIndexFromEndpointName(info.endpointName)]) {

                }
            }
        }

        override fun onEndpointLost(endpointId: String) {

        }
    }

    /**
     * Define el comportamiento al establecer una conexión entre dos dispositivos.
     */
    private val connectionLifecycleCallback: ConnectionLifecycleCallback = object : ConnectionLifecycleCallback()
    {
        /**
         * Función que se ejecuta al iniciar la conexión entre dos miembros.
         */
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo)
        {
            val index = getMemberIndexFromEndpointName(connectionInfo.endpointName)
            if (index != -1) {
                groupMembers[index].endpointId = endpointId
            }
            getNearbyClient().acceptConnection(endpointId, mPayloadCallback)
        }

        /**
         * Función que se ejecuta una vez ha finalizado el proceso de establecimiento de conexión.
         *
         * @param endpointId Id del endpoint con el que se hecho el proceso de establecimiento de conexión.
         * @param result Objeto que contiene los resultados del proceso de establecimiento de conexión.
         */
        override fun onConnectionResult(endpointId: String, result: ConnectionResolution)
        {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    if (nearbyRole == EnumHelper.NearbyRole.GUEST.role) {
                        if (sessionStatus == EnumHelper.SessionStatus.UNDEFINED.status) {
                            sendMessageToEndpoint(P2PMessage.guestInfoMessage(myNameIs), endpointId)
                        } else {
                            sendMessageToEndpoint(P2PMessage.sendIntentionToLeaveMessage(), endpointId)
                        }
                    }
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> { }
                ConnectionsStatusCodes.STATUS_ERROR -> { }
                else -> {}
            }
        }

        /**
         * Función que se ejecuta cuando se produce la desconexión entre dos dispositivos.
         *
         * @param endpointId Id del endpoint del que se desconecta el dispositivo actual.
         */
        override fun onDisconnected(endpointId: String) {
            if (sessionStatus == EnumHelper.SessionStatus.LEAVING.status) {
                getNearbyClient().stopAdvertising()
                getNearbyClient().stopAllEndpoints()
                changeSessionStatus(EnumHelper.SessionStatus.LEFT.status)
            } else if (sessionStatus != EnumHelper.SessionStatus.STARTED.status) {
                getNearbyClient().stopDiscovery()
                isDiscovering = false
                groupMemberAdvertisingForHost()
            }
        }
    }

    /**
     * Define el comportamiento al establecer una conexión entre dos dispositivos (usado para conexiones posteriores al establecimiento del grupo).
     */
    private val connectionLifecycleCallbackAfterGroupSetup: ConnectionLifecycleCallback = object : ConnectionLifecycleCallback()
    {
        /**
         * Función que se ejecuta cuando se inicia la conexión entre dos dispositivos.
         *
         * @param endpointId Id del endpoint con el que se inicia la conexión
         * @param connectionInfo Datos de conexión del dispositivo con el que se realiza la conexión.
         */
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo)
        {
            val indexOfGroupMember = getMemberIndexFromEndpointName(connectionInfo.endpointName)
            groupMembers[indexOfGroupMember].endpointId = endpointId
            groupMembers[indexOfGroupMember].connectedAt = DateHelper.getCurrentDateString()
            getNearbyClient().acceptConnection(endpointId, mPayloadCallback)
        }

        /**
         * Función que se ejecuta una vez ha finalizado el proceso de establecimiento de conexión.
         *
         * @param endpointId Id del endpoint con el que se ha llevado a cabo el proceso.
         * @param result Contiene el resultado del proceso.
         */
        override fun onConnectionResult(endpointId: String, result: ConnectionResolution)
        {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    if (nearbyRole == EnumHelper.NearbyRole.HOST.role) {
                        if (!updatingMembersChange[getMemberIndexFromEndpointId(endpointId)]) {
                            sendMessageToEndpoint(P2PMessage.sendGroupMembersUpdateMessage(groupMembers), endpointId)
                        }
                        when (sessionStatus) {
                            EnumHelper.SessionStatus.STARTING.status -> {
                                sendMessageToEndpoint(P2PMessage.notifySessionStatusChanged(EnumHelper.SessionStatus.STARTED.status), endpointId)
                            }
                            EnumHelper.SessionStatus.STARTED.status -> {
                                if (updateMembersInNextPicturesFetch) {
                                    sendMessageToEndpoint(P2PMessage.sendGroupMembersUpdateMessage(groupMembers), endpointId)
                                }
                                sendMessageToEndpoint(P2PMessage.requestPhotosMessage(), endpointId)
                            }
                            EnumHelper.SessionStatus.LEAVING.status -> {
                                if (groupMembers[getMemberIndexFromEndpointId(endpointId)].endpointName == newHostEndpointName) {
                                    sendMessageToEndpoint(P2PMessage.sendChooseNewHostMessage(), endpointId)
                                }
                            }
                            EnumHelper.SessionStatus.FINISHED.status -> {
                                if (!closingGroupMembersNotified[getMemberIndexFromEndpointId(endpointId)]) {
                                    closingGroupMembersNotified[getMemberIndexFromEndpointId(endpointId)] = true
                                    sendMessageToEndpoint(P2PMessage.sendCloseGroupMessage(), endpointId)
                                }
                            }
                        }
                    }
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> { }
                ConnectionsStatusCodes.STATUS_ERROR -> { }
                else -> {}
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.d(TAG, "DISCONNECTED")
            if (sessionStatus == EnumHelper.SessionStatus.FINISHED.status) {
                if (nearbyRole == EnumHelper.NearbyRole.HOST.role) {
                    var membersUpdated = 0;
                    Log.d(TAG, "El tamaño de closingGroupMembersNotified es ${closingGroupMembersNotified.size}")
                    closingGroupMembersNotified.forEach { item ->
                        if (item) {
                            Log.d(TAG, "Item actualizado")
                            membersUpdated++
                        }
                    }
                    Log.d(TAG, "Han sido actualizados $membersUpdated de ${closingGroupMembersNotified.size}")
                    if (membersUpdated == closingGroupMembersNotified.size) {
                        resetAfterFinishedGroup()
                    }
                } else {
                    resetAfterFinishedGroup()
                }
            }
        }
    }

    /*********************************************************
     * FUNCIONES PARA EL PROCESAMIENTO DE MENSAJES RECIBIDOS *
     *********************************************************/

    private fun parseMessageTypeToStringForLogs(type: Int): String
    {
        return when (type) {
            EnumHelper.P2PMessageType.SEND_GUEST_INFO.type -> {
                "SEND_GUEST_INFO"
            }
            EnumHelper.P2PMessageType.GROUP_JOINED_CONFIRMATION.type -> {
                "GROUP_JOINED_CONFIRMATION"
            }
            EnumHelper.P2PMessageType.SESSION_STATUS_CHANGED.type -> {
                "SESSION_STATUS_CHANGED"
            }
            EnumHelper.P2PMessageType.DISCONNECT.type -> {
                "DISCONNECT"
            }
            EnumHelper.P2PMessageType.REQUEST_NEW_PHOTOS.type -> {
                "REQUEST_NEW_PHOTOS"
            }
            EnumHelper.P2PMessageType.SEND_NEW_PHOTOS.type -> {
                "SEND_NEW_PHOTOS"
            }
            EnumHelper.P2PMessageType.CHUNK_RECEIVED_SUCCESSFULLY.type -> {
                "CHUNK_RECEIVED_SUCCESSFULLY"
            }
            EnumHelper.P2PMessageType.NOTIFY_INTENTION_TO_LEAVE.type -> {
                "NOTIFY_INTENTION_TO_LEAVE"
            }
            EnumHelper.P2PMessageType.UPDATE_GROUP_MEMBERS.type -> {
                "UPDATE_GROUP_MEMBERS"
            }
            EnumHelper.P2PMessageType.UPDATE_GROUP_MEMBERS_RECEIVED.type -> {
                "UPDATE_GROUP_MEMBERS_RECEIVED"
            }
            EnumHelper.P2PMessageType.CHOOSE_NEW_HOST.type -> {
                "CHOOSE_NEW_HOST"
            }
            EnumHelper.P2PMessageType.CHOOSE_NEW_HOST_RECEIVED.type -> {
                "CHOOSE_NEW_HOST_RECEIVED"
            }
            else -> {
                "DEFAULT"
            }
        }
    }

    /**
     * Determina el tipo de mensaje recibido y lo procesa de manera acorde al tipo de mensaje que se trata.
     *
     * @param endpointId Id del endpoint que ha enviado el mensaje.
     * @param payload Contenido del mensaje enviado.
     */
    private fun processMessageReceived(endpointId: String, payload: Payload)
    {
        val messageReceived = Json.decodeFromString<P2PMessage>(String(payload.asBytes()!!))
        Log.d(TAG, "Recibido mensaje de tipo ${parseMessageTypeToStringForLogs(messageReceived.messageType)} en $myNameIs de parte de ${if (getMemberIndexFromEndpointId(endpointId) != -1) groupMembers[getMemberIndexFromEndpointId(endpointId)].name else "UNKNOWN"}" +
                "a las ${DateHelper.getCurrentDateVerbose()}")
        when (messageReceived.messageType) {
            EnumHelper.P2PMessageType.SEND_GUEST_INFO.type -> {
                processGuestInfoMessage(messageReceived, endpointId)
            }
            EnumHelper.P2PMessageType.GROUP_JOINED_CONFIRMATION.type -> {
                processGroupJoinedConfirmationMessage(messageReceived, endpointId)
            }
            EnumHelper.P2PMessageType.SESSION_STATUS_CHANGED.type -> {
                processSessionStatusChanged(messageReceived, endpointId)
            }
            EnumHelper.P2PMessageType.DISCONNECT.type -> {
                processDisconnectMessage(endpointId)
            }
            EnumHelper.P2PMessageType.REQUEST_NEW_PHOTOS.type -> {
                processRequestNewPhotosMessage(endpointId)
            }
            EnumHelper.P2PMessageType.SEND_NEW_PHOTOS.type -> {
                processSendNewPhotosMessage(messageReceived, endpointId)
            }
            EnumHelper.P2PMessageType.CHUNK_RECEIVED_SUCCESSFULLY.type -> {
                processChunkReceivedSuccessfullyMessage(messageReceived, endpointId)
            }
            EnumHelper.P2PMessageType.NOTIFY_INTENTION_TO_LEAVE.type -> {
                processIntentionToLeaveMessage(endpointId)
            }
            EnumHelper.P2PMessageType.UPDATE_GROUP_MEMBERS.type -> {
                processGroupMembersUpdateMessage(messageReceived, endpointId)
            }
            EnumHelper.P2PMessageType.UPDATE_GROUP_MEMBERS_RECEIVED.type -> {
                processGroupMembersUpdateReceivedMessage(endpointId)
            }
            EnumHelper.P2PMessageType.CHOOSE_NEW_HOST.type -> {
                processChooseNewHostMessage(endpointId)
            }
            EnumHelper.P2PMessageType.CHOOSE_NEW_HOST_RECEIVED.type -> {
                processChooseNewHostReceivedMessage()
            }
            EnumHelper.P2PMessageType.CLOSE_GROUP.type -> {
                processCloseGroupReceivedMessage(endpointId)
            }
            else -> {

            }
        }
    }

    /**
     * Procesa la recepción de un mensaje de tipo SEND_GUEST_INFO.
     */
    private fun processGuestInfoMessage(messageReceived: P2PMessage, endpointId: String)
    {
        if (nearbyRole == EnumHelper.NearbyRole.HOST.role) {
            CoroutineScope(Dispatchers.IO).launch {
                val endpointName = "${mGroup.groupCommonPrefix}_${GroupHelper.generateRandomPrefix(8)}"
                val newMember = GroupMember(messageReceived.guestName!!, endpointName, false, mGroup.id!!, endpointId, DateHelper.getCurrentDateString())
                newMember.id = groupMemberDao.insertGroupMember(newMember)
                groupMembers.add(newMember)
                membersUpdated.add(false)
                updatingMembersChange.fill(false)
                updatingMembersChange[whichGroupMemberAmI()] = true
                updatingMembersChange.add(true)
                sendMessageToEndpoint(P2PMessage.groupJoinedConfirmationMessage(mGroup, groupMembers, endpointName), endpointId)
                getNearbyClient().stopAdvertising()
                startAdvertisingOnGroupCreation(mGroup)
            }
        }
    }

    /**
     * Procesa la recepción de un mensaje de tipo GROUP_JOINED_CONFIRMATION.
     */
    private fun processGroupJoinedConfirmationMessage(messageReceived: P2PMessage, endpointId: String)
    {
        if (nearbyRole == EnumHelper.NearbyRole.GUEST.role) {
            CoroutineScope(Dispatchers.IO).launch {
                val newGroup = Group(
                    messageReceived.fullGroupInfo!!.name,
                    messageReceived.fullGroupInfo.createdAt,
                    messageReceived.fullGroupInfo.groupCommonPrefix
                )
                myEndpointName = messageReceived.memberEndpointName!!
                newGroup.id = groupDao.insertGroup(newGroup)
                mGroup = newGroup
                messageReceived.fullMemberList?.forEach { member ->
                    val newMember = GroupMember(member.name, member.endpointName, member.isHost, mGroup.id!!, null, null)
                    groupMemberDao.insertGroupMember(newMember)
                    groupMembers.add(newMember)
                    membersUpdated.add(true)
                    updatingMembersChange.add(true)
                }
                groupJoinCode.postValue(EnumHelper.JoinGroupCode.SUCCESS.code)
                sendMessageToEndpoint(P2PMessage.notifyDisconnectMessage(), endpointId)
            }
        }
    }

    /**
     * Procesa la recepción de un mensaje de tipo SESSION_STATUS_CHANGED.
     */
    private fun processSessionStatusChanged(messageReceived: P2PMessage, endpointId: String)
    {
        if (nearbyRole == EnumHelper.NearbyRole.GUEST.role) {
            changeSessionStatus(messageReceived.sessionStatus!!)
            sendMessageToEndpoint(P2PMessage.notifyDisconnectMessage(), endpointId)
        }
    }

    /**
     * Procesa la recepción de un mensaje de tipo DISCONNECT.
     */
    private fun processDisconnectMessage(endpointId: String)
    {
        if (nearbyRole == EnumHelper.NearbyRole.HOST.role) {
            getNearbyClient().disconnectFromEndpoint(endpointId)
            val index = getMemberIndexFromEndpointId(endpointId)
            if (index != -1) {
                groupMembers[index].disconnectedAt = DateHelper.getCurrentDateVerbose()
            }
            resetEndpointId(endpointId)
            if (sessionStatus == EnumHelper.SessionStatus.STARTING.status) {
                if (membersUpdated.all { element -> element }) {
                    getNearbyClient().stopDiscovery()
                    isDiscovering = false
                    changeSessionStatus(EnumHelper.SessionStatus.STARTED.status)
                    mainHandler.postDelayed(requestPicturesUpdate, 10000)
                }
            } else if (sessionStatus == EnumHelper.SessionStatus.STARTED.status) {
                getNearbyClient().stopDiscovery()
                isDiscovering = false
                updatePictures()
            }
        }
    }

    /**
     * Procesa la recepción de un mensaje de tipo REQUEST_NEW_PHOTOS.
     */
    private fun processRequestNewPhotosMessage(endpointId: String)
    {
        if (nearbyRole == EnumHelper.NearbyRole.GUEST.role) {

            if (lastUpdateWasSentToEndpointWithName.isEmpty() || lastUpdateWasSentToEndpointWithName != groupMembers[getMemberIndexFromEndpointId(endpointId)].endpointName) {
                markAllPicturesAsNotSynchronized()
            }
            lastUpdateWasSentToEndpointWithName = groupMembers[getMemberIndexFromEndpointId(endpointId)].endpointName
            val onlyNonSynchronizedImages = imagesInGroup().filter { image -> image.image.isSynchronized == 0 }
            if (onlyNonSynchronizedImages.isNotEmpty()) {
                onlyNonSynchronizedImages.forEach { image ->
                    image.files.forEachIndexed { chunkIndex, chunk ->
                        Log.d(TAG, "Se va a mandar el chunk ${chunkIndex + 1} de ${image.files.size}")
                        sendMessageToEndpoint(P2PMessage.sendPhotosMessage(image.image, chunk.bytes!!, chunkIndex, image.files.size), endpointId)
                    }
                }
            } else {
                sendMessageToEndpoint(P2PMessage.notifyDisconnectMessage(), endpointId)
            }
        }
    }

    /**
     * Procesa la recepción de un mensaje de tipo SEND_NEW_PHOTOS.
     */
    private fun processSendNewPhotosMessage(message: P2PMessage, endpointId: String)
    {
        if (nearbyRole == EnumHelper.NearbyRole.HOST.role) {
            CoroutineScope(Dispatchers.IO).launch {
                if (imageDao.getImageByHash(message.image!!.hashCode).isEmpty()) {
                    val nImage = com.example.tfgandroid.database.models.Image(message.image.name, null, mGroup.id!!, message.image.owner, message.image.hashCode)
                    imageDao.insertImage(nImage)
                }
            }
            val filesFromMember = groupMembers[getMemberIndexFromEndpointId(endpointId)].images
            var fileIndex = filesFromMember.indexOfFirst { el -> el.image.hashCode == message.image!!.hashCode }
            if (fileIndex == -1) {
                filesFromMember.add(ImageFile(message.image!!, ArrayList()))
                fileIndex = filesFromMember.size - 1
            }
            if (filesFromMember[fileIndex].files.size == 0) {
                for (i in 0 until message.totalChunks!!) {
                    filesFromMember[fileIndex].files.add(Chunk(true, null))
                }
            }
            filesFromMember[fileIndex].files[message.chunkIndex!!].bytes = message.file!!
            Log.d(TAG, "Recibido el chunk ${message.chunkIndex!! + 1} de ${message.totalChunks!!}")
            if (filesFromMember[fileIndex].files.filter { el -> el.bytes != null }.size == message.totalChunks!!) {
                reconstructPhoto(ArrayList(filesFromMember[fileIndex].files.map { chunk -> chunk.bytes }), filesFromMember[fileIndex].image)
            }
            sendMessageToEndpoint(P2PMessage.sendChunkReceivedSuccessfullyMessage(message.image!!, message.chunkIndex), endpointId)
        }
    }

    /**
     * Procesa la recepción de un mensaje de tipo CHUNK_RECEIVED_SUCCESSFULLY.
     */
    private fun processChunkReceivedSuccessfullyMessage(messageReceived: P2PMessage, endpointId: String)
    {
        if (nearbyRole == EnumHelper.NearbyRole.GUEST.role) {
            val imageIndex = getIndexOfImage(messageReceived.image!!.hashCode)
            imagesInGroup()[imageIndex].files[messageReceived.chunkIndex!!].isSynchronized = true
            val totalChunks = imagesInGroup()[imageIndex].files.size
            val synchronizedChunks = imagesInGroup()[imageIndex].files.filter { chunk -> chunk.isSynchronized }.size
            if (totalChunks == synchronizedChunks) {
                imagesInGroup()[imageIndex].image.isSynchronized = 1
                sendMessageToEndpoint(P2PMessage.notifyDisconnectMessage(), endpointId)
            }
        }
    }

    /**
     * Procesa la recepción de un mensaje de tipo NOTIFY_INTENTION_TO_LEAVE.
     */
    private fun processIntentionToLeaveMessage(endpointId: String)
    {
        val index = getMemberIndexFromEndpointId(endpointId)
        if (index != -1) {
            groupMembers.removeAt(index)
            membersUpdated.removeAt(index)
            updatingMembersChange.removeAt(index)
            for (i in 0 until groupMembers.size) {
                membersUpdated[i] = false
                updatingMembersChange[i] = false
            }
            updateMembersInNextPicturesFetch = true
            getNearbyClient().disconnectFromEndpoint(endpointId)
        }
    }

    /**
     * Procesa la recepción de un mensaje de tipo UPDATE_GROUP_MEMBERS.
     */
    private fun processGroupMembersUpdateMessage(message: P2PMessage, endpointId: String)
    {
        if (groupMembers.size > message.fullMemberList!!.size) {
            val remoteEndpointNames = message.fullMemberList.map { element -> element.endpointName }
            var memberToDeleteFound = false
            var counter = 0
            while (!memberToDeleteFound && counter < groupMembers.size) {
                if (remoteEndpointNames.indexOf(groupMembers[counter].endpointName) == -1) {
                    memberToDeleteFound = true
                } else {
                    counter++
                }
            }
            if (memberToDeleteFound) {
                CoroutineScope(Dispatchers.IO).launch {
                    groupMemberDao.delete(groupMembers[counter])
                }
                groupMembers.removeAt(counter)
            }
        } else {
            val localEndpointNames = groupMembers.map { element -> element.endpointName }
            var memberToAddFound = false
            var counter = 0
            while (!memberToAddFound && counter < message.fullMemberList.size) {
                if (localEndpointNames.indexOf(message.fullMemberList[counter].endpointName) == -1) {
                    memberToAddFound = true
                } else {
                    counter++
                }
            }
            if (memberToAddFound) {
                val receivedGM = message.fullMemberList[counter]
                val newGM = GroupMember(receivedGM.name, receivedGM.endpointName, receivedGM.isHost, mGroup.id!!, null, null)
                groupMembers.add(newGM)
                CoroutineScope(Dispatchers.IO).launch {
                    groupMemberDao.insertGroupMember(newGM)
                }
            }
        }
        sendMessageToEndpoint(P2PMessage.sendGroupMembersUpdateReceivedMessage(), endpointId)
    }

    /**
     * Procesa la recepción de un mensaje de tipo UPDATE_GROUP_MEMBERS_RECEIVED.
     */
    private fun processGroupMembersUpdateReceivedMessage(endpointId: String)
    {
        val index = getMemberIndexFromEndpointId(endpointId)
        if (index != -1) {
            updatingMembersChange[index] = true
            if (updatingMembersChange.all { el -> el }) {
                updateMembersInNextPicturesFetch = false
            }
            getNearbyClient().disconnectFromEndpoint(endpointId)
        }
    }

    /**
     * Procesa la recepción de un mensaje de tipo CHOOSE_NEW_HOST.
     */
    private fun processChooseNewHostMessage(endpointId: String)
    {
        if (nearbyRole == EnumHelper.NearbyRole.GUEST.role) {
            val index = getIndexOfHost()
            if (index != -1) {
                groupMembers.removeAt(index)
                membersUpdated.removeAt(index)
                updatingMembersChange.removeAt(index)
                updateMembersInNextPicturesFetch = true
                nearbyRole = EnumHelper.NearbyRole.HOST.role
                getNearbyClient().stopAdvertising()
                startAdvertisingOnGroupCreation(mGroup)
                sendMessageToEndpoint(P2PMessage.sendChooseNewHostReceivedMessage(), endpointId)
                updatePictures()
            }
        }
    }

    /**
     * Procesa la recepción de un mensaje de tipo CHOOSE_NEW_HOST_RECEIVED.
     */
    private fun processChooseNewHostReceivedMessage()
    {
        getNearbyClient().stopDiscovery()
        getNearbyClient().stopAdvertising()
        getNearbyClient().stopAllEndpoints()
        nearbyRole = EnumHelper.NearbyRole.UNDEFINED.role
        myEndpointName = ""
        groupsFound.clear()
        groupMembers.clear()
        membersUpdated.clear()
        updatingMembersChange.clear()
        isDiscovering = false
        isAdvertising = false
        updateMembersInNextPicturesFetch = false
        newHostEndpointName = ""
        mainHandler.removeCallbacks(requestPicturesUpdate)
        changeSessionStatus(EnumHelper.SessionStatus.LEFT.status)
    }


    /**
     *
     */
    private fun processCloseGroupReceivedMessage(endpointId: String)
    {
        Log.d(TAG, "Recibida notificación de que el grupo ha finalizado, vuelvo a HOME")
        getNearbyClient().stopDiscovery()
        getNearbyClient().stopAdvertising()
        getNearbyClient().stopAllEndpoints()
        nearbyRole = EnumHelper.NearbyRole.UNDEFINED.role
        myEndpointName = ""
        groupsFound.clear()
        groupMembers.clear()
        membersUpdated.clear()
        updatingMembersChange.clear()
        isDiscovering = false
        isAdvertising = false
        updateMembersInNextPicturesFetch = false
        newHostEndpointName = ""
        changeSessionStatus(EnumHelper.SessionStatus.LEFT.status)
    }


    /************************************************
     * FUNCIONES PARA LA CONSULTA CON BASE DE DATOS *
     ************************************************/

    /**
     * Devuelve el último grupo creado en base de datos.
     */
    fun getLastGroup(): LiveData<Group>
    {
        return groupDao.getLastGroup()
    }

    /**
     * Devuelve todos los miembros de un grupo.
     *
     * @param groupId ID del grupo sobre el que se quiere realizar la consulta.
     */
    fun getActiveMembersOfGroup(groupId: Long): LiveData<List<GroupMember>>
    {
        return groupMemberDao.getLiveAllGroupMembersFromGroup(groupId)
    }

    /**
     * Devuelve un listado con todos los grupos.
     */
    fun getAllGroups(): LiveData<List<Group>>
    {
        return groupDao.getAllGroupsLive()
    }

    /**
     * Devuelve un listado con todos los grupos encontrados durante el proceso de búsqueda en Nearby.
     */
    fun getAllFoundGroups(): LiveData<ArrayList<GroupFound>>
    {
        return groupsLivedata
    }

    /**
     * Devuelve el estado de la sesión actual.
     */
    fun getSessionStatus(): LiveData<Int>
    {
        return sessionStatusLivedata
    }

    /**
     * Devuelve el estado en el que se encuentra el proceso de unión a un grupo.
     */
    fun getGroupJoinCode(): LiveData<Int>
    {
        return groupJoinCode
    }

    /**
     * Devuelve todas las imágenes del grupo actual.
     */
    fun getAllImagesFromGroup(): LiveData<List<com.example.tfgandroid.database.models.Image>>
    {
        return imageDao.getAllImagesLiveData(mGroup.id!!)
    }

    /**
     * Devuelve la última imagen del  grupo actual.
     */
    fun getLastImageFromGroup(): LiveData<com.example.tfgandroid.database.models.Image>
    {
        return imageDao.getLastImageLiveData(mGroup.id!!)
    }

    /**
     * Devuelve la notificación de que una foto ha sido añadida.
     */
    fun getImagedAddedCallback(): LiveData<Int>
    {
        return imageAdded
    }

    fun getGroupDetails(groupId: Long): MyGroup
    {
        val group = groupDao.getGroupById(groupId)
        return MyGroup(group.name, group.createdAt, ArrayList(imageDao.getAllImages(groupId)), ArrayList(groupMemberDao.getAllGroupMembersFromGroup(groupId)), videoDao.getVideo(groupId))
    }

    fun deleteGroupFromDatabase(groupId: Long)
    {
        groupDao.deleteGroupById(groupId)
    }

}