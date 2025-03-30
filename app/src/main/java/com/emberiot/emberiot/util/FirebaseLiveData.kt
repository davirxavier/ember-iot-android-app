package com.emberiot.emberiot.util

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class FirebaseLiveData<T>(
    query: Query?,
    builder: DataBuilder<T>
) : LiveData<T>() {
    private val listener: FirebaseLiveDataEventListener = FirebaseLiveDataEventListener(builder)
    var query = query
        private set

    override fun onActive() {
        super.onActive()
        query?.addValueEventListener(listener)
    }

    override fun onInactive() {
        super.onInactive()
        query?.removeEventListener(listener)
    }

    fun updateQuery(newQuery: Query) {
        if (query == newQuery) return
        query?.removeEventListener(listener)

        query = newQuery
        if (hasActiveObservers()) {
            query?.addValueEventListener(listener)
        }
    }

    private inner class FirebaseLiveDataEventListener(val builder: DataBuilder<T>) : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val t = builder.buildData(snapshot)
            this@FirebaseLiveData.postValue(t)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w("FIREBASE_LIVE_DATA", "load:onCancelled", error.toException())
        }
    }

    interface DataBuilder<T> {
        fun buildData(dataSnapshot: DataSnapshot): T
    }
}
