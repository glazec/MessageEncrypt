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
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.text_selection.*
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStore.ProtectionParameter
import java.security.PrivateKey
import javax.crypto.Cipher


class TextSelection : AppCompatActivity() {

    lateinit var rawme: String
    val datas: MutableList<String> = mutableListOf()
    private var mList: MutableList<String> = mutableListOf()
    private var keyAlias: String = "ull"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.text_selection)
        setSupportActionBar(toolbar)
        initList()

        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)

        for (i in ks.aliases()) {
            datas.add(i)
        }


        var text: CharSequence? = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
        val intent = Intent()
        keyAlias = textProcess(text, ks, mList)

        bttn_apply2.setOnClickListener() {

            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.text = decryptMessage(EncryptionContent.text.toString(), ks, keyAlias)
            Toast.makeText(this@TextSelection, "The Decrypted text is in clipboard", Toast.LENGTH_SHORT).show()
        }

        bttn_apply.setOnClickListener() {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.text = encryptMessage(text.toString().toByteArray(), ks, keyAlias)

            EncryptionContent.setText(clipboard.text)
            finish()
            Toast.makeText(this@TextSelection, "The Encrypted text is in clipboard", Toast.LENGTH_SHORT).show()
        }
        intent.putExtra(Intent.EXTRA_PROCESS_TEXT, "paste your encryption data")

        setResult(RESULT_OK, intent)


    }


    fun textProcess(text: CharSequence?, ks: KeyStore, mlist: List<String>): String {

        var ChooseAKey = AlertDialog.Builder(this)

        var contactName = findViewById<EditText>(R.id.ContactName)
        var EncryptionContent = findViewById<TextView>(R.id.EncryptionContent)
        EncryptionContent.setText(text.toString())
        ChooseAKey.setTitle("Choose a key")
        ChooseAKey.setNegativeButton(
            "Confirm"
        ) { dialog, which -> dialog.cancel() }
        ChooseAKey.setSingleChoiceItems(mList.toTypedArray(), -1) { dialogue, which ->
            contactName.setText(mList[which])
            Log.e("edittext text", contactName.text.toString())
            setContact(mList[which])
        }


        ChooseAKey.create().show()

        return keyAlias

    }


    private fun setContact(contact: String) {
        keyAlias = contact
        Log.e("keyAlias", keyAlias)
    }

    fun create_key(alias: String) {
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
        for (i in ks.aliases()) {
            Log.i("android key store", i.toString())
        }
        Log.i("new key entry", ks.getEntry(alias, null).toString())
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
            R.id.action_settings -> {
                true
            }
            R.id.action_permission -> {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun initList() {
        mList = datas
//        for (x in 1..20 step 1) {
////            mList.add("$x")
//    }
//    }
    }
}



