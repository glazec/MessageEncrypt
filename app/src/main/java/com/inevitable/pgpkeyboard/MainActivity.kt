package com.inevitable.pgpkeyboard


import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStore.ProtectionParameter
import javax.crypto.Cipher


class MainActivity : AppCompatActivity() {

    val datas:MutableList<String> = mutableListOf()
    private var mList: MutableList<String> = mutableListOf()
    lateinit var adapter: ListItemKeypairAdapter


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        initList()


        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)

        for(i in ks.aliases()){
            datas.add(i)
        }

        val listView = findViewById<ListView>(R.id.listView)

        //list view show keystore alias
//        val listView: ListView = findViewById(R.id.lv)
        adapter = ListItemKeypairAdapter(this@MainActivity, datas)
//        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, datas)
        listView.adapter = adapter
        listView.setOnItemClickListener { adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->
            var keyPairBuilder = AlertDialog.Builder(this)
            keyPairBuilder.setTitle("Key Info")
            keyPairBuilder.setMessage(ks.getEntry(datas[i], null).toString())
            keyPairBuilder.setNegativeButton(
                "I know it"
            ) { dialog, which -> dialog.cancel() }
            keyPairBuilder.show()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.text = ks.getCertificate(datas[i]).publicKey.toString()
            Toast.makeText(this@MainActivity, "Copy the entry to the clipboard", Toast.LENGTH_SHORT).show();


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
                for (i in ks.aliases()) Log.e("keystore alisa", i.toString())
                datas.removeAt(i)
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
        Log.e("test", "test".toByteArray().toString())
        var a = encryptMessage("test".toByteArray(), ks, "testkey")
        decryptMessage(a, ks, "testkey")

//        var btn_openSetting = findViewById<Button>(R.id.btn_openSetting)
//        var btn_openFloatingBall = findViewById<Button>(R.id.btn_openFloatingBall)

//        btn_openSetting.setOnClickListener{
//
//            fun onClick(v:View ) {
//                //打开设置  打开服务才能实现返回功能
//                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
//            }
//        }
//
//        btn_openFloatingBall.setOnClickListener{
//            fun onClick(v:View ) {
                ViewManager(this@MainActivity).showFloatBall()
//                ViewManager.getInstance(this@MainActivity).showFloatBall();
//            }
//    }
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
        new_key.generateKeyPair()
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)
//        testOutput.setText(ks.getEntry(alias,null).toString())
        for(i in ks.aliases()){
            Log.i("android key store",i.toString())
        }
        Log.i("new key entry",ks.getEntry(alias,null).toString())
    }

    fun encryptMessage(plaintext: ByteArray, ks: KeyStore, alias: String): ByteArray {
        var protParam: ProtectionParameter = KeyStore.PasswordProtection(null);
        val pkEntry = ks.getEntry(alias, protParam) as KeyStore.PrivateKeyEntry
        val keyPrivate = pkEntry.privateKey

        Log.e("alias", alias)
        var keyPublic = ks.getCertificate(alias).publicKey
        val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keyPublic)
        val ciphertext = cipher.doFinal(plaintext)
        Log.e("cipher data", ciphertext.toString())
        return ciphertext
    }

    fun decryptMessage(ciphertext: ByteArray, ks: KeyStore, alias: String): String {
        var keyPublic = ks.getCertificate(alias).publicKey
        var protParam: ProtectionParameter = KeyStore.PasswordProtection(null);
        val pkEntry = ks.getEntry(alias, protParam) as KeyStore.PrivateKeyEntry
        val keyPrivate = pkEntry.privateKey


//        var alias="testKey"
//        var keyPrivate=ks.getKey(alias, null)
        val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, keyPrivate)
        val plaintext = cipher.doFinal(ciphertext)
        Log.e("plain text", String(plaintext))
        return String(plaintext)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun initList() {
        mList = datas
//        for (x in 1..20 step 1) {
//            mList.add("$x")
//    }
        }
    }

