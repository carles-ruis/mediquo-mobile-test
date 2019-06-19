package com.carles.mediquomobiletest.view

import android.support.annotation.LayoutRes
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.ViewGroup

fun ViewGroup.inflate(@LayoutRes layoutRes: Int) = LayoutInflater.from(context).inflate(layoutRes, this, false)

inline fun AppCompatActivity.consume(f: () -> Unit): Boolean {
    f()
    return true
}

fun AppCompatActivity.getStrings(ids:List<Int>) = ids.map { getString(it) }.toTypedArray()
