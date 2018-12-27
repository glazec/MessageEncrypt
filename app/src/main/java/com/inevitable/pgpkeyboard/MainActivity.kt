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
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStore.ProtectionParameter
import java.security.PrivateKey
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
        decryptMessage(encryptMessage("test".toByteArray(), ks, "uu"), ks, "uu")



//        var textSelectionActionModeCallback = object:ActionMode.Callback2(){
//
//            override fun onCreateActionMode(actionMode:ActionMode , menu:Menu ): Boolean {
//                 var menuInflater:MenuInflater = actionMode.getMenuInflater()
//                menuInflater.inflate(R.menu.selection_action_menu,menu);
//                return true;//返回false则不会显示弹窗
//            }
//
//            override fun onPrepareActionMode(actionMode:ActionMode , menu:Menu ):Boolean {
//                return false;
//            }
//
//
//            override fun onActionItemClicked(actionMode:ActionMode , menuItem:MenuItem):Boolean {
//                //根据item的ID处理点击事件
//                when (menuItem.getItemId()) {
//                    R.id.Informal22 -> {
//                        Toast.makeText(this@MainActivity, "点击的是22", Toast.LENGTH_SHORT).show();
//                        actionMode.finish();//收起操作菜单
////                        break;
//                    }
//                    R.id.Informal33 -> {
//
//                    Toast.makeText(this@MainActivity, "点击的是33", Toast.LENGTH_SHORT).show();
//                        actionMode.finish();
////                    break;
//                }
//                }
//                return false;//返回true则系统的"复制"、"搜索"之类的item将无效，只有自定义item有响应
//            }
//
//
//            override public fun onDestroyActionMode(actionMode:ActionMode ) {
//
//            }
//
//            override fun onGetContentRect(mode:ActionMode , view:View , outRect:Rect) {
//                //可选  用于改变弹出菜单的位置
//                super.onGetContentRect(mode, view, outRect);
//            }
//        };

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
        var protParam: ProtectionParameter = KeyStore.PasswordProtection(null);
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
            R.id.action_settings -> true
            R.id.action_permission -> {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));return true
            }
            R.id.action_show_flow_ball -> {
//                ViewManager(this@MainActivity).getInstance(this@MainActivity).showFloatBall();

//                    var viewmanager = ViewManager(this@MainActivity)
//                    viewmanager.showFloatBall()
//                    viewmanager.getInstance(this@MainActivity).showFloatBall()
                return true
            }
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

