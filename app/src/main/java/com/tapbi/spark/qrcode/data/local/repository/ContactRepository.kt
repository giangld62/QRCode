package com.tapbi.spark.qrcode.data.local.repository

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.tapbi.spark.qrcode.data.model.Contact
import javax.inject.Inject


class ContactRepository @Inject constructor() {

    @SuppressLint("Range")
    fun getContact(context: Context, uri: Uri?): Contact? {
        val contact = Contact()
        val cr = context.contentResolver
        return try {
            uri?.let {
                val cur = cr.query(uri, null, null, null, null)
                cur!!.moveToFirst()
                val id =
                    cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                contact.name =
                    cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                cur.close()


                val phoneCursor = cr.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(id), null
                )
                while (phoneCursor!!.moveToNext()) {
                    contact.phoneNumber =
                        phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                }
                phoneCursor.close()


                val emailCursor = cr.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                    arrayOf(id),
                    null
                )
                while (emailCursor!!.moveToNext()) {
                    contact.email =
                        emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS))
                }
                emailCursor.close()


//                val postalUri = StructuredPostal.CONTENT_URI
//                val postalCursor = cr.query(
//                    postalUri,
//                    null,
//                    ContactsContract.Data.CONTACT_ID + "=",
//                    arrayOf(id),
//                    null
//                )
//                postalCursor?.let {
//                    if (it.moveToFirst()) {
//                        val street = it.getString(it.getColumnIndex(StructuredPostal.STREET))
//                        val city = it.getString(it.getColumnIndex(StructuredPostal.CITY))
//                        val country = it.getString(it.getColumnIndex(StructuredPostal.COUNTRY))
//                        contact.address = "$street-$city-$country"
//                    }
//                    it.close()
//                }
                contact
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}