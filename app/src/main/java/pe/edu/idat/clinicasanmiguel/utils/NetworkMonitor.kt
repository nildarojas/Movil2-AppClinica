package pe.edu.idat.clinicasanmiguel.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object NetworkMonitor {

    private lateinit var connectivityManager:
            ConnectivityManager

    private val estadoMutable =
        MutableLiveData<Boolean>()

    val estadoConexion:
            LiveData<Boolean>
        get() = estadoMutable

    @Volatile
    private var iniciado =
        false

    @Volatile
    private var estadoActual:
            Boolean? = null

    private val networkCallback =
        object :
            ConnectivityManager.NetworkCallback() {

            override fun onAvailable(
                network: Network
            ) {
                actualizarEstado()
            }

            override fun onLost(
                network: Network
            ) {
                actualizarEstado()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                actualizarEstado()
            }
        }

    @Synchronized
    fun iniciar(
        context: Context
    ) {
        if (!::connectivityManager.isInitialized) {
            connectivityManager =
                context.applicationContext
                    .getSystemService(
                        Context.CONNECTIVITY_SERVICE
                    ) as ConnectivityManager
        }

        publicarEstado(
            comprobarConexion()
        )

        if (iniciado) {
            return
        }

        try {
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.N
            ) {
                connectivityManager
                    .registerDefaultNetworkCallback(
                        networkCallback
                    )
            } else {
                val networkRequest =
                    NetworkRequest.Builder()
                        .addCapability(
                            NetworkCapabilities
                                .NET_CAPABILITY_INTERNET
                        )
                        .build()

                connectivityManager
                    .registerNetworkCallback(
                        networkRequest,
                        networkCallback
                    )
            }

            iniciado = true

        } catch (exception: Exception) {
            iniciado = false

            publicarEstado(
                comprobarConexion()
            )
        }
    }

    @Synchronized
    fun detener() {
        if (
            !iniciado ||
            !::connectivityManager.isInitialized
        ) {
            return
        }

        try {
            connectivityManager
                .unregisterNetworkCallback(
                    networkCallback
                )
        } catch (exception: Exception) {
        }

        iniciado = false
    }

    fun hayInternet():
            Boolean {
        if (!::connectivityManager.isInitialized) {
            return false
        }

        return comprobarConexion()
    }

    private fun actualizarEstado() {
        publicarEstado(
            comprobarConexion()
        )
    }

    private fun comprobarConexion():
            Boolean {
        return try {
            val redActiva =
                connectivityManager.activeNetwork
                    ?: return false

            val capacidades =
                connectivityManager
                    .getNetworkCapabilities(
                        redActiva
                    ) ?: return false

            capacidades.hasCapability(
                NetworkCapabilities
                    .NET_CAPABILITY_INTERNET
            ) &&
                    capacidades.hasCapability(
                        NetworkCapabilities
                            .NET_CAPABILITY_VALIDATED
                    )

        } catch (exception: SecurityException) {
            false
        }
    }

    private fun publicarEstado(
        conectado: Boolean
    ) {
        if (
            estadoActual == conectado &&
            estadoMutable.value != null
        ) {
            return
        }

        estadoActual =
            conectado

        estadoMutable.postValue(
            conectado
        )
    }
}