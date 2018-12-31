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
import java.security.*
import java.security.KeyStore.ProtectionParameter
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher

// datas for the key in keystore
//mlist for the key in the database
class MainActivity : AppCompatActivity() {

    lateinit var publicmoduls: BigInteger
    lateinit var publicExponent: BigInteger
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
                    "I know it"
                ) { dialog, which -> dialog.cancel() }
                keyPairBuilder.show()
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.text = info
                Toast.makeText(this@MainActivity, "Copy the entry to the clipboard", Toast.LENGTH_SHORT).show()
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
                    "I know it"
                ) { dialog, which -> dialog.cancel() }
                keyPairBuilder.show()
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.text = info
                Toast.makeText(this@MainActivity, "Copy the entry to the clipboard", Toast.LENGTH_SHORT).show()
            }
//            Log.e("process",ks.getEntry(datas[i], null).toString())

//            Toast.makeText(this@MainActivity, ks.getEntry(datas[i],null).toString(), Toast.LENGTH_LONG).show()
        }
//        listView.setOnItemClickListener(AdapterView.OnItemClickListener() {
//            override fun onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Toast.makeText(MainActivity.this, "Click item" + i, Toast.LENGTH_SHORT).show();
//            }
//        })

//        {
//            Toast.makeText(this@MainActivity,"Click item $(i:Int)",Toast.LENGTH_SHORT).show()
//        }

        adapter.setOnItemDeleteClickListener(object : ListItemKeypairAdapter.OnItemDeleteListener {
            override fun onDeleteClick(i: Int) {
                ks.deleteEntry(datas[i])
                deleteKey(datas[i])
                for (i in ks.aliases()) Log.e("keystore alisa", i.toString())
                datas.removeAt(i)
                mList.removeAt(i)
//                initList()
                adapter.notifyDataSetChanged()
            }
        })

//        adapter.setOnItemDeleteClickListener(fun() {
//            //            mList.removeAt(i)
//            adapter.notifyDataSetChanged()
//        })

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
                .setBlockModes(KeyProperties.BLOCK_MODE_CTR)
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

    fun encryptMessage(plaintext: ByteArray, ks: KeyStore, alias: String): String {
//        var protParam: ProtectionParameter = KeyStore.PasswordProtection(null);
//        val pkEntry = ks.getEntry(alias, protParam) as KeyStore.PrivateKeyEntry

        Log.e("alias", alias)
        var keyPublic = ks.getCertificate(alias).publicKey
        val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keyPublic)
        val ciphertext = cipher.doFinal(plaintext)
        Log.e("cipher data", ciphertext.toString())
        return Base64.encodeToString(ciphertext, Base64.DEFAULT)

    }

    fun decryptMessage(ciphertext: String, ks: KeyStore, alias: String): String {
        var keyPublic = ks.getCertificate(alias).publicKey
        var protParam: ProtectionParameter = KeyStore.PasswordProtection(null)
//        val pkEntry = ks.getEntry(alias, protParam) as KeyStore.PrivateKeyEntry
//        val keyPrivate = pkEntry.privateKey
        var t = ks.getKey(alias, null) as PrivateKey?

//        var alias="testKey"
//        var keyPrivate=ks.getKey(alias, null)
        val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, t)
        val plaintext = cipher.doFinal(Base64.decode(ciphertext, Base64.DEFAULT))
        Log.e("plain text", String(plaintext))
        return String(plaintext)
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
                builder.setTitle("Exponent")
                var input = EditText(this)
                input.setRawInputType(InputType.TYPE_CLASS_TEXT)
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

//                var builder2=AlertDialog.Builder(this)
//                var input2 = EditText(this)
//                input2.setRawInputType(InputType.TYPE_CLASS_TEXT)
//                builder2.setView(input)
//                builder2.setTitle("modulus")
//                builder2.setPositiveButton(
//                    "OK"
//                ) { dialog, which -> setModuluss(input.text.toString().toBigInteger());importKey();}
//                builder2.setNegativeButton(
//                    "Cancel"
//                ) { dialog, which -> dialog.cancel() }
//                builder2.show();
                true


            }
            R.id.action_permission -> {
                startActivity(Intent(this, TextSelection::class.java));return true

            }
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
//        mList = datas
//        for (x in 1..20 step 1) {
//            mList.add("$x")
//    }
    }

    private fun setExponent(expo: BigInteger) {
        publicExponent = expo
    }

    private fun setModuls(mod: BigInteger) {
        publicmoduls = mod
    }

    private fun deleteAllinKeyStore(ks: KeyStore) {
        for (i in ks.aliases()) {
            ks.deleteEntry(i)
        }
    }
    }

