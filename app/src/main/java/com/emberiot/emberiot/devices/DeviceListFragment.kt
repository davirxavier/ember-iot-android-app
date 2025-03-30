package com.emberiot.emberiot.devices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.emberiot.emberiot.databinding.FragmentDeviceListBinding
import com.emberiot.emberiot.view_model.DeviceViewModel
import com.emberiot.emberiot.view_model.LoginViewModel

class DeviceListFragment : Fragment() {

    private lateinit var binding: FragmentDeviceListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeviceListBinding.inflate(inflater)

        val adapter = DeviceListAdapter(requireContext())
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
}