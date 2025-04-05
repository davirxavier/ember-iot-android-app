package com.emberiot.emberiot.device_view

import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emberiot.emberiot.R
import com.emberiot.emberiot.data.Device
import com.emberiot.emberiot.data.DeviceUiObject
import com.emberiot.emberiot.data.enum.UiObjectType
import com.emberiot.emberiot.databinding.FragmentDeviceViewBinding
import com.emberiot.emberiot.util.DeviceViewChannelUpdateCallback
import com.emberiot.emberiot.util.DeviceViewUtil
import com.emberiot.emberiot.util.OnActionClick
import com.emberiot.emberiot.util.UiUtils
import com.emberiot.emberiot.view.UpdateChannelFn
import com.emberiot.emberiot.view_model.DeviceViewModel
import com.emberiot.emberiot.view_model.LoginViewModel
import kotlinx.coroutines.launch

class DeviceViewFragment : Fragment(), OnActionClick {

    companion object {
        const val DEVICE_ID_ARG = "devid"

        class AddDialogHolder(view: View, val name: TextView, val desc: TextView) :
            RecyclerView.ViewHolder(view) {
        }

        class AddDialogAdapter(private val itemClicked: (item: UiObjectType) -> Unit) :
            RecyclerView.Adapter<AddDialogHolder>() {

            val list = UiObjectType.entries

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddDialogHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.add_ui_element_item, parent, false)
                return AddDialogHolder(
                    view,
                    view.findViewById(R.id.name),
                    view.findViewById(R.id.description)
                )
            }

            override fun getItemCount(): Int {
                return list.size
            }

            override fun onBindViewHolder(holder: AddDialogHolder, position: Int) {
                val enum = list[position]
                val element = DeviceViewUtil.getClass(holder.itemView.context, enum)?.apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginStart = UiUtils.dpToPx(8f, holder.itemView.resources).toInt()
                        marginEnd = UiUtils.dpToPx(8f, holder.itemView.resources).toInt()
                        gravity = Gravity.CENTER
                        weight = 0.2f
                    }

                    id = View.generateViewId()
                } ?: return

                if (element is TextView) {
                    element.setText(enum.nameLabelId)
                    element.setTextColor(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.md_theme_onBackground
                        )
                    )
                }

                (holder.itemView as? ViewGroup)?.findViewById<LinearLayout>(R.id.layout)
                    ?.addView(element)

                holder.name.setText(enum.nameLabelId)
                holder.desc.setText(enum.descLabelId)

                holder.itemView.setOnClickListener {
                    itemClicked(enum)
                }
            }
        }
    }

    private lateinit var binding: FragmentDeviceViewBinding
    private lateinit var device: Device
    private var initDone = false
    private val deviceViewUtil = DeviceViewUtil()
    private var channelUpdateCallback: DeviceViewChannelUpdateCallback? = null
    private var lastUiObjects: List<DeviceUiObject> = listOf()
    var editMode = false
        private set

    private val loginViewModel by lazy {
        ViewModelProvider(requireActivity())[LoginViewModel::class.java]
    }
    private val deviceViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            DeviceViewModel.DeviceViewModelFactory(loginViewModel, viewLifecycleOwner)
        )[DeviceViewModel::class.java]
    }

    private val updateChannelFn: DeviceViewChannelUpdateCallback = { channel, newVal ->
        if (!editMode) {
            viewLifecycleOwner.lifecycleScope.launch {
                device.id?.let { deviceViewModel.updateChannel(it, channel, newVal) }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeviceViewBinding.inflate(layoutInflater)

        val deviceId = arguments?.getString(DEVICE_ID_ARG)

        if (deviceId == null) {
            findNavController().navigateUp()
            return binding.root
        }

        deviceViewModel.devices.observe(viewLifecycleOwner) { devices ->
            if (initDone && editMode) {
                return@observe
            }

            val d = devices.find { it.id == deviceId } ?: return@observe

            if (d.uiObjects != lastUiObjects) {
                initDone = false
            }

            if (!initDone) {
                channelUpdateCallback = deviceViewUtil.init(binding.viewLayout, d, editMode, updateChannelFn)
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

    private fun onEditClick() {
        editMode = true
        channelUpdateCallback = deviceViewUtil.init(binding.viewLayout, device, editMode, updateChannelFn)
        requireActivity().invalidateOptionsMenu()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onExit()
        }
    }

    fun onExit() {
        showConfirmation(R.string.edit_device_ui_exit_confirmation_body) { findNavController().popBackStack() }
    }

    private fun onSave() {
        viewLifecycleOwner.lifecycleScope.launch {
            device.id?.let { deviceViewModel.updateDeviceUi(deviceId = it, device.uiObjects) }
            findNavController().popBackStack()
        }
    }

    private fun showConfirmation(bodyText: Int, cb: (d: DialogInterface) -> Unit) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.edit_device_ui_confirmation_title)
            setMessage(bodyText)
            setNegativeButton(R.string.cancel) { d, _ -> d.dismiss() }
            setPositiveButton(R.string.confirm) { d, _ ->
                d.dismiss()
                cb(d)
            }
        }.create().show()
    }

    private fun showAddModal() {
        val d = AlertDialog.Builder(requireContext())

        d.setTitle(R.string.add_new_ui_element)

        val mainView: View = layoutInflater.inflate(R.layout.add_ui_element_dialog, null)
        d.setView(mainView)

        val list = mainView.findViewById<RecyclerView>(R.id.valueListView)
        list.layoutManager = LinearLayoutManager(requireContext())

        d.setNegativeButton(R.string.cancel) { _, _ -> }
        d.create().apply {
            list.adapter = AddDialogAdapter() { type ->
                dismiss()

                device.uiObjects.add(
                    DeviceUiObject(
                        id = device.uiObjects.size.toString(),
                        propDef = null,
                        type = type,
                        parameters = mutableMapOf(),
                        horizontalPosition = 0.5f,
                        verticalPosition = 0.5f
                    )
                )

                channelUpdateCallback = deviceViewUtil.init(binding.viewLayout, device, editMode, updateChannelFn)
            }

            show()
        }
    }

    override fun onActionClick(actionId: Int): Boolean {
        if (actionId == R.id.action_view_save) {
            showConfirmation(R.string.edit_device_ui_save_confirmation_body) { onSave() }
        } else if (actionId == R.id.action_view_add) {
            showAddModal()
        } else if (actionId == R.id.action_edit) {
            onEditClick()
        }

        return true
    }


}