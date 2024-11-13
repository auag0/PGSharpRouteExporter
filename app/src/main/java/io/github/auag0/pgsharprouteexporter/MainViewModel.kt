package io.github.auag0.pgsharprouteexporter

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.io.SuFile
import io.github.auag0.pgsharprouteexporter.model.LatLng
import io.github.auag0.pgsharprouteexporter.model.Route
import io.github.auag0.pgsharprouteexporter.utils.SPParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray

sealed class ToastMeg {
    data object EmptyRoutes : ToastMeg()
    data object NotInstalled : ToastMeg()
    data object SavedAsGpx : ToastMeg()
    data class Error(val e: Exception) : ToastMeg()
}

class MainViewModel(private val app: Application) : AndroidViewModel(app) {
    private var _pgo: MutableStateFlow<PackageInfo?> = MutableStateFlow(null)
    val pgo = _pgo.asStateFlow()

    private var _routes: MutableStateFlow<List<Route>> = MutableStateFlow(emptyList())
    val routes = _routes.asStateFlow()

    private var _toastMsg: MutableStateFlow<ToastMeg?> = MutableStateFlow(null)
    val toastMsg = _toastMsg.asStateFlow()

    private var _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var isLoading = _isLoading.asStateFlow()

    var saveRoute: Route? = null

    fun clearToastMsg() {
        _toastMsg.value = null
    }

    private suspend fun loadRoutes(appInfo: ApplicationInfo) {
        _routes.emit(emptyList())
        val prefsFile = SuFile(
            appInfo.dataDir,
            "shared_prefs/com.google.android.gms.chimera.hl2.xml"
        )
        val xmlText = prefsFile.newInputStream().reader().use { it.readText() }
        val prefs = SPParser.parseXmlText(xmlText)
        val hlfavorRoute = prefs["hlfavorRoute"] ?: run {
            _toastMsg.emit(ToastMeg.EmptyRoutes)
            return
        }
        val favoriteRoutes = JSONArray(hlfavorRoute.toString())
        val routes: ArrayList<Route> = ArrayList()
        for (i in 0 until favoriteRoutes.length()) {
            val route = favoriteRoutes.getJSONObject(i)
            val routeName = route.getString("name")
            val routePoints = route.getJSONArray("points")
            val points = (0 until routePoints.length()).map { pointIndex ->
                val point = routePoints.getJSONArray(pointIndex)
                val latitude = point.optDouble(0)
                val longitude = point.optDouble(1)
                LatLng(latitude, longitude)
            }
            routes.add(Route(routeName, points))
        }
        _routes.emit(routes)
    }

    fun loadPGOAndRoutes() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.emit(true)
                val packageInfo = app.packageManager.getPackageInfo("com.nianticlabs.pokemongo", 0)
                _pgo.emit(packageInfo)

                val appInfo = packageInfo?.applicationInfo
                if (packageInfo == null || appInfo == null) {
                    _toastMsg.emit(ToastMeg.NotInstalled)
                    return@launch
                }

                loadRoutes(appInfo)
            } catch (e: Exception) {
                e.printStackTrace()
                _toastMsg.emit(ToastMeg.Error(e))
            } finally {
                _isLoading.emit(false)
            }
        }
    }

    fun saveRouteAsGpx(uri: Uri) {
        if (saveRoute == null) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val gpxText = buildString {
                appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                appendLine("<gpx version=\"1.1\" creator=\"PGSharp Route Exporter\">")
                saveRoute?.points?.forEach { point ->
                    appendLine("    <wpt lat=\"${point.latitude}\" lon=\"${point.longitude}\"></wpt>")
                }
                appendLine("</gpx>")
            }
            try {
                app.contentResolver.openOutputStream(uri, "w")?.use { outputStream ->
                    outputStream.writer().use { writer ->
                        writer.write(gpxText)
                    }
                }
                _toastMsg.emit(ToastMeg.SavedAsGpx)
            } catch (e: Exception) {
                e.printStackTrace()
                _toastMsg.emit(ToastMeg.Error(e))
            } finally {
                saveRoute = null
            }
        }
    }
}