package com.emberiot.emberiot.device_view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.emberiot.emberiot.data.Device
import com.emberiot.emberiot.data.DeviceUiObject
import com.emberiot.emberiot.databinding.FragmentDeviceViewBinding
import com.emberiot.emberiot.util.DeviceViewChannelUpdateCallback
import com.emberiot.emberiot.util.DeviceViewUtil
import com.emberiot.emberiot.view_model.DeviceViewModel
import com.emberiot.emberiot.view_model.LoginViewModel

class DeviceViewFragment : Fragment() {

    companion object {
        const val DEVICE_ID_ARG = "devid"
    }

    private lateinit var binding: FragmentDeviceViewBinding
    private lateinit var device: Device
    private var initDone = false
    private val deviceViewUtil = DeviceViewUtil()
    private var channelUpdateCallback: DeviceViewChannelUpdateCallback? = null
    private var lastUiObjects: List<DeviceUiObject> = listOf()

    private val loginViewModel by lazy {
        ViewModelProvider(requireActivity())[LoginViewModel::class.java]
    }
    private val deviceViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            DeviceViewModel.DeviceViewModelFactory(loginViewModel, viewLifecycleOwner)
        )[DeviceViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeviceViewBinding.inflate(layoutInflater)

        val deviceId = arguments?.getString(DEVICE_ID_ARG)

        if (deviceId == null) {
            findNavController().navigateUp()
            return binding.root
        }

        deviceViewModel.devices.observe(viewLifecycleOwner) { devices ->
            val d = devices.find { it.id == deviceId } ?: return@observe

            if (d.uiObjects != lastUiObjects) {
                initDone = false
            }

            if (!initDone) {
                channelUpdateCallback = deviceViewUtil.init(binding.viewLayout, d)
                initDone = true
                device = d
                (requireActivity() as? AppCompatActivity)?.supportActionBar?.title = d.name
                lastUiObjects = d.uiObjects
            }

            if (channelUpdateCallback == null) {
                return@observe
            }

            d.properties.forEach { (channel, value) ->
                if (value != null) {
                    channelUpdateCallback?.invoke(channel, value)
                }
            }
        }

        return binding.root
    }
}