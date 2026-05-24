package jp.osdn.gokigen.a01lib.camera.omds.connection

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.a01lib.camera.omds.status.IOmdsCommunicationInfo
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * カメラとの接続管理クラス
 *   (ConnectivityManager.NetworkCallback を使用した実装)
 */
class OmdsCameraConnection(
    private val communicationInfo: IOmdsCommunicationInfo,
    private val cameraStatusReceiver: ICameraConnectionStatus,
    private val liveViewQuality : String = "0640x0480",
    private val userAgent: String = "OlympusCameraKit",
    private val executeUrl: String = "http://192.168.0.10"
)
{
    // 順序性を保証するため、シングルスレッドのExecutorを使用
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var connectivityManager: ConnectivityManager? = null
    private var isStartWifiWatching = false
    private val isConnecting = AtomicBoolean(false)

    // ネットワーク状態を監視するコールバック
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.v(TAG, "Network Available: $network")

            // プロセス全体をこのネットワーク（Wi-Fi）に縛り付ける
            connectivityManager?.bindProcessToNetwork(network)

            // Wi-Fi接続が確認できたらカメラ接続処理へ
            cameraStatusReceiver.onStatusNotify(ICameraConnectionStatus.CameraConnectionStatus.CHECK_WIFI)
            connect()
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            Log.v(TAG, "onCapabilitiesChanged")
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties)
        {
            super.onLinkPropertiesChanged(network, linkProperties)
            Log.v(TAG, "onCapabilitiesChanged")
        }

        override fun onLost(network: Network)
        {
            super.onLost(network)
            connectivityManager?.bindProcessToNetwork(null)
            Log.v(TAG, "Network Lost")
            isConnecting.set(false)
        }
    }

    // ----- Wi-Fi状態の監視を開始
    fun startWatchWifiStatus(context: Context)
    {
        Log.v(TAG, "startWatchWifiStatus()")
        try
        {
            if (!isStartWifiWatching)
            {
                cameraStatusReceiver.onStatusNotify(ICameraConnectionStatus.CameraConnectionStatus.START)
                connectivityManager =
                    context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

                // Wi-Fiネットワークのみを対象にするリクエスト
                val request = NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .build()

                connectivityManager?.registerNetworkCallback(request, networkCallback)
                isStartWifiWatching = true
                cameraStatusReceiver.onStatusNotify(ICameraConnectionStatus.CameraConnectionStatus.DISCONNECTED)
            }
        }
        catch (e: Exception)
        {
            Log.e(TAG, "Failed to register network callback", e)
            isStartWifiWatching = false
        }
    }

    // ----- Wi-Fi状態の監視を停止 (メモリリーク防止のため必要）
    fun stopWatchWifiStatus()
    {
        Log.v(TAG, "stopWatchWifiStatus()")
        try
        {
            isStartWifiWatching = false
            connectivityManager?.unregisterNetworkCallback(networkCallback)
            connectivityManager = null
        }
        catch (e: Exception)
        {
            Log.e(TAG, "Failed to unregister network callback", e)
        }
    }

    // ----- カメラとの切断処理
    fun disconnect(powerOff: Boolean)
    {
        Log.v(TAG, "disconnectFromCamera(powerOff=$powerOff)")

        // --- Executorが終了している場合は処理をスキップ
        if (cameraExecutor.isShutdown) {
            Log.w(TAG, "disconnect(): cameraExecutor is already shutdown. Skipping.")
            return
        }

        try
        {
            cameraExecutor.execute(OmdsCameraDisconnectSequence(powerOff, cameraStatusReceiver, userAgent, executeUrl))
        }
        catch (e: Exception)
        {
            Log.w(TAG, "disconnect EXCEPTION", e)
        }
    }

    // ----- カメラとの接続処理
    fun connect()
    {
        if (!isConnecting.compareAndSet(false, true)) {
            Log.d(TAG, "connectToCamera(): Already connecting or connect task is queued. Skipping.")
            return
        }
        Log.v(TAG, "connectToCamera() : Start connect sequence")
        try
        {
            cameraExecutor.execute {
                try
                {
                    cameraStatusReceiver.onStatusNotify(ICameraConnectionStatus.CameraConnectionStatus.CONNECTING)
                    OmdsCameraConnectSequence(
                        cameraStatusReceiver,
                        communicationInfo,
                        liveViewQuality,
                        userAgent,
                        executeUrl
                    ).run()
                }
                catch (ee: Exception)
                {
                    Log.e(TAG, "Connect sequence failed", ee)
                    cameraStatusReceiver.onStatusNotify(ICameraConnectionStatus.CameraConnectionStatus.ERROR)
                }
                finally
                {
                    // 成功・失敗に関わらず、処理が終わったらフラグを下ろす
                    isConnecting.set(false)
                    Log.v(TAG, "connectToCamera() : Sequence finished.")
                }
            }
        }
        catch (e: Exception)
        {
            Log.e(TAG, "connectToCamera() : EXCEPTION: ${e.message}", e)
            cameraStatusReceiver.onStatusNotify(ICameraConnectionStatus.CameraConnectionStatus.EXCEPTION)
            isConnecting.set(false)
        }
    }

    // ----- Executorのシャットダウン
    fun release()
    {
        stopWatchWifiStatus()
        cameraExecutor.shutdown()
    }

    companion object
    {
        private val TAG = OmdsCameraConnection::class.java.simpleName
    }
}
