package pe.edu.idat.clinicasanmiguel.utils

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import pe.edu.idat.clinicasanmiguel.R

class LoadingDialogFragment : DialogFragment() {

    private var mensajeActual: String =
        MENSAJE_PREDETERMINADO

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        isCancelable = false

        mensajeActual =
            arguments
                ?.getString(ARG_MENSAJE)
                .orEmpty()
                .ifBlank {
                    MENSAJE_PREDETERMINADO
                }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.dialog_cargando,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        view.findViewById<TextView>(
            R.id.tvMensajeCarga
        ).text = mensajeActual
    }

    override fun onStart() {
        super.onStart()

        dialog?.setCanceledOnTouchOutside(false)

        dialog?.window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )
    }

    private fun actualizarMensaje(
        mensaje: String
    ) {
        mensajeActual =
            mensaje.ifBlank {
                MENSAJE_PREDETERMINADO
            }

        view?.findViewById<TextView>(
            R.id.tvMensajeCarga
        )?.text = mensajeActual
    }

    companion object {

        private const val TAG =
            "LoadingDialogFragment"

        private const val ARG_MENSAJE =
            "ARG_MENSAJE"

        private const val MENSAJE_PREDETERMINADO =
            "Cargando..."

        fun mostrar(
            fragmentManager: FragmentManager,
            mensaje: String = MENSAJE_PREDETERMINADO
        ) {
            val modalExistente =
                fragmentManager.findFragmentByTag(
                    TAG
                ) as? LoadingDialogFragment

            if (modalExistente != null) {
                modalExistente.actualizarMensaje(
                    mensaje
                )

                return
            }

            if (fragmentManager.isStateSaved) {
                return
            }

            LoadingDialogFragment().apply {
                arguments =
                    Bundle().apply {
                        putString(
                            ARG_MENSAJE,
                            mensaje
                        )
                    }
            }.show(
                fragmentManager,
                TAG
            )
        }

        fun ocultar(
            fragmentManager: FragmentManager
        ) {
            val modal =
                fragmentManager.findFragmentByTag(
                    TAG
                ) as? LoadingDialogFragment
                    ?: return

            modal.dismissAllowingStateLoss()
        }
    }
}