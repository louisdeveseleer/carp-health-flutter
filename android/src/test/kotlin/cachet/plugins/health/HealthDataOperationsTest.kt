package cachet.plugins.health

import android.os.RemoteException
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel.Result
import java.lang.reflect.Proxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Test

class HealthDataOperationsTest {

    private class RecordingResult : Result {
        val outcomes = mutableListOf<Any?>()

        override fun success(result: Any?) {
            outcomes.add(result)
        }

        override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
            outcomes.add("error:$errorCode")
        }

        override fun notImplemented() {
            outcomes.add("notImplemented")
        }
    }

    /** A client whose permission controller fails the way an unbindable Health Connect does. */
    private fun unbindableClient(): HealthConnectClient {
        val controller =
                object : PermissionController {
                    override suspend fun getGrantedPermissions(): Set<String> =
                            throw RemoteException("Binding to service failed")

                    override suspend fun revokeAllPermissions() =
                            throw RemoteException("Binding to service failed")
                }
        return Proxy.newProxyInstance(
                HealthConnectClient::class.java.classLoader,
                arrayOf(HealthConnectClient::class.java),
        ) { _, method, _ ->
            if (method.name == "getPermissionController") controller
            else throw UnsupportedOperationException(method.name)
        } as HealthConnectClient
    }

    private fun operations() =
            HealthDataOperations(
                    unbindableClient(),
                    CoroutineScope(Dispatchers.Unconfined),
                    HealthConnectClient.SDK_AVAILABLE,
                    true,
            )

    @Test
    fun hasPermissionsReportsAPlatformErrorWhenHealthConnectCannotBeBound() {
        val result = RecordingResult()
        val call =
                MethodCall(
                        "hasPermissions",
                        hashMapOf("types" to arrayListOf("WEIGHT"), "permissions" to arrayListOf(0)),
                )

        operations().hasPermissions(call, result)

        assertEquals(listOf<Any?>("error:HEALTH_CONNECT_ERROR"), result.outcomes)
    }

    @Test
    fun isHealthDataHistoryAuthorizedReportsAPlatformErrorWhenHealthConnectCannotBeBound() {
        val result = RecordingResult()

        operations().isHealthDataHistoryAuthorized(MethodCall("isHealthDataHistoryAuthorized", null), result)

        assertEquals(listOf<Any?>("error:HEALTH_CONNECT_ERROR"), result.outcomes)
    }
}
