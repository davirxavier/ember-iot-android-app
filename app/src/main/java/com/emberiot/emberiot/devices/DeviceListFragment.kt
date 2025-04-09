package com.emberiot.emberiot.devices

import android.R.attr.label
import android.R.attr.text
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.emberiot.emberiot.R
import com.emberiot.emberiot.databinding.FragmentDeviceListBinding
import com.emberiot.emberiot.device_view.DeviceViewFragment
import com.emberiot.emberiot.util.OnActionClick
import com.emberiot.emberiot.view_model.DeviceViewModel
import com.emberiot.emberiot.view_model.LoginViewModel
import kotlinx.coroutines.launch


class DeviceListFragment : Fragment(), OnActionClick {

    private lateinit var binding: FragmentDeviceListBinding
    private lateinit var adapter: DeviceListAdapter
    private lateinit var deviceViewModel: DeviceViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeviceListBinding.inflate(inflater)

        adapter = DeviceListAdapter(requireContext()) { d ->
            findNavController().navigate(R.id.viewDeviceFragment, Bundle().apply {
                putString(DeviceViewFragment.DEVICE_ID_ARG, d.id)
            })
        }

        binding.list.adapter = adapter

        val loginViewModel = ViewModelProvider(requireActivity())[LoginViewModel::class.java]
        deviceViewModel = ViewModelProvider(
            requireActivity(),
            DeviceViewModel.DeviceViewModelFactory(loginViewModel, viewLifecycleOwner)
        )[DeviceViewModel::class.java]

        deviceViewModel.devices.observe(viewLifecycleOwner) { devices ->
            adapter.submitList(devices)
            binding.deviceProgress.visibility = View.GONE
            binding.list.visibility = View.VISIBLE
        }

        return binding.root
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (adapter.contextMenuCurrentId == null)
            return super.onContextItemSelected(item)

        if (item.itemId == 0) {
            val clipboard = getSystemService(requireContext(), ClipboardManager::class.java)
            val clip = ClipData.newPlainText(getString(R.string.device_id), adapter.contextMenuCurrentId)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(requireContext(), R.string.device_id_copied, Toast.LENGTH_SHORT).show()
        } else if (item.itemId == 1) {
            findNavController().navigate(
                R.id.newDeviceFragment,
                bundleOf(NewDeviceFragment.DEVICE_PARAM to adapter.contextMenuCurrentId!!)
            )
        } else {
            AlertDialog.Builder(requireContext()).apply {
                setTitle(R.string.confirm_deletion)

                setNegativeButton(R.string.cancel) { _, _ -> }
                setPositiveButton(R.string.confirm) { d, _ ->
                    adapter.contextMenuCurrentId?.let {
                        viewLifecycleOwner.lifecycleScope.launch {
                            deviceViewModel.deleteDevice(it)
                        }
                    }
                }
            }.create().show()
        }

        return super.onContextItemSelected(item)
    }

    override fun onActionClick(actionId: Int): Boolean {
        if (actionId == R.id.action_add) {
            findNavController().navigate(R.id.newDeviceFragment)
            return true
        } else {
            return false
        }
    }
}