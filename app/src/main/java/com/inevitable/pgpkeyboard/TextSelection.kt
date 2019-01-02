package com.inevitable.pgpkeyboard


import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.provider.Settings
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
import java.lang.reflect.InvocationTargetException
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyStore
import java.security.KeyStore.ProtectionParameter
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec
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

        bttn_apply2.setOnClickListener {

            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.text = decryptMessage(EncryptionContent.text.toString(), ks, keyAlias)
            Toast.makeText(this@TextSelection, "The Decrypted text is in clipboard", Toast.LENGTH_SHORT).show()
        }

        bttn_apply.setOnClickListener {
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
        EncryptionContent.text = text.toString()
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


    fun encryptMessage(plaintext: ByteArray, ks: KeyStore, alias: String): String {
//        var protParam: ProtectionParameter = KeyStore.PasswordProtection(null);
//        val pkEntry = ks.getEntry(alias, protParam) as KeyStore.PrivateKeyEntry
        try {
        Log.e("alias", alias)
        var keyPublic = ks.getCertificate(alias).publicKey
        val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keyPublic)
        val ciphertext = cipher.doFinal(plaintext)
        Log.e("cipher data", ciphertext.toString())
            return Base64.encodeToString(ciphertext, Base64.DEFAULT)
        } catch (e: Exception) {
            var dbhelper1: SQLiteOpenHelper = DatabaseHelper(this@TextSelection)
            var db1: SQLiteDatabase? = dbhelper1.writableDatabase
            val cursor = db1!!.rawQuery("select * from user where name=?", arrayOf<String>(alias))
            cursor.moveToFirst()
            var endindex: Int = cursor.getString(cursor.getColumnIndex("modulus")).trim().length - 1
            var modulus =
                cursor.getString(cursor.getColumnIndex("modulus")).trim().substring(1, endindex).toBigInteger()
            var publicExponent: BigInteger = cursor.getString(cursor.getColumnIndex("exponent")).trim().toBigInteger()
            cursor.close()
            db1.close()
            var publickeyimported = generatePublicKeyImported(modulus, publicExponent)
            val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cipher.init(Cipher.ENCRYPT_MODE, publickeyimported)
            val ciphertext = cipher.doFinal(plaintext)
            Log.e("cipher data", Base64.encodeToString(ciphertext, Base64.DEFAULT))
            return Base64.encodeToString(ciphertext, Base64.DEFAULT)
        }
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

    fun decryptMessage(ciphertext: String, ks: KeyStore, alias: String): String {
        try {
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
        } catch (e: InvocationTargetException) {
            val t = e.targetException// 获取目标异常
            t.printStackTrace()
            e.printStackTrace()
            return "error"
        }
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
        var dbhelper1: SQLiteOpenHelper = DatabaseHelper(this@TextSelection)
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
}



