package com.example.myscheduler

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myscheduler.databinding.FragmentScheduleEditBinding
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ScheduleEditFragment : Fragment() {

    private var _binding: FragmentScheduleEditBinding? = null
    private val binding get() = _binding!!

    private lateinit var realm: Realm

    val REQUEST_PICTURE = 1
    val REQUEST_GALLERY = 2
    val REQUEST_EXTERNAL_STORAGE = 3
    lateinit var currentPhotoUri: Uri

    var currentPhotoUriList: MutableList<Uri> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        realm = Realm.getDefaultInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScheduleEditBinding.inflate(
            inflater, container,
            false
        )
        return binding.root
    }

    private val args: ScheduleEditFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (args.scheduleId != -1L) {
            val schedule = realm.where<Schedule>()
                .equalTo("id", args.scheduleId).findFirst()
            binding.dateEdit.setText(
                DateFormat.format(
                    "yyyy/MM/dd",
                    schedule?.date
                )
            )
            binding.timeEdit.setText(
                DateFormat.format(
                    "HH:mm",
                    schedule?.date
                )
            )
            binding.titleEdit.setText(schedule?.title)
            binding.detailEdit.setText(schedule?.detail)
            binding.delete.visibility = View.VISIBLE

            if(schedule?.photoList != null){
                binding.photoList.layoutManager = LinearLayoutManager(context)
                val adapter = PhotoAdapter(schedule.photoList)
                binding.photoList.adapter = adapter
                adapter.setOnItemClickListener { id ->
                    id?.let {
                        val action =
                            ScheduleEditFragmentDirections.actionScheduleEditFragmentToPhotoEditFragment(it)
                        findNavController().navigate(action)
                    }
                }
            }
        } else {
            binding.delete.visibility = View.INVISIBLE
        }
        (activity as? MainActivity)?.setFabVisible(View.INVISIBLE)
        binding.save.setOnClickListener {
            val dialog = ConfirmDialog(
                "保存しますか？",
                "保存", { saveSchedule(it) },
                "キャンセル", {
                    Snackbar.make(it, "キャンセルしました", Snackbar.LENGTH_SHORT)
                        .show()
                })
            dialog.show(parentFragmentManager, "save_dialog")
        }
        binding.delete.setOnClickListener {
            val dialog = ConfirmDialog(
                "削除しますか？",
                "削除", { deleteSchedule(it) },
                "キャンセル", {
                    Snackbar.make(it, "キャンセルしました", Snackbar.LENGTH_SHORT)
                        .show()
                })
            dialog.show(parentFragmentManager, "delete_dialog")
        }

        binding.dateButton.setOnClickListener {
            DateDialog { date ->
                binding.dateEdit.setText(date)
            }.show(parentFragmentManager, "date_dialog")
        }

        binding.timeButton.setOnClickListener {
            TimeDialog { time ->
                binding.timeEdit.setText(time)
            }.show(parentFragmentManager, "time_dialog")
        }


        binding.cameraButton.setOnClickListener {
            takePicture()
        }

        binding.galleryButton.setOnClickListener {
            gallery()
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            storagePermission()
        }
    }

    private fun takePicture() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(requireActivity().packageManager)?.also {
                val time: String = SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(Date())
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "${time}_.jpg")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                }
                val collection = MediaStore.Images.Media
                    .getContentUri("external")
                val photoUri = requireActivity().contentResolver.insert(collection, values)
                photoUri?.let {
                    currentPhotoUri = it
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, REQUEST_PICTURE)
            }
        }
    }

    private fun gallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int, data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICTURE) {
            when (resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    currentPhotoUriList.add(currentPhotoUri)
                }
                else -> {
                    requireActivity().contentResolver.delete(currentPhotoUri, null, null)
                }
            }
        }else if(requestCode == REQUEST_GALLERY && resultCode == AppCompatActivity.RESULT_OK){
            if (data != null) {
                val uri = data.data;
                if (uri != null) {
                    currentPhotoUriList.add(uri)
                }
            }
        }
    }

    private fun storagePermission() {
        val permission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_EXTERNAL_STORAGE
            )
        }
    }

    private fun saveSchedule(view: View) {
        when (args.scheduleId) {
            -1L -> {
                realm.executeTransaction { db: Realm ->
                    val maxId = db.where<Schedule>().max("id")
                    val nextId = (maxId?.toLong() ?: 0L) + 1L
                    val schedule = db.createObject<Schedule>(nextId)
                    val date =
                        "${binding.dateEdit.text} ${binding.timeEdit.text}"
                            .toDate()
                    if (date != null) schedule.date = date
                    schedule.title = binding.titleEdit.text.toString()
                    schedule.detail = binding.detailEdit.text.toString()

                    val maxIdPhoto = db.where<Photo>().max("id")
                    val nextIdPhoto = (maxIdPhoto?.toLong() ?: 0L) + 1L
                    for(i in currentPhotoUriList.indices){
                        val photo = db.createObject<Photo>(i+nextIdPhoto)
                        photo.uri = currentPhotoUriList[i].toString()
                        schedule.photoList.add(photo)
                    }
                }
                Snackbar.make(view, "追加しました", Snackbar.LENGTH_SHORT)
                    .setAction("戻る") { findNavController().popBackStack() }
                    .setActionTextColor(Color.YELLOW)
                    .show()
            }
            else -> {
                realm.executeTransaction { db: Realm ->
                    val schedule = db.where<Schedule>()
                        .equalTo("id", args.scheduleId).findFirst()
                    val date = ("${binding.dateEdit.text} " +
                            "${binding.timeEdit.text}").toDate()
                    if (date != null) schedule?.date = date
                    schedule?.title = binding.titleEdit.text.toString()
                    schedule?.detail = binding.detailEdit.text.toString()

                    val maxIdPhoto = db.where<Photo>().max("id")
                    val nextIdPhoto = (maxIdPhoto?.toLong() ?: 0L) + 1L
                    if(schedule?.photoList != null){
                        for(i in currentPhotoUriList.indices){
                            val photo = db.createObject<Photo>(i+nextIdPhoto)
                            photo.uri = currentPhotoUriList[i].toString()
                            schedule.photoList.add(photo)
                        }
                    }
                }
                Snackbar.make(view, "修正しました", Snackbar.LENGTH_SHORT)
                    .setAction("戻る") { findNavController().popBackStack() }
                    .setActionTextColor(Color.YELLOW)
                    .show()
            }
        }
    }

    private fun deleteSchedule(view: View) {
        realm.executeTransaction { db: Realm ->
            db.where<Schedule>().equalTo("id", args.scheduleId)
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

    private fun String.toDate(pattern: String = "yyyy/MM/dd HH:mm"): Date? {
        return try {
            SimpleDateFormat(pattern).parse(this)
        } catch (e: IllegalArgumentException) {
            return null
        } catch (e: ParseException) {
            return null
        }
    }
}


