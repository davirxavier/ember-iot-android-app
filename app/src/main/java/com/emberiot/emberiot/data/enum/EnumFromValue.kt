package com.emberiot.emberiot.data.enum

interface EnumFromValue<T, S : Enum<S>> {

    fun getValueInternal(): T

    fun getLabelId(): Int

    fun getValues(): List<EnumFromValue<T, S>>

    fun getDefault(): EnumFromValue<T, S>

    companion object {
        fun <T, S, X : EnumFromValue<T, S>> fromValue(value: T?, outClazz: Class<X>): S? {
            return if (value == null) null else outClazz.enumConstants?.find { it.getValueInternal() == value }?.let { it as? S }
        }

        fun <T, S : Enum<S>, X : EnumFromValue<T, S>> fromValue(value: T?, instance: X): EnumFromValue<T, S>? {
            return if(value == null) null else instance.getValues().find { it.getValueInternal() == value }
        }
    }
}