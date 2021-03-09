package one.hix.qqqqqqqqqq

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.*


class MainActivity : AppCompatActivity() {

    val storageRef = Firebase.storage.reference

    companion object {
        private var sheetsService: Sheets? = null
        private const val APPLICATION_NAME = "Google Sheets Example"
        private const val SHEET_ID = "1QBWHPZx185WF5MJp5Enk_s8q8iNvlJAFv-BgIxbtwzQ"
        private const val PICK_IMAGE_REQUEST = 1
        var count = 0
    }

    private var mButtonChooseImage: Button? = null
    private var mButtonUpload: Button? = null
    private var mEditTextFileName: EditText? = null
    private var mImageView: ImageView? = null
    private var mImageUri: Uri? = null
    private var fireStorageUri: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        mButtonChooseImage = findViewById(R.id.button_choose_image)
        mButtonUpload = findViewById(R.id.button_upload)
        mEditTextFileName = findViewById(R.id.edit_text_file_name)
        mImageView = findViewById(R.id.image_view)


        mButtonChooseImage?.setOnClickListener {
            openFileChooser()
        }
        mButtonUpload?.setOnClickListener {
            uploadFile()
        }
//        mButtonShow?.setOnClickListener {
//            val storageReference = Firebase.storage.reference
//
//            Glide.with(this /* context */)
//                .load(storageReference)
//                .into(mImageView!!)
//
//
//
//            val ref = storageReference.child("1612543899986.jpg")
//            val localFile = File.createTempFile("images", "jpg")
//
//            ref.getFile(localFile).addOnSuccessListener {
//                // Local temp file has been created
//            }.addOnFailureListener {
//                // Handle any errors
//            }
//        }
//
//        val user: MutableMap<String, Any> = HashMap()
//        user["first"] = "Ada"
//        user["last"] = "Lovelace"
//        user["born"] = 1815
// Add a new document with a generated ID
//        firebaseFirestore.document()
//            .addOnSuccessListener { documentReference ->
//                println("3ss  DocumentSnapshot added with ID: ${documentReference.id}")
//            }
//            .addOnFailureListener { e ->
//                println("3ss  Error adding document, $e")
//            }
//
//        firebaseFirestore.collection("user1")
//            .get()
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    for (document in task.result!!) {
//                        println("3ss ${document.id} =>  ${document.data}")
//                    }
//                } else {
//                    println("3ss   Error getting documents.${task.exception}")
//                }
//            }

    }


    private fun uploadFile() {
        if (mImageUri != null) {
            val filename = Calendar.getInstance().time.time
            val ref = storageRef.child("images").child(filename.toString())
            ref.putFile(mImageUri!!).addOnSuccessListener { taskSnapshot ->
                ref.downloadUrl.addOnSuccessListener {
                    Toast.makeText(this@MainActivity, "Загрузка успешна", Toast.LENGTH_SHORT).show()
                    GlobalScope.launch(Dispatchers.IO) {
                        update2(it.toString())
                    }
                }
            }.addOnFailureListener {

            }
        }
    }

    fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            mImageUri = data.data
            Picasso.get().load(mImageUri).into(mImageView)
        }
    }

    private fun getFileExtension(uri: Uri): String {
        val cR = contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))!!
    }


    fun update2(url :String) {
        ++count
        ++count
        val body =
            ValueRange().setValues(Arrays.asList(Arrays.asList<Any>("=IMAGE(\"${url}\"; 4; 55; 55)")))
        getSheetsService().spreadsheets().values()
            .update(SHEET_ID, "C${count}", body)
            .setValueInputOption("USER_ENTERED")
            .execute()
    }


    fun delete() {
        sheetsService = getSheetsService()
        val deleteRequest = DeleteDimensionRequest()
            .setRange(
                DimensionRange()
                    .setSheetId(11111)
                    .setDimension("ROWS")
                    .setStartIndex(111)
            )
        val request = arrayListOf<Request>().apply { add(Request().setDeleteDimension(deleteRequest)) }
        val body = BatchUpdateSpreadsheetRequest().setRequests(request)
        sheetsService!!.spreadsheets().batchUpdate(SHEET_ID, body).execute()
    }


    fun update() {
        sheetsService = getSheetsService()
        val body = ValueRange().setValues(Arrays.asList(Arrays.asList<Any>("updated")))
        val upateResponse = sheetsService!!.spreadsheets().values()
            .update(SHEET_ID, "C5", body)
            .setValueInputOption("RAW")
            .execute()
    }

    fun append() {
        sheetsService = getSheetsService()
        val appendBody = ValueRange().setValues(Arrays.asList(Arrays.asList<Any>("From", "From", "From", "From", "From", "From", "From", "From", "From", "From")))
        val appendResult = sheetsService!!.spreadsheets().values()
            .append(SHEET_ID, "list1", appendBody)
            .setValueInputOption("USER_ENTERED")
            .setInsertDataOption("INSERT_ROWS")
            .setIncludeValuesInResponse(true)
            .execute()
    }

    fun getInfo() {
        sheetsService = getSheetsService()
        val range = "list1!A1:B2"
        val response = sheetsService!!.spreadsheets().values()[SHEET_ID, range]
            .execute()
        val values = response.getValues()
        if (values != null) {
            if (values.isEmpty()) {
            }
        } else {
            (values as MutableList).forEach {
            }
        }
    }


    private fun autorize(): Credential {
        val `in` = this::class.java.getResourceAsStream("/credentials.json")
        val clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), InputStreamReader(`in`))
        val scopes = Arrays.asList(SheetsScopes.SPREADSHEETS)
        val flow = GoogleAuthorizationCodeFlow.Builder(NetHttpTransport(), JacksonFactory.getDefaultInstance(), clientSecrets, scopes)
            .setDataStoreFactory(FileDataStoreFactory(File(Environment.getExternalStorageDirectory().toString())))
            .setAccessType("offline")
            .build()
        val ab = object : AuthorizationCodeInstalledApp(flow, LocalServerReceiver()) {
            @Throws(IOException::class)
            override fun onAuthorization(authorizationUrl: AuthorizationCodeRequestUrl) {
                val url = authorizationUrl.build()
                println("sss url  ${url}")
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            }
        }.authorize("user")
        return ab
    }


    fun getSheetsService(): Sheets {
        val credential = autorize()
        return Sheets.Builder(NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
            .setApplicationName(APPLICATION_NAME)
            .build()
    }


}