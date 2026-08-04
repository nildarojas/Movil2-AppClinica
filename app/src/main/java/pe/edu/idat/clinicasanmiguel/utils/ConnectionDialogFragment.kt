package pe.edu.idat.clinicasanmiguel.utils

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ConnectionDialogFragment :
    DialogFragment() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        isCancelable =
            false
    }

    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {

        val titulo =
            arguments
                ?.getString(
                    ARG_TITULO
                )
                .orEmpty()
                .ifBlank {
                    "Sin conexión a Internet"
                }

        val mensaje =
            arguments
                ?.getString(
                    ARG_MENSAJE
                )
                .orEmpty()
                .ifBlank {
                    "Esta función necesita conexión a Internet."
                }

        val permitirReintento =
            arguments
                ?.getBoolean(
                    ARG_PERMITIR_REINTENTO,
                    true
                ) ?: true

        val requestKey =
            arguments
                ?.getString(
                    ARG_REQUEST_KEY
                )
                .orEmpty()

        val constructor =
            MaterialAlertDialogBuilder(
                requireContext()
            )
                .setTitle(
                    titulo
                )
                .setMessage(
                    mensaje
                )
                .setCancelable(
                    false
                )
                .setNegativeButton(
                    "ENTENDIDO"
                ) { dialog, _ ->
                    dialog.dismiss()
                }

        if (permitirReintento) {
            constructor.setPositiveButton(
                "REINTENTAR"
            ) { dialog, _ ->

                dialog.dismiss()

                if (requestKey.isNotBlank()) {
                    parentFragmentManager
                        .setFragmentResult(
                            requestKey,
                            bundleOf(
                                RESULTADO_REINTENTAR to true
                            )
                        )
                }
            }
        }

        return constructor.create()
    }

    companion object {

        const val RESULTADO_REINTENTAR =
            "RESULTADO_REINTENTAR"

        private const val TAG =
            "ConnectionDialogFragment"

        private const val ARG_TITULO =
            "ARG_TITULO"

        private const val ARG_MENSAJE =
            "ARG_MENSAJE"

        private const val ARG_PERMITIR_REINTENTO =
            "ARG_PERMITIR_REINTENTO"

        private const val ARG_REQUEST_KEY =
            "ARG_REQUEST_KEY"

        fun mostrar(
            fragmentManager: FragmentManager,
            mensaje: String,
            requestKey: String = "",
            titulo: String =
                "Sin conexión a Internet",
            permitirReintento: Boolean =
                true
        ) {
            if (fragmentManager.isStateSaved) {
                return
            }

            val dialogoExistente =
                fragmentManager
                    .findFragmentByTag(
                        TAG
                    )

            if (dialogoExistente != null) {
                return
            }

            ConnectionDialogFragment()
                .apply {
                    arguments =
                        Bundle().apply {

                            putString(
                                ARG_TITULO,
                                titulo
                            )

                            putString(
                                ARG_MENSAJE,
                                mensaje
                            )

                            putBoolean(
                                ARG_PERMITIR_REINTENTO,
                                permitirReintento
                            )

                            putString(
                                ARG_REQUEST_KEY,
                                requestKey
                            )
                        }
                }
                .show(
                    fragmentManager,
                    TAG
                )
        }

        fun ocultar(
            fragmentManager: FragmentManager
        ) {
            val dialogo =
                fragmentManager
                    .findFragmentByTag(
                        TAG
                    ) as? ConnectionDialogFragment
                    ?: return

            dialogo.dismissAllowingStateLoss()
        }
    }
}