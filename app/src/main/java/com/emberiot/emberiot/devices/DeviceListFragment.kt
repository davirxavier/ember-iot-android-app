package com.emberiot.emberiot.devices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.emberiot.emberiot.R
import com.emberiot.emberiot.databinding.FragmentDeviceListBinding
import com.emberiot.emberiot.device_view.DeviceViewFragment
import com.emberiot.emberiot.util.OnActionClick
import com.emberiot.emberiot.view_model.DeviceViewModel
import com.emberiot.emberiot.view_model.LoginViewModel

class DeviceListFragment : Fragment(), OnActionClick {

    private lateinit var binding: FragmentDeviceListBinding
    private lateinit var adapter: DeviceListAdapter

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
        val deviceViewModel = ViewModelProvider(requireActivity(), DeviceViewModel.DeviceViewModelFactory(loginViewModel, viewLifecycleOwner))[DeviceViewModel::class.java]

        deviceViewModel.devices.observe(viewLifecycleOwner) { devices ->
            adapter.submitList(devices)
            binding.deviceProgress.visibility = View.GONE
            binding.list.visibility = View.VISIBLE
        }

        return binding.root
    }

    override fun onActionClick(actionId: Int): Boolean {
        if (actionId == R.id.action_add) {
            findNavController().navigate(R.id.newDeviceFragment)
            return true
        }
        else {
            return false
        }
    }
}