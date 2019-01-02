package com.inevitable.pgpkeyboard


import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PublicKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher

// datas for the key in keystore
//mlist for the key in the database
class MainActivity : AppCompatActivity() {

    val datas:MutableList<String> = mutableListOf()
    private var mList: MutableList<String> = mutableListOf()
    lateinit var adapter: ListItemKeypairAdapter
    val ks = KeyStore.getInstance("AndroidKeyStore")


    override fun onCreate(savedInstanceState: Bundle?) {
        ks.load(null)
//        deleteDatabase("EM.db")
//        deleteAllinKeyStore(ks)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        initList()





        for(i in ks.aliases()){
            datas.add(i)
        }

        val listView = findViewById<ListView>(R.id.listView)

        //list view show keystore alias
//        val listView: ListView = findViewById(R.id.lv)

        //change view datas/mList(from database)
        adapter = ListItemKeypairAdapter(this@MainActivity, mList)
//        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, datas)
        listView.adapter = adapter
        listView.setOnItemClickListener { adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->
            try {
                var rsaPub: RSAPublicKey = ks.getCertificate(mList[i]).publicKey as RSAPublicKey
                var modulus: BigInteger = rsaPub.modulus
                var publicExponent: BigInteger = rsaPub.publicExponent
                var keyPairBuilder = AlertDialog.Builder(this)
                var info = "-------Public Key--------\n${Base64.encodeToString(
                    ks.getCertificate(mList[i]).publicKey.encoded,
                    Base64.DEFAULT
                )}\n-------Modules--------\n$modulus\n-------Exponents--------\n$publicExponent"
                keyPairBuilder.setTitle("Key Info")
                keyPairBuilder.setMessage(
                    info
                )
                keyPairBuilder.setNegativeButton(
                    "Delete"
                ) { dialog, which ->
                    run {
                        try {
                            ks.deleteEntry(datas[i])
                            deleteKey(datas[i])
                            for (i in ks.aliases()) Log.e("keystore alisa", i.toString())
                            datas.removeAt(i)
                            mList.removeAt(i)
                            adapter.notifyDataSetChanged()
                        } catch (e: java.lang.Exception) {
                            mList.removeAt(0)
                            adapter.notifyDataSetChanged()
                            deleteDatabase("EM.db")
                            deleteAllinKeyStore(ks)
                        }
                        dialog.cancel()
                    }
                }
                keyPairBuilder.setPositiveButton(
                    "I know it"
                ) { dialog, which ->
                    run {
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.text = info
                        Toast.makeText(this@MainActivity, "Copy the entry to the clipboard", Toast.LENGTH_SHORT).show()
                        dialog.cancel()
                    }
                }
                keyPairBuilder.show()

            } catch (e: Exception) {
                var dbhelper1: SQLiteOpenHelper = DatabaseHelper(this@MainActivity)
                var db1: SQLiteDatabase? = dbhelper1.writableDatabase
                val cursor = db1!!.rawQuery("select * from user where name=?", arrayOf<String>(mList[i]))
                cursor.moveToFirst()
                var endindex: Int = cursor.getString(cursor.getColumnIndex("modulus")).trim().length - 1
                var modulus =
                    cursor.getString(cursor.getColumnIndex("modulus")).trim().substring(1, endindex).toBigInteger()
                var publicExponent: BigInteger =
                    cursor.getString(cursor.getColumnIndex("exponent")).trim().toBigInteger()
                cursor.close()
                db1.close()
                var publickeyimported = generatePublicKeyImported(modulus, publicExponent)
                var info: String = "-------Public Key--------\n${Base64.encodeToString(
                    publickeyimported.encoded,
                    Base64.DEFAULT
                )}\n-------Modules--------\n$modulus\n-------Exponents--------\n$publicExponent"
                //dialogue
                var keyPairBuilder = AlertDialog.Builder(this)
                keyPairBuilder.setTitle("Key Info")
                keyPairBuilder.setMessage(
                    info
                )
                keyPairBuilder.setNegativeButton(
                    "Delete"
                ) { dialog, which ->
                    run {
                        try {
                            ks.deleteEntry(datas[i])
                            deleteKey(datas[i])
                            for (i in ks.aliases()) Log.e("keystore alisa", i.toString())
                            datas.removeAt(i)
                            mList.removeAt(i)
                            adapter.notifyDataSetChanged()
                        } catch (e: java.lang.Exception) {
                            mList.removeAt(0)
                            adapter.notifyDataSetChanged()
                            deleteDatabase("EM.db")
                            deleteAllinKeyStore(ks)

                        }
                        dialog.cancel()
                    }
                }
                keyPairBuilder.setPositiveButton(
                    "I know it"
                ) { dialog, which ->
                    run {
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.text = info
                        Toast.makeText(this@MainActivity, "Copy the entry to the clipboard", Toast.LENGTH_SHORT).show()
                        dialog.cancel()
                    }
                }
                keyPairBuilder.show()
            }
//            Log.e("process",ks.getEntry(datas[i], null).toString())

//            Toast.makeText(this@MainActivity, ks.getEntry(datas[i],null).toString(), Toast.LENGTH_LONG).show()
        }


        button_keypari.setOnClickListener{

          //input dialog
            var builder=AlertDialog.Builder(this)
            builder.setTitle("Key Name")
            var input = EditText(this)
            input.setRawInputType(InputType.TYPE_CLASS_TEXT)
            builder.setView(input)
            builder.setPositiveButton(
                "OK"
            ) { dialog, which -> create_key(input.text.toString())}
            builder.setNegativeButton(
                "Cancel"
            ) { dialog, which -> dialog.cancel() }
            builder.show()
        }
//        Log.e("test", "test".toByteArray().toString())
//        decryptMessage(encryptMessage("test".toByteArray(), ks, "uu"), ks, "uu")

    }


    fun generatePublicKeyImported(modulus: BigInteger, exponent: BigInteger): PublicKey {
        val keySpec = RSAPublicKeySpec(modulus, exponent)
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKey = keyFactory.generatePublic(keySpec)
        Log.e("import", publicKey.toString())
        //test
        val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val ciphertext = cipher.doFinal("tettt".toByteArray())
        Log.e("cipher data", Base64.encodeToString(ciphertext, Base64.DEFAULT))
        return publicKey
    }


    fun create_key(alias:String){
        val new_key = KeyPairGenerator.getInstance(
            "RSA", "AndroidKeyStore"
        )

        new_key.initialize(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setDigests(
                    KeyProperties.DIGEST_SHA256,
                    KeyProperties.DIGEST_SHA512
                )
                .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .setRandomizedEncryptionRequired(true)
                .build()
        )

        datas.add(alias)
        mList.add(alias)
        new_key.generateKeyPair()
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)
//        testOutput.setText(ks.getEntry(alias,null).toString())
        for(i in ks.aliases()){
            Log.i("android key store",i.toString())
        }
        Log.i("new key entry",ks.getEntry(alias,null).toString())
        recordNewKey(alias)

    }

    fun recordNewKey(alias: String) {
        var dbhelper1: SQLiteOpenHelper = DatabaseHelper(this@MainActivity)
        var db1: SQLiteDatabase? = dbhelper1.writableDatabase
        var sql: String = "insert into user (name) values ('$alias')"
        db1!!.execSQL(sql)
        db1.close()
    }

    fun deleteKey(alias: String) {
        var dbhelper1: SQLiteOpenHelper = DatabaseHelper(this@MainActivity)
        var db1: SQLiteDatabase? = dbhelper1.writableDatabase
        var sql = "delete from user where name='$alias'"
        db1!!.execSQL(sql)
        db1.close()
    }

    fun recordImportKey(modulus: String, exponent: String, alias: String) {
        //modulus;exponent;alias
        mList.add(alias)
        var dbhelper1: SQLiteOpenHelper = DatabaseHelper(this@MainActivity)
        var db1: SQLiteDatabase? = dbhelper1.writableDatabase
        var sql: String = "insert into user (name,exponent,modulus) values ('$alias','$exponent','${'a' + modulus}')"
        db1!!.execSQL(sql)
        db1.close()
        adapter.notifyDataSetChanged()
    }





    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
//        return true
//        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                //import key
                var builder = AlertDialog.Builder(this)
                builder.setTitle("Public Key")
                var input = EditText(this)
                input.setRawInputType(InputType.TYPE_CLASS_TEXT)
                input.hint = "Modulus;Exponent;Alias"
                builder.setView(input)
                builder.setPositiveButton(
                    "OK"
                ) { dialog, which ->
                    //                    setExponent(input.text.toString().split(";")[1].trim().toBigInteger())
//                    setModuls(input.text.toString().split(";")[0].trim().toBigInteger())
                    //modulus;exponent;alias
                    recordImportKey(
                        input.text.toString().split(";")[0].trim(),
                        input.text.toString().split(";")[1].trim(),
                        input.text.toString().split(";")[2].trim()
                    )

                }
                builder.setNegativeButton(
                    "Cancel"
                ) { dialog, which -> dialog.cancel() }
                builder.show()

                true


            }
            R.id.action_permission -> {
                startActivity(Intent(this, TextSelection::class.java));return true

            }
//            R.id.action_deleteAll->{
//                deleteDatabase("EM.db")
//                deleteAllinKeyStore(ks)
//                mList= mutableListOf()
//                adapter.notifyDataSetChanged()
//                return true
//            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun initList() {
        var dbhelper1: SQLiteOpenHelper = DatabaseHelper(this@MainActivity)
        var db1: SQLiteDatabase? = dbhelper1.writableDatabase
        val cursor = db1!!.query("user", arrayOf("id", "name"), null, null, null, null, null)
        while (!cursor.isAfterLast) {
            try {
                mList.add(cursor.getString(1))
            } catch (e: Exception) {

            }
            cursor.moveToNext()
        }
        cursor.close()
        db1.close()
    }


    private fun deleteAllinKeyStore(ks: KeyStore) {
        for (i in ks.aliases()) {
            ks.deleteEntry(i)
        }
    }
    }

