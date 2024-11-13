package io.github.auag0.pgsharprouteexporter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.recyclerview.widget.RecyclerView
import io.github.auag0.pgsharprouteexporter.databinding.ListitemRouteItemBinding
import io.github.auag0.pgsharprouteexporter.model.Route

class RouteAdapter(
    private val context: Context,
    private val listener: Listener
) : RecyclerView.Adapter<RouteAdapter.ViewHolder>() {
    var routes: List<Route> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListitemRouteItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return routes.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(routes[position])
    }

    inner class ViewHolder(
        private val binding: ListitemRouteItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(route: Route) {
            binding.tvRouteName.text = route.name
            binding.tvRoutePoints.text = "%d".format(route.points.size)
            binding.btnRouteSave.setOnClickListener {
                listener.saveRouteAsGpx(route)
            }
            TooltipCompat.setTooltipText(
                binding.btnRouteSave,
                context.getString(R.string.tooltip_save_as_gpx)
            )
        }
    }

    interface Listener {
        fun saveRouteAsGpx(route: Route)
    }
}