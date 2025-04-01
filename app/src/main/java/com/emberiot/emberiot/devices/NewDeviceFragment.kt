package com.emberiot.emberiot.devices

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emberiot.emberiot.EmberIotApp
import com.emberiot.emberiot.R
import com.emberiot.emberiot.data.Device
import com.emberiot.emberiot.data.DevicePropertyDefinition
import com.emberiot.emberiot.data.enum.PropertyType
import com.emberiot.emberiot.databinding.FragmentNewDeviceBinding
import com.emberiot.emberiot.view_model.DeviceViewModel
import com.emberiot.emberiot.view_model.LoginViewModel
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.maltaisn.icondialog.IconDialog
import com.maltaisn.icondialog.IconDialogSettings
import com.maltaisn.icondialog.data.Icon
import kotlinx.coroutines.launch


class NewDeviceFragment : Fragment() {

    companion object {
        const val DEVICE_PARAM = "device-id"
    }

    private val iconDialogTag: String = "icon-dialog"

    private lateinit var binding: FragmentNewDeviceBinding
    private var selectedIcon: Icon? = EmberIotApp.iconPack?.getIcon(0)
    private lateinit var currentDevice: Device
    private lateinit var channelListAdapter: DataChannelListAdapter
    private var isEdit = false;

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
        binding = FragmentNewDeviceBinding.inflate(layoutInflater)

        val iconDialog =
            requireActivity().supportFragmentManager.findFragmentByTag(iconDialogTag) as IconDialog?
                ?: IconDialog.newInstance(IconDialogSettings())

        binding.btnSelectIcon.setOnClickListener {
            iconDialog.show(
                requireActivity().supportFragmentManager,
                iconDialogTag
            )
        }
        binding.btnSelectIcon.icon = selectedIcon?.drawable
        binding.btnAddChannel.setOnClickListener { openChannelDialog() }

        channelListAdapter = DataChannelListAdapter(
            { onRemoveChannel(it) },
            { item, index -> openChannelDialog(item, index) })
        binding.channelList.adapter = channelListAdapter
        binding.channelList.layoutManager = LinearLayoutManager(requireContext())

        val device = arguments?.getString(DEVICE_PARAM)?.let {
            deviceViewModel.devices.value?.find { d -> d.id == it }
        }
        if (device == null) {
            currentDevice = Device()
        } else {
            currentDevice = device

            binding.editDeviceName.setText(currentDevice.name)
            binding.btnSelectIcon.icon =
                EmberIotApp.iconPack?.getIcon(currentDevice.iconId)?.drawable
                    ?: selectedIcon?.drawable

            channelListAdapter.list.clear()
            channelListAdapter.list.addAll(currentDevice.propertyDefinitions.filter { e -> e.value != null }
                .map { e ->
                    e.value!!
                }.toMutableList())
            channelListAdapter.notifyItemRangeInserted(0, channelListAdapter.itemCount)
        }

        return binding.root
    }

    private fun onAddOrUpdateChannel(
        name: String,
        type: PropertyType,
        possibleValues: List<String>,
        updatePosition: Int = -1
    ) {
        val newProp = DevicePropertyDefinition(name, type, possibleValues)
        currentDevice.propertyDefinitions[DevicePropertyDefinition.getPropId(currentDevice.propertyDefinitions.size)] =
            newProp

        if (updatePosition >= 0) {
            channelListAdapter.list[updatePosition] = newProp
            channelListAdapter.notifyItemChanged(updatePosition)
        } else {
            channelListAdapter.list.add(newProp)
            channelListAdapter.notifyItemInserted(channelListAdapter.itemCount - 1)
        }
    }

    fun onSave() {
        val name = binding.editDeviceName.text.toString()

        if (name.isBlank()) {
            Toast.makeText(requireContext(), R.string.device_invalid_name, Toast.LENGTH_SHORT)
                .show()
            return
        }

        currentDevice.name = binding.editDeviceName.text.toString()
        currentDevice.iconId = selectedIcon?.id ?: 0

        viewLifecycleOwner.lifecycleScope.launch {
            deviceViewModel.addOrUpdateDevice(currentDevice)
            findNavController().navigate(R.id.nav_devices, null, NavOptions.Builder().apply {
                setPopUpTo(R.id.newDeviceFragment, true)
            }.build())
        }
    }

    private fun onRemoveChannel(index: Int) {
        currentDevice.propertyDefinitions.remove(DevicePropertyDefinition.getPropId(index))
    }

    private fun openChannelDialog(prop: DevicePropertyDefinition? = null, index: Int = -1) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle(if (prop == null) R.string.add_new_channel else R.string.edit_new_channel)
        builder.setCancelable(false)

        val inflater = layoutInflater
        val dialogView: View = inflater.inflate(R.layout.new_property_dialog, null)
        builder.setView(dialogView)

        val valuesList = dialogView.findViewById<RecyclerView>(R.id.valueListView)
        valuesList.layoutManager = LinearLayoutManager(requireContext())
        val possibleValuesAdapter = NewValueListAdapter(valuesList)
        valuesList.adapter = possibleValuesAdapter

        val addBtn = dialogView.findViewById<Button>(R.id.addValueBtn)
        addBtn.setOnClickListener {
            possibleValuesAdapter.currentList.add("")
            possibleValuesAdapter.notifyItemInserted(possibleValuesAdapter.currentList.size - 1)
        }

        val spinner = dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.typeSpinner)
        val typeAdapter = ArrayAdapter<String>(
            requireContext(),
            R.layout.simple_list_item,
            PropertyType.entries.map { getString(it.labelId) })
        spinner.setAdapter(typeAdapter)

        var selectedSpinnerItem = -1
        spinner.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                if (position < 0) {
                    return@OnItemClickListener
                }

                selectedSpinnerItem = position

                val type = PropertyType.entries[position]
                if (type == PropertyType.ENUM) {
                    valuesList.visibility = View.VISIBLE
                    addBtn.visibility = View.VISIBLE
                } else {
                    valuesList.visibility = View.GONE
                    addBtn.visibility = View.GONE
                }
            }

        val channelNameField = dialogView.findViewById<TextInputEditText>(R.id.nameEditText)!!

        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.setPositiveButton(R.string.save, null)

        val dialog = builder.create()

        dialog.setOnShowListener {
            val b: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            b.setOnClickListener {
                val name = channelNameField.text.toString()
                val possibleValues: List<String> = possibleValuesAdapter.currentList

                if (name.isBlank()) {
                    Toast.makeText(requireContext(), R.string.invalid_name, Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                if (selectedSpinnerItem == -1) {
                    Toast.makeText(requireContext(), R.string.invalid_type, Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                val type = PropertyType.entries[selectedSpinnerItem]
                if (type == PropertyType.ENUM && possibleValues.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.invalid_values, Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                onAddOrUpdateChannel(
                    name,
                    type,
                    if (type == PropertyType.ENUM) possibleValues else emptyList(),
                    index
                )
                dialog.dismiss()
            }
        }

        if (prop != null) {
            possibleValuesAdapter.currentList.addAll(prop.possibleValues)
            selectedSpinnerItem = prop.type.ordinal
            spinner.setText(prop.type.labelId)
            channelNameField.setText(prop.name)

            if (prop.type == PropertyType.ENUM) {
                valuesList.visibility = View.VISIBLE
                addBtn.visibility = View.VISIBLE
            } else {
                valuesList.visibility = View.GONE
                addBtn.visibility = View.GONE
            }
        }

        dialog.show()
    }

    fun onIconSelected(dialog: IconDialog, icons: List<Icon>) {
        icons.firstOrNull()?.let {
            binding.btnSelectIcon.icon = it.drawable
            selectedIcon = it
        }
    }

    override fun onResume() {
        super.onResume()
        isEdit = currentDevice.id != null
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.setTitle(if (isEdit) R.string.edit_device else R.string.new_device)
    }
}