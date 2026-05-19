package com.shopapp.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shopapp.data.model.AppNotification
import com.shopapp.data.repository.NotificationRepository
import com.shopapp.databinding.FragmentNotificationsBinding
import com.shopapp.databinding.ItemNotificationBinding
import com.shopapp.ui.home.MainActivity

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivity = requireActivity() as MainActivity
        binding.ivBack.setOnClickListener { mainActivity.navigateUp() }

        val adapter = NotificationAdapter { notification ->
            // Marcar como leída
            NotificationRepository.markRead(notification.id)
            renderList()

            // Navegar según el tipo de destino (targetType puede ser null en notifs antiguas)
            when (notification.targetType) {
                "product" -> {
                    val id = notification.targetId
                    if (!id.isNullOrBlank()) mainActivity.navigateToProductDetail(id)
                }
                "order"    -> mainActivity.navigateToOrders()
                "category" -> {
                    val id = notification.targetId
                    if (!id.isNullOrBlank()) mainActivity.navigateToCategoryProducts(id)
                }
                // null o vacío → solo marcar como leída
            }
        }

        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            this.adapter = adapter
        }

        binding.btnMarkAll.setOnClickListener {
            NotificationRepository.markAllRead()
            renderList()
            Toast.makeText(requireContext(), "Todas marcadas como leídas", Toast.LENGTH_SHORT).show()
        }

        renderList()
    }

    private fun renderList() {
        val notifications = NotificationRepository.getAll()
        val adapter = binding.rvNotifications.adapter as? NotificationAdapter
        adapter?.submitList(notifications)

        if (notifications.isEmpty()) {
            binding.layoutEmpty.visibility     = View.VISIBLE
            binding.rvNotifications.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility     = View.GONE
            binding.rvNotifications.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

class NotificationAdapter(
    private val onClick: (AppNotification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.VH>() {

    private val items = mutableListOf<AppNotification>()

    fun submitList(list: List<AppNotification>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val n = items[position]
        holder.binding.tvNotifIcon.text    = n.icon
        holder.binding.tvNotifTitle.text   = n.title
        holder.binding.tvNotifMessage.text = n.message
        holder.binding.tvNotifTime.text    = n.formattedTime
        holder.binding.vUnreadDot.visibility = if (!n.isRead) View.VISIBLE else View.GONE
        holder.binding.root.alpha = if (n.isRead) 0.75f else 1f

        // Indicar visualmente que la notificación es navegable
        val isNavigable = !n.targetType.isNullOrBlank()
        holder.binding.ivChevron.visibility = if (isNavigable) View.VISIBLE else View.GONE

        holder.binding.root.setOnClickListener { onClick(n) }
    }
}
