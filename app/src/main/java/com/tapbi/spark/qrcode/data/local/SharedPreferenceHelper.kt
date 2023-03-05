package com.tapbi.spark.qrcode.data.local

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONException
import javax.inject.Inject


class SharedPreferenceHelper @Inject constructor(private val sharedPreferences: SharedPreferences) {

    fun storeString(key: String?, value: String?) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getString(key: String?): String? {
        return sharedPreferences.getString(key, DEFAULT_STRING)
    }

    fun getStringWithDefault(key: String?, defaultValue: String?): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    fun storeInt(key: String?, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    fun getInt(key: String?): Int {
        return sharedPreferences.getInt(key, DEFAULT_NUM)
    }

    fun getIntWithDefault(key: String?, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun storeLong(key: String?, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }

    fun getLong(key: String?): Long {
        return sharedPreferences.getLong(key, DEFAULT_NUM.toLong())
    }

    fun storeBoolean(key: String?, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String?, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun storeFloat(key: String?, value: Float) {
        sharedPreferences.edit().putFloat(key, value).apply()
    }

    fun getFloat(key: String?): Float {
        return sharedPreferences.getFloat(key, 0f)
    }

    fun setStringArrayPref(key: String?, values: List<String?>) {
        val editor = sharedPreferences.edit()
        val a = JSONArray()
        for (i in values.indices) {
            a.put(values[i])
        }
        if (values.isNotEmpty()) {
            editor.putString(key, a.toString())
        } else {
            editor.putString(key, DEFAULT_STRING)
        }
        editor.apply()
    }

    fun getStringArrayPref(key: String?): List<String> {
        val json = sharedPreferences.getString(key, null)
        val urls = ArrayList<String>()
        if (json != null) {
            try {
                val a = JSONArray(json)
                for (i in 0 until a.length()) {
                    val url = a.optString(i)
                    urls.add(url)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return urls
    }

    fun setIntArray(key: String?, values: List<Int?>) {
        val editor = sharedPreferences.edit()
        val a = JSONArray()
        for (i in values.indices) {
            a.put(values[i])
        }
        if (values.isNotEmpty()) {
            editor.putString(key, a.toString())
        } else {
            editor.putString(key, "")
        }
        editor.apply()
    }

    fun getIntArray(key: String?): List<Int> {
        val json = sharedPreferences.getString(key, null)
        val urls = ArrayList<Int>()
        if (json != null) {
            try {
                val a = JSONArray(json)
                for (i in 0 until a.length()) {
                    val url = a.optString(i)
                    urls.add(Integer.valueOf(url))
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return urls
    }

    fun removeKey(key: String?) {
        sharedPreferences.edit().remove(key).apply()
    }

    fun resetAll() {
        sharedPreferences.edit().clear().apply()
    }

    fun containKey(key: String?): Boolean {
        return sharedPreferences.contains(key)
    }


    companion object {
        const val DEFAULT_NUM = 0
        const val DEFAULT_STRING = ""
    }
}
