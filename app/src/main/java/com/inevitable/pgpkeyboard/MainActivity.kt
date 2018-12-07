package com.inevitable.pgpkeyboard

import android.os.Bundle
import android.security.*
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
//import com.cossacklabs.themis.ISessionCallbacks;
//import com.cossacklabs.themis.InvalidArgumentException;
//import com.cossacklabs.themis.KeyGenerationException;
//import com.cossacklabs.themis.KeypairGenerator;
//import com.cossacklabs.themis.Keypair;
//import com.cossacklabs.themis.NullArgumentException;
//import com.cossacklabs.themis.PublicKey;
//import com.cossacklabs.themis.SecureCellData;
//import com.cossacklabs.themis.SecureCellException;
//import com.cossacklabs.themis.SecureSession;
//import com.cossacklabs.themis.SecureSessionException;
//import com.cossacklabs.themis.SecureCell;

import java.security.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import android.security.keystore.KeyProperties
import android.security.keystore.KeyGenParameterSpec
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.util.Log
import android.widget.EditText
import java.util.logging.Logger
import android.R.string.cancel
import android.content.DialogInterface
import android.widget.ArrayAdapter
import android.widget.ListView
import android.view.Window.FEATURE_NO_TITLE
import android.app.Activity
import android.view.Window.FEATURE_NO_TITLE


class MainActivity : AppCompatActivity() {

    val datas:MutableList<String> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        //get keystore
//        val kpg = KeyPairGenerator.getInstance(
//            KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore"
//        )
//
//        kpg.initialize(
//            KeyGenParameterSpec.Builder(
//                "hh",
//                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
//            )
//                .setDigests(
//                    KeyProperties.DIGEST_SHA256,
//                    KeyProperties.DIGEST_SHA512
//                )
//                .build()
//        )
//
//
//        val kp = kpg.generateKeyPair()
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)

        for(i in ks.aliases()){
            datas.add(i)
        }
//        testOutput.setText(ks.getEntry("hh",null).toString())


        //list view show keystore alias
        val listView = findViewById(R.id.lv) as ListView
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, datas)
        listView.setAdapter(adapter)

//        button_keypari.setOnClickListener { view ->
//            Snackbar.make(view,KeyPairGenerator.getInstance("RSA").generateKeyPair().getPrivate(), Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }
//    }
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
    }


//                button_keypari.setOnClickListener {
//                    Toast.makeText(this,"click",Toast.LENGTH_SHORT).show();
//        }
//    }

    fun create_key(alias:String){
        val new_key = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore"
        )

        new_key.initialize(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setDigests(
                    KeyProperties.DIGEST_SHA256,
                    KeyProperties.DIGEST_SHA512
                )
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
}
