package com.inevitable.pgpkeyboard


import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.text_selection.*
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStore.ProtectionParameter
import javax.crypto.Cipher
import kotlin.math.log


class TextSelection : AppCompatActivity() {

    val datas:MutableList<String> = mutableListOf()
    private var mList: MutableList<String> = mutableListOf()
    private var keyAlias:String="ull"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.text_selection)
        setSupportActionBar(toolbar)
        initList()

        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)

        for(i in ks.aliases()){
            datas.add(i)
        }


        var text:CharSequence? = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
        val intent = Intent()
            keyAlias=textProcess(text,ks,mList)
            bttn_apply.setOnClickListener(){
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.text = encryptMessage(text.toString().toByteArray(),ks,keyAlias).toString()
            }
            intent.putExtra(Intent.EXTRA_PROCESS_TEXT, "paste your encryption data")

        setResult(RESULT_OK, intent)


    }


    fun textProcess(text:CharSequence?, ks:KeyStore, mlist:List<String>):String{

        var ChooseAKey = AlertDialog.Builder(this)

        var contactName=findViewById<EditText>(R.id.ContactName)
        var EncryptionContent = findViewById<TextView>(R.id.EncryptionContent)
        EncryptionContent.setText(text.toString())
        ChooseAKey.setTitle("Choose a key")
        ChooseAKey.setNegativeButton(
            "I know it"
        ) { dialog, which -> dialog.cancel() }
        ChooseAKey.setSingleChoiceItems(mList.toTypedArray(),-1) { dialogue, which ->
            contactName.setText(mList[which])
            Log.e("edittext text",contactName.text.toString())
            setContact(mList[which])
        }


        ChooseAKey.create().show()

        return keyAlias

    }


    private fun setContact(contact:String){
        keyAlias=contact
        Log.e("keyAlias",keyAlias)
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

