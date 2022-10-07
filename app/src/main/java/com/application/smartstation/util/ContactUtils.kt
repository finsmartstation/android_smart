package com.application.smartstation.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import com.application.smartstation.app.App
import com.application.smartstation.ui.model.ExpandableContact
import com.application.smartstation.ui.model.PhoneContact
import com.application.smartstation.ui.model.PhoneNumber
import com.thoughtbot.expandablecheckrecyclerview.models.MultiCheckExpandableGroup
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import com.wafflecopter.multicontactpicker.ContactResult
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.realm.RealmList
import java.util.*

object ContactUtils {

    private fun getRawContactsObservable(context: Context): Observable<PhoneContact> {

        return Observable.create { emitter: ObservableEmitter<PhoneContact> ->


            val contactsList: MutableList<PhoneContact> = ArrayList()
            val uri = ContactsContract.Contacts.CONTENT_URI
            val projection = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
            )
            val selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '1'"
            val selectionArgs: Array<String>? = null
            val sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC"

            // Build adapter with contact entries
//            var mCursor: Cursor? = null
            var mPhoneNumCursor: Cursor? = null
            context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.let { mCursor ->
                try {


                    while (mCursor.moveToNext()) {
                        //get contact name
                        val name = mCursor.getString(mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))

                        //get contact name
                        val contactID = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts._ID))
                        //create new phoneContact object
                        val contact = PhoneContact()
                        contact.id = contactID.toInt()
                        contact.name = name


                        //get all phone numbers in this contact if it has multiple numbers
                        context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", arrayOf(contactID), null)?.let { phoneNumCursor ->

                            mPhoneNumCursor = phoneNumCursor

                            phoneNumCursor?.moveToFirst()


                            //create empty list to fill it with phone numbers for this contact
                            val phoneNumberList: MutableList<String> = ArrayList()
                            while (!phoneNumCursor.isAfterLast) {
                                //get phone number
                                val number = phoneNumCursor.getString(phoneNumCursor.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER))


                                //prevent duplicates numbers
                                if (!phoneNumberList.contains(number)) phoneNumberList.add(number)
                                phoneNumCursor.moveToNext()
                            }

                            //fill contact object with phone numbers
                            contact.phoneNumbers = phoneNumberList
                            //add final phoneContact object to contactList
                            contactsList.add(contact)
                            emitter.onNext(contact)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    mCursor?.close()
                    mPhoneNumCursor?.close()
                    emitter.onComplete()
                }
            }
        }

    }

    //get the Contact name from phonebook by number
    @JvmStatic
    fun queryForNameByNumber(phone: String): String {
        val context = App.context()
        var name = phone
        try {
            val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone))
            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    name = cursor.getString(0)
                }
                cursor.close()
            }
        } catch (e: Exception) {
            return name
        }
        return name
    }

    //check if a contact is exists in phonebook
    @JvmStatic
    fun contactExists(context: Context, number: String?): Boolean {
        var cur: Cursor? = null

        try {
            val lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
            val mPhoneNumberProjection = arrayOf(ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME)
            cur = context.contentResolver.query(lookupUri, mPhoneNumberProjection, null, null, null)
            if (cur != null) {
                if (cur.moveToFirst()) {
                    return true
                }
            }
        } catch (e: Exception) {
        } finally {
            cur?.close()
        }

        return false


    }

    //convert the contacts that the user's picked into an ExpandableContact list
    @JvmStatic
    fun getContactsFromContactResult(results: List<ContactResult>): List<ExpandableContact> {
        val contactList: MutableList<ExpandableContact> = ArrayList()
        for (result in results) {
            val phoneNumbers = RealmList<PhoneNumber>()
            for (s in result.phoneNumbers) {
                if (!phoneNumbers.contains(PhoneNumber(s.number))) phoneNumbers.add(PhoneNumber(s.number))
            }
            val contactName = ExpandableContact(result.displayName, phoneNumbers)
            contactList.add(contactName)
        }
        return contactList
    }

    //get only selected phone numbers
    @JvmStatic
    fun getContactsFromExpandableGroups(groups: List<ExpandableGroup<*>?>): List<ExpandableContact> {
        val contactNameList: MutableList<ExpandableContact> = ArrayList()
        for (x in groups.indices) {
            val group = groups[x] as MultiCheckExpandableGroup? ?: continue
            val name = group.title
            val phoneNumberList = RealmList<PhoneNumber>()
            for (i in group.items.indices) {
                val phoneNumber = group.items[i] as PhoneNumber
                //get only selected numbers && prevent duplicate numbers
                if (group.selectedChildren[i] && !phoneNumberList.contains(phoneNumber)) {
                    phoneNumberList.add(phoneNumber)
                }
            }
            if (!phoneNumberList.isEmpty()) contactNameList.add(ExpandableContact(name, phoneNumberList))
        }
        return contactNameList
    }

}