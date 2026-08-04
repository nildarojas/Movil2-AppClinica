package pe.edu.idat.clinicasanmiguel.utils

import android.os.SystemClock
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoadingController(
    private val fragmentManager: FragmentManager,
    private val coroutineScope: CoroutineScope,
    private val minimumVisibleTime: Long = MINIMUM_VISIBLE_TIME
) {

    private var loadingStartedAt: Long = 0L
    private var currentRequestToken: Long = 0L
    private var hideJob: Job? = null

    fun show(
        message: String = "Cargando..."
    ): Long {
        hideJob?.cancel()

        currentRequestToken++
        loadingStartedAt = SystemClock.elapsedRealtime()

        LoadingDialogFragment.mostrar(
            fragmentManager = fragmentManager,
            mensaje = message
        )

        return currentRequestToken
    }
    fun hide(
        requestToken: Long,
        onHidden: () -> Unit = {}
    ) {
        if (requestToken != currentRequestToken) {
            return
        }

        hideJob?.cancel()

        val elapsedTime =
            SystemClock.elapsedRealtime() - loadingStartedAt

        val remainingTime =
            (minimumVisibleTime - elapsedTime)
                .coerceAtLeast(0L)

        hideJob = coroutineScope.launch {
            if (remainingTime > 0L) {
                delay(remainingTime)
            }

            if (requestToken != currentRequestToken) {
                return@launch
            }

            LoadingDialogFragment.ocultar(
                fragmentManager
            )

            onHidden()
        }
    }
    fun forceHide() {
        hideJob?.cancel()
        currentRequestToken++

        LoadingDialogFragment.ocultar(
            fragmentManager
        )
    }

    companion object {
        private const val MINIMUM_VISIBLE_TIME = 1_500L
    }
}