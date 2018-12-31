package com.inevitable.pgpkeyboard

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(ctx: Context) : SQLiteOpenHelper(ctx, "EM.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        val sql: String = "create table user(id integer primary key autoincrement," +
                "name  varchar(64) unique," +
                "exponent BIGINT(20)," +
                "modulus Memo," +
                "alias varchar(64))"
        db!!.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val sql: String = "alter table person add certificate memo"
        db!!.execSQL(sql)
    }
}