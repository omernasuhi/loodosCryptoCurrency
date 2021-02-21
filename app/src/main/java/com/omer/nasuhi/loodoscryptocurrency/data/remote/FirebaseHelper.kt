package com.omer.nasuhi.loodoscryptocurrency.data.remote

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class FirebaseHelper @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore
) {

    suspend fun signInFirebase(): Task<AuthResult> {
        return firebaseAuth.signInWithEmailAndPassword(
            "omernsh143@gmail.com",
            "123456"
        )
    }

    suspend fun favoriteCoinsCollection(): CollectionReference {
        return firebaseFirestore.collection("users").document("oSI2rmiUp6Om2QelevYNbMQJzBw1")
            .collection("favoriteCoins")
    }
}