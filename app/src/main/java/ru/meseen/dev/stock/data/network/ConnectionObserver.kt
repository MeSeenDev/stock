package ru.meseen.dev.stock.data.network

import android.annotation.TargetApi
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import ru.meseen.dev.stock.R

class ConnectionObserver(private val context: Context) : LiveData<ConnectionState>() {
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    companion object {
        const val TAG = "ConnectionObserver"
    }


    override fun onActive() {
        isNetworkAvailable()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val manager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            networkCallback = createNetworkCallback()
            networkCallback?.let {
                manager.registerDefaultNetworkCallback(it)
            }

        } else
            lollipopNetworkAvailableRequest()

    }


    private fun isNetworkAvailable() {
        if (isAvailable()) positive() else negative()
    }

    private fun isAvailable(): Boolean {// Рекомендуемая реализация от гугла Депрекнутая в api 30  по этому вот так
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //Для устройств что подключены по Эзернету (и такое бывает)
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                //Для устройств что подключены через Bluetooth модем
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            @Suppress("Deprecation")
            return connectivityManager.activeNetworkInfo?.isConnected ?: false
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun lollipopNetworkAvailableRequest() {
        val builder = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        val manager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        manager.registerNetworkCallback(builder.build(), createNetworkCallback())
    }

    private fun createNetworkCallback() = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            positive()
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            val isInternet = networkCapabilities.hasCapability(NET_CAPABILITY_INTERNET)
            val isValidated = networkCapabilities.hasCapability(NET_CAPABILITY_VALIDATED)
            val hasNetwork = isValidated && isInternet
            if (hasNetwork) {
                positive()
            } else {
                negative()
            }
        }

        override fun onLost(network: Network) {
            negative()
        }
    }


    private fun positive() {
        postValue(ConnectionState.Connected.Network(context.getString(R.string.connection_observer_restored)))
    }

    private fun negative() {
        postValue(ConnectionState.Error.Network(context.getString(R.string.connection_observer_lost)))
    }

    override fun onInactive() {
        val manager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback?.let {
            manager.unregisterNetworkCallback(it)
        }
    }

}

open class ConnectionState {
    sealed class Error : ConnectionState() {
        data class Network(val error: String) : ConnectionState()
    }

    sealed class Connected : ConnectionState() {
        data class Network(val status: String) : ConnectionState()
    }
}