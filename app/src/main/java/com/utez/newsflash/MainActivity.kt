package com.utez.newsflash

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.utez.newsflash.databinding.ActivityMainBinding
import com.utez.newsflash.worker.NewsWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Manejador del permiso de notificaciones para Android 13+
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Permiso de notificaciones concedido.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Las alertas no podrán mostrarse sin permiso.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Solicitar permisos de notificación si se ejecuta en Android 13 o superior
        checkAndRequestPermissions()

        // 2. Configurar eventos de los botones
        binding.btnScheduleSync.setOnClickListener {
            setupPeriodicWork()
        }

        binding.btnForceImmediateSync.setOnClickListener {
            triggerImmediateWork()
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    /**
     * Programa la tarea periódica en segundo plano respetando las políticas de ahorro de energía.
     */
    @SuppressLint("SetTextI18n")
    private fun setupPeriodicWork() {
        // Restricciones: Sólo ejecutar si el dispositivo cuenta con conexión a Internet activa
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Intervalo mínimo permitido por el sistema Android para tareas periódicas es de 15 minutos
        val periodicWorkRequest = PeriodicWorkRequestBuilder<NewsWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        // Registrar la tarea en WorkManager con política de reemplazo o conservación
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "NewsFlashPeriodicSync",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )

        binding.tvWorkerStatus.text = "Monitoreo activo: Verificando noticias cada 15 min (Requiere red)."
        Toast.makeText(this, "Servicio periódico programado exitosamente.", Toast.LENGTH_SHORT).show()
    }

    /**
     * Dispara una tarea única e inmediata para pruebas de laboratorio del docente o evaluador.
     */
    private fun triggerImmediateWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeRequest = OneTimeWorkRequestBuilder<NewsWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueue(oneTimeRequest)

        // Observar en tiempo real el ciclo de vida de la tarea inmediata en la UI
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(oneTimeRequest.id)
            .observe(this) { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.ENQUEUED -> binding.tvWorkerStatus.text = "Tarea en cola..."
                        WorkInfo.State.RUNNING -> binding.tvWorkerStatus.text = "Consultando noticias en la nube..."
                        WorkInfo.State.SUCCEEDED -> binding.tvWorkerStatus.text = "Sincronización completada con éxito."
                        WorkInfo.State.FAILED -> binding.tvWorkerStatus.text = "Error en la sincronización."
                        else -> {}
                    }
                }
            }
    }
}
