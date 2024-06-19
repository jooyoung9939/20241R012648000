package closet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.lookatme.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PhotoBottomSheetDialogFragment : BottomSheetDialogFragment() {
    interface OnPhotoOptionClickListener {
        fun onCameraOptionClicked()
        fun onGalleryOptionClicked()
    }

    private lateinit var callback: OnPhotoOptionClickListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_photo_bottom_sheet, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        return BottomSheetDialog(requireContext(), R.style.TransparentBottomSheetDialog)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val buttonCamera: Button? = view?.findViewById(R.id.button_camera)
        val buttonGallery: Button? = view?.findViewById(R.id.button_gallery)
        val buttonCancel: Button? = view?.findViewById(R.id.button_cancel)

        buttonCamera?.setOnClickListener {
            callback.onCameraOptionClicked()
            dismiss()
        }

        buttonGallery?.setOnClickListener {
            callback.onGalleryOptionClicked()
            dismiss()
        }

        buttonCancel?.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        parentFragmentManager.setFragmentResult("bottomSheetDismiss", Bundle.EMPTY)
    }

    fun setOnPhotoOptionClickListener(callback: OnPhotoOptionClickListener) {
        this.callback = callback
    }
}