package com.emberiot.emberiot.device_view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emberiot.emberiot.R
import com.emberiot.emberiot.data.Device
import com.emberiot.emberiot.data.DevicePropertyDefinition
import com.emberiot.emberiot.data.DeviceUiObject
import com.emberiot.emberiot.data.enum.EmberButtonStyle
import com.emberiot.emberiot.data.enum.EmberButtonType
import com.emberiot.emberiot.data.enum.EnumFromValue
import com.emberiot.emberiot.data.enum.LabelSize
import com.emberiot.emberiot.data.enum.LabelType
import com.emberiot.emberiot.data.enum.UiObjectType
import com.emberiot.emberiot.databinding.FragmentUiElementConfigBinding
import com.emberiot.emberiot.util.DeviceViewUtil
import com.emberiot.emberiot.util.OnActionClick
import com.emberiot.emberiot.view.EmberButton
import com.emberiot.emberiot.view.EmberUiClass
import com.emberiot.emberiot.view_model.DeviceViewModel
import com.emberiot.emberiot.view_model.LoginViewModel
import com.emberiot.emberiot.view_model.UiElementConfigViewModel
import kotlinx.coroutines.launch
import kotlin.math.log

class UiElementConfigFragment : Fragment(), OnActionClick {

    companion object {
        class BindHolder(view: View, val name: TextView, val desc: TextView) :
            RecyclerView.ViewHolder(view) {
        }

        class BindAdapter(
            private val list: List<DevicePropertyDefinition>,
            private val itemClicked: (propDef: DevicePropertyDefinition) -> Unit,
        ) :
            RecyclerView.Adapter<BindHolder>() {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.add_ui_element_item, parent, false)
                return BindHolder(
                    view,
                    view.findViewById(R.id.name),
                    view.findViewById(R.id.description)
                )
            }

            override fun getItemCount(): Int {
                return list.size
            }

            override fun onBindViewHolder(holder: BindHolder, position: Int) {
                val def = list[position]

                holder.name.text = "${def.id} - ${def.name}"
                holder.desc.setText(def.type.labelId)

                holder.itemView.setOnClickListener {
                    itemClicked(def)
                }
            }
        }
    }

    private val loginViewModel by lazy {
        ViewModelProvider(requireActivity())[LoginViewModel::class.java]
    }
    private val deviceViewModel by lazy {
        ViewModelProvider(requireActivity(), DeviceViewModel.DeviceViewModelFactory(loginViewModel, viewLifecycleOwner))[DeviceViewModel::class.java]
    }

    private val allElements = setOf(
        R.id.sizeSelect,
        R.id.textOffInput,
        R.id.textOnInput,
        R.id.typeSelect,
        R.id.styleSelect,
        R.id.prefixInput,
        R.id.unitsInput
    )

    private val uiElementsByType = mapOf(
        UiObjectType.BUTTON to listOf(
            R.id.sizeSelect,
            R.id.textOffInput,
            R.id.textOnInput,
            R.id.typeSelect,
            R.id.styleSelect
        ),
        UiObjectType.TEXT to listOf(
            R.id.sizeSelect,
            R.id.prefixInput,
            R.id.unitsInput
        )
    )

    private val enumsToUiElements by lazy {
        mapOf<EnumFromValue<String, *>, AppCompatAutoCompleteTextView>(
            EmberButtonStyle.entries.first() to binding.styleSelect,
            EmberButtonType.entries.first() to binding.typeSelect,
            LabelSize.entries.first() to binding.sizeSelect,
            LabelType.entries.first() to binding.labelTypeSelect
        )
    }

    private val fieldParamsToElements by lazy {
        mapOf<String, TextView>(
            EmberButton.TEXT_OFF to binding.textOffInput,
            EmberButton.TEXT_ON to binding.textOnInput,
            DeviceViewUtil.LABEL_PARAM to binding.labelText
        )
    }

    private lateinit var binding: FragmentUiElementConfigBinding
    private lateinit var currentEditing: DeviceUiObject
    private lateinit var currentDevice: Device
    private val modifiedParams: MutableMap<String, String> = mutableMapOf()
    private val handler = Handler(Looper.getMainLooper())
    private var previewObj: View? = null
    private var propDef: DevicePropertyDefinition? = null

    private val uiElementConfigViewModel by lazy {
        ViewModelProvider(requireActivity())[UiElementConfigViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUiElementConfigBinding.inflate(layoutInflater)

        val currentEditing = uiElementConfigViewModel.currentlyEdited
        val currentDevice = uiElementConfigViewModel.currentDevice

        if (currentEditing == null) {
            findNavController().popBackStack()
            return binding.root
        }

        if (currentDevice == null) {
            findNavController().popBackStack()
            return binding.root
        }

        this.currentEditing = currentEditing
        this.currentDevice = currentDevice

        allElements.forEach { e ->
            binding.root.findViewById<View>(e)?.let { it.visibility = View.GONE }
        }

        uiElementsByType[currentEditing.type]?.let { params ->
            params.forEach { e ->
                binding.root.findViewById<View>(e)?.let { it.visibility = View.VISIBLE }
            }
        }

        previewObj = DeviceViewUtil.getClass(requireContext(), currentEditing.type)?.apply {
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            }

            id = View.generateViewId()
        }

        modifiedParams.clear()
        modifiedParams.putAll(currentEditing.parameters)

        propDef = currentEditing.propDef
        updatePreview()
        prepareAllParams()
        updatePropDef()

        binding.bindChannelLayout.setOnClickListener {
            bindDataChannelModal()
        }

        binding.bindChannelBtn.setOnClickListener {
            bindDataChannelModal()
        }

        binding.removeBindBtn.setOnClickListener {
            propDef = null
            updatePropDef()
        }

        return binding.root
    }

    private fun updatePropDef() {
        if (propDef != null) {
            binding.bindChannelBtn.visibility = View.GONE
            binding.removeBindBtn.visibility = View.VISIBLE

            propDef?.let {
                binding.boundChannelName.text = "${it.id} - ${it.name}"
                binding.boundChannelDesc.setText(R.string.bound_channel_desc)
            }
        } else {
            binding.bindChannelBtn.visibility = View.VISIBLE
            binding.removeBindBtn.visibility = View.GONE

            binding.boundChannelName.setText(R.string.select_channel)
            binding.boundChannelDesc.setText(R.string.select_channel_desc)
        }
    }

    private fun bindDataChannelModal() {
        val d = androidx.appcompat.app.AlertDialog.Builder(requireContext())

        d.setTitle(R.string.select_data_channel)

        val mainView: View = layoutInflater.inflate(R.layout.add_ui_element_dialog, null)
        d.setView(mainView)

        val list = mainView.findViewById<RecyclerView>(R.id.valueListView)
        list.layoutManager = LinearLayoutManager(requireContext())

        d.setNegativeButton(R.string.cancel) { _, _ -> }
        d.create().apply {
            list.adapter = BindAdapter(currentDevice.propertyDefinitions.map { it.value }.filterNotNull()) { def ->
                dismiss()

                propDef = def
                updatePropDef()
            }

            show()
        }
    }

    private fun updateLabelVisibility(type: LabelType?) {
        if (type != null && type != LabelType.NONE) {
            binding.labelTextContainer.visibility = View.VISIBLE
        } else {
            binding.labelTextContainer.visibility = View.GONE
        }
    }

    private fun updatePreview() {
        handler.removeCallbacksAndMessages(null)
        previewObj?.let {
            binding.previewLayout.removeAllViews()
            (it as? EmberUiClass)?.parseParams(modifiedParams)
            binding.previewLayout.addView(it)

            modifiedParams[DeviceViewUtil.LABEL_PARAM]?.let { label ->
                DeviceViewUtil.createLabel(
                    label,
                    EnumFromValue.fromValue(
                        modifiedParams[DeviceViewUtil.LABEL_TYPE_PARAM],
                        LabelType::class.java
                    ) ?: LabelType.NONE,
                    binding.previewLayout, it
                )
            }
        }
    }

    private fun prepareAllParams() {
        fieldParamsToElements.forEach { entry ->
            entry.value.text = currentEditing.parameters[entry.key] ?: ""
            entry.value.addTextChangedListener {
                modifiedParams[entry.key] = it.toString()
                handler.postDelayed({ updatePreview() }, 1000)
            }
        }

        currentEditing.type.enumParams.filter { it.value != null }.forEach { entry ->
            val enum = entry.value ?: return@forEach
            val select = enumsToUiElements[enum.getValues().first()]
            var value = currentEditing.parameters[entry.key]

            if (enum is LabelType) {
                updateLabelVisibility(EnumFromValue.fromValue(value, enum) as? LabelType)
            }

            val text = (EnumFromValue.fromValue(value, enum) ?: enum.getDefault()).getLabelId()
            select?.setText(text)

            select?.setOnItemClickListener { _, _, position, _ ->
                if (position < 0) {
                    return@setOnItemClickListener
                }

                modifiedParams[entry.key] = enum.getValues()[position].getValueInternal()

                if (enum is LabelType) {
                    val selectedType = LabelType.entries[position]
                    updateLabelVisibility(selectedType)

                    if (selectedType == LabelType.NONE) {
                        modifiedParams.remove(DeviceViewUtil.LABEL_TYPE_PARAM)
                    }
                }

                updatePreview()
            }
        }

        enumsToUiElements.forEach { entry ->
            entry.value.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.simple_list_item,
                    entry.key.getValues().map { getString(it.getLabelId()) }
                ))
        }
    }

    override fun onActionClick(actionId: Int): Boolean {
        if (R.id.action_save == actionId) {
            currentDevice.id?.let {
                currentEditing.parameters.clear()
                currentEditing.parameters.putAll(modifiedParams)
                currentEditing.propDef = propDef

                viewLifecycleOwner.lifecycleScope.launch {
                    deviceViewModel.updateDeviceUi(it, currentDevice.uiObjects)
                    findNavController().popBackStack()
                }
            }
        }

        return true
    }
}