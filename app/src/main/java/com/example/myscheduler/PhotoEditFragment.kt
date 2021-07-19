package com.example.myscheduler

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myscheduler.databinding.FragmentPhotoEditBinding
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.kotlin.where
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [photoEditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class PhotoEditFragment : Fragment() {

    private var _binding: FragmentPhotoEditBinding? = null
    private val binding get() = _binding!!

    private lateinit var realm: Realm


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        realm = Realm.getDefaultInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPhotoEditBinding.inflate(
            inflater, container,
            false
        )
        return binding.root
    }

    private val args: PhotoEditFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (args.photoId != -1L) {
            val photo = realm.where<Photo>()
                .equalTo("id", args.photoId).findFirst()
            if(photo != null){
                val uri = Uri.parse(photo.uri)
                val imageBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                binding.imageView.setImageBitmap(imageBitmap)
            }
        }
        (activity as? MainActivity)?.setFabVisible(View.INVISIBLE)
        binding.deleteButton.setOnClickListener {
            val dialog = ConfirmDialog(
                "削除しますか？",
                "削除", { deletePhoto(it) },
                "キャンセル", {
                    Snackbar.make(it, "キャンセルしました", Snackbar.LENGTH_SHORT)
                        .show()
                })
            dialog.show(parentFragmentManager, "delete_dialog")
        }
    }




    private fun deletePhoto(view: View) {
        realm.executeTransaction { db: Realm ->
            db.where<Photo>().equalTo("id", args.photoId)
                ?.findFirst()
                ?.deleteFromRealm()
        }
        Snackbar.make(view, "削除しました", Snackbar.LENGTH_SHORT)
            .setActionTextColor(Color.YELLOW)
            .show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}