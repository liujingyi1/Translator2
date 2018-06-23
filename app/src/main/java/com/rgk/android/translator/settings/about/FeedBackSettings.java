package com.rgk.android.translator.settings.about;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.rgk.android.translator.R;

public class FeedBackSettings {
    private Context mContext;
    private Spinner mQuestionType;
    private Button mDescriptionTypeSwitch;
    private EditText mDescriptionText;
    private Button mDescriptionSound;
    private EditText mPhoneNumber;
    private Button mSubmitBtn;

    public FeedBackSettings(Context context) {
        mContext = context;
    }

    public void onCreateView(View v) {
        initViews(v);

    }

    private void initViews(View v) {
        mQuestionType = v.findViewById(R.id.question_type);
        mDescriptionTypeSwitch = v.findViewById(R.id.question_description_input_type);
        mDescriptionText = v.findViewById(R.id.question_description_text);
        mDescriptionSound = v.findViewById(R.id.question_description_sound);
        mPhoneNumber = v.findViewById(R.id.phone_number);
        mSubmitBtn = v.findViewById(R.id.submit_button);
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
    }

    private void submit() {

    }

    public void onStart() {

    }

    public void onResume() {

    }

    public void onPause() {

    }

    public void onStop() {

    }
}
