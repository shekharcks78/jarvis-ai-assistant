package com.jarvis.assistant

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract

data class ContactMatch(val name: String, val phoneNumber: String)

object ContactHelper {

    fun searchContact(context: Context, queryName: String): ContactMatch? {
        val contentResolver = context.contentResolver
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        
        val cleanQuery = queryName.trim().lowercase()
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$cleanQuery%")

        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )

            if (cursor != null && cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                if (nameIndex != -1 && numberIndex != -1) {
                    val foundName = cursor.getString(nameIndex)
                    val foundNumber = cursor.getString(numberIndex)
                    return ContactMatch(foundName, foundNumber)
                }
            }
        } catch (e: SecurityException) {
            return null
        } finally {
            cursor?.close()
        }
        return null
    }
}
