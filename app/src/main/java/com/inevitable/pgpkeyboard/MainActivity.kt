package com.inevitable.pgpkeyboard

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

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
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
        adapter = ListItemKeypairAdapter(this@MainActivity, mList)
//        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, datas)
        listView.setAdapter(adapter)
        listView.setOnItemClickListener { adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->
            Toast.makeText(this@MainActivity, "CLick$l", Toast.LENGTH_SHORT).show()
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
                mList.removeAt(i);
                adapter.notifyDataSetChanged();
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
    }


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

    private fun initList() {
        for (x in 1..20 step 1) {
            mList.add("$x")
        }
    }
}
