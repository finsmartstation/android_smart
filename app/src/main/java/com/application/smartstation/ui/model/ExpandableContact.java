package com.application.smartstation.ui.model;

import android.os.Parcelable;

import com.thoughtbot.expandablecheckrecyclerview.models.MultiCheckExpandableGroup;

import io.realm.RealmList;

public class ExpandableContact  extends MultiCheckExpandableGroup implements Parcelable {



    public ExpandableContact(String contactName, RealmList<PhoneNumber> phoneNumbers) {
        super(contactName, phoneNumbers);

    }

}
