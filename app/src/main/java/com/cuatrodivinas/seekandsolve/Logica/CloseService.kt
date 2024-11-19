package com.cuatrodivinas.seekandsolve.Logica

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_ACTIVOS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.auth
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.database

class CloseService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        
        val databaseRef = database.getReference(PATH_ACTIVOS).child(auth.currentUser!!.uid)
        databaseRef.removeValue()
        stopSelf()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        val databaseRef = database.getReference(PATH_ACTIVOS).child(auth.currentUser!!.uid)
        databaseRef.removeValue()

        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
