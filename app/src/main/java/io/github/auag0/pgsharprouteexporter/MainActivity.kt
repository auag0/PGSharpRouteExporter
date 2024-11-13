package io.github.auag0.pgsharprouteexporter

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.divider.MaterialDividerItemDecoration
import io.github.auag0.pgsharprouteexporter.databinding.ActivityMainBinding
import io.github.auag0.pgsharprouteexporter.model.Route
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), RouteAdapter.Listener {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = view.paddingLeft + systemInsets.left,
                top = view.paddingTop + systemInsets.top,
                right = view.paddingRight + systemInsets.right,
                bottom = view.paddingBottom + systemInsets.bottom
            )
            insets
        }

        viewModel.loadPGOAndRoutes()

        val routeAdapter = RouteAdapter(this.applicationContext, this)
        val layoutManager = LinearLayoutManager(this)
        val itemDecoration = MaterialDividerItemDecoration(this, layoutManager.orientation)
        itemDecoration.isLastItemDecorated = false

        binding.rvFavoriteRoute.adapter = routeAdapter
        binding.rvFavoriteRoute.layoutManager = layoutManager
        binding.rvFavoriteRoute.addItemDecoration(itemDecoration)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.pgo.collect { packageInfo: PackageInfo? ->
                        binding.proAppLoading.visibility = View.GONE
                        if (packageInfo == null) {
                            setNotInstalledState()
                        } else {
                            setInstalledPGOState(packageInfo)
                        }
                    }
                }

                launch {
                    viewModel.routes.collect { routes ->
                        routeAdapter.routes = routes
                    }
                }

                launch {
                    viewModel.isLoading.collect { isLoading ->
                        binding.proRouteLoading.isVisible = isLoading
                    }
                }

                launch {
                    viewModel.toastMsg.collect {
                        val msg = when (it) {
                            ToastMeg.EmptyRoutes -> getString(R.string.toast_empty_routes)
                            ToastMeg.NotInstalled -> getString(R.string.toast_not_installed)
                            ToastMeg.SavedAsGpx -> getString(R.string.toast_saved_as_gpx)
                            is ToastMeg.Error -> it.e.message
                            null -> return@collect
                        }
                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                        viewModel.clearToastMsg()
                    }
                }
            }
        }
    }

    private fun setNotInstalledState() {
        binding.tvNotInstalled.visibility = View.VISIBLE
        binding.ivPGOIcon.setImageResource(android.R.mipmap.sym_def_app_icon)
        binding.tvPGOName.text = null
        binding.tvPGOPackageName.text = null
        binding.tvPGOVersion.text = null
    }

    private fun setInstalledPGOState(packageInfo: PackageInfo) {
        val appInfo = packageInfo.applicationInfo ?: return
        binding.tvNotInstalled.visibility = View.GONE
        Glide.with(this)
            .load(packageInfo)
            .error(android.R.mipmap.sym_def_app_icon)
            .into(binding.ivPGOIcon)
        binding.tvPGOName.text = appInfo.loadLabel(packageManager)
        binding.tvPGOPackageName.text = packageInfo.packageName
        val versionText = getString(
            R.string.version_format,
            packageInfo.versionName,
            PackageInfoCompat.getLongVersionCode(packageInfo)
        )
        binding.tvPGOVersion.text = HtmlCompat.fromHtml(
            versionText,
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }

    private val saveLauncher =
        registerForActivityResult(
            ActivityResultContracts.CreateDocument("application/gpx+xml")
        ) { uri ->
            uri?.let { safeUri ->
                viewModel.saveRouteAsGpx(safeUri)
            } ?: run {
                viewModel.saveRoute = null
            }
        }

    override fun saveRouteAsGpx(route: Route) {
        if (viewModel.saveRoute == null) {
            viewModel.saveRoute = route
            saveLauncher.launch("${route.name}.gpx")
        }
    }
}