package com.application.smartstation.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.application.smartstation.R;
import com.application.smartstation.tokenautocomplete.TokenCompleteTextView;
import com.application.smartstation.ui.model.Person;

public class ContactsCompletionView extends TokenCompleteTextView<Person> {

    public ContactsCompletionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View getViewForObject(Person person) {

        LayoutInflater l = (LayoutInflater) LayoutInflater.from(getContext());
        LinearLayout view = (LinearLayout) l.inflate(R.layout.contact_token, (ViewGroup) ContactsCompletionView.this.getParent(), false);
        ((TextView) view.findViewById(R.id.name)).setText(person.getEmail());

        return view;
    }

    @Override
    protected Person defaultObject(String completionText) {
        //Oversimplified example of guessing if we have an email or not
        int index = completionText.indexOf('@');
        if (index == -1) {
            return new Person(completionText, completionText.replace(" ", "") + "@fsmartstation.com");
        } else {
            return new Person(completionText.substring(0, index), completionText);
        }
    }
}
