package com.rgk.android.translator.composemessage;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rgk.android.translator.AppContext;
import com.rgk.android.translator.R;
import com.rgk.android.translator.adapter.ComposeListAdapter;
import com.rgk.android.translator.database.DbConstants.MessageType;
import com.rgk.android.translator.database.beans.MessageBean;
import com.rgk.android.translator.media.MediaManager;
import com.rgk.android.translator.mvpbase.BaseMvpActivity;
import com.rgk.android.translator.mvpbase.BasePresenter;
import com.rgk.android.translator.tts.TTSConstants;
import com.rgk.android.translator.utils.Logger;
import com.rgk.android.translator.view.AudioRecorderButton;
import com.rgk.android.translator.view.TextViewAnim;

import java.util.ArrayList;
import java.util.List;

public class ComposeMessageActivity extends BaseMvpActivity implements View.OnClickListener, ComposeMessageContact.IComposeMessageView {
    private static final String TAG = "RTranslator/ComposeMessageActivity";

    private static final int MSG_RECEIVED = 1001;
    private static final int MSG_PLAY_ANIMI = 1002;
    private static final int MSG_SPEAK_SUCCESS = 1003;

    private List<String> permissions = new ArrayList<>();

    private RecyclerView mComposeList;

    private ComposeListAdapter mComposeLiseAdapter;
    private List<MessageBean> mDatas = new ArrayList<>();

    private AudioRecorderButton mAudioRecorderButton;
    private TextViewAnim mTextViewAnim;
    private List<Integer> mTextViewAnimList = new ArrayList<>();
    private ImageView mEndConversationBtn;
    private TextView mLanguageNameText;
    private ImageView mTextSizeImage;
    private ImageView mAutoImage;
    private LinearLayoutManager mLayoutManager;

    private ComposeMessageContact.IComposeMessagePresenter mComposeMessagePresenter;

    private String mLanguageName;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_compose_message;
    }

    @Override
    public void initView() {
        mAudioRecorderButton = findViewById(R.id.id_recorder_btn);
        mAudioRecorderButton.setAudioRecorderStateListener(new AudioRecorderButton.onAudioRecorderStateListener() {
            @Override
            public void onFinish(boolean isToShort, int stats) {
                Logger.i(TAG, "AudioRecorderStateListener - onFinish");
                mComposeMessagePresenter.recordFinish();
            }

            @Override
            public void onStart() {
                Logger.i(TAG, "AudioRecorderStateListener - onStart");
                mComposeMessagePresenter.recordStart();
            }
        });

        mComposeList = findViewById(R.id.id_compose_list);
        mLayoutManager = new LinearLayoutManager(this);
        mComposeList.setLayoutManager(mLayoutManager);
        mComposeLiseAdapter = new ComposeListAdapter(this, mDatas);
        mComposeLiseAdapter.updateTextSize(mComposeMessagePresenter.getTextSize());
        mComposeList.setAdapter(mComposeLiseAdapter);
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        mComposeList.setItemAnimator(itemAnimator);
        mComposeLiseAdapter.setOnItemClickListener(new ComposeListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Logger.v(TAG, "onItemClick:" + position);
                if ((mDatas.get(position).getType() == MessageType.TYPE_SOUND
                        || mDatas.get(position).getType() == MessageType.TYPE_SOUND_TEXT)
                        && !mDatas.get(position).isSend()) {
                    //播放动画
                    mTextViewAnim.startPlayAnim((TextView) view,
                            mDatas.get(position).getText(), mTextViewAnimList, R.drawable.ic_play_recordor_wave_receive_v3);


                    //播放音频
                    MediaManager.playSound(mDatas.get(position).getUrl(), new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mTextViewAnim.stopPlayAnim();
                        }
                    });
                }
            }
        });
        mLanguageNameText = findViewById(R.id.id_language_name);
        mLanguageNameText.setText(mLanguageName);
        mEndConversationBtn = findViewById(R.id.id_compose_end_img);
        mEndConversationBtn.setOnClickListener(this);
        mTextSizeImage = findViewById(R.id.id_text_size_big);
        mTextSizeImage.setOnClickListener(this);
        mAutoImage = findViewById(R.id.id_auto_img);
        mAutoImage.setOnClickListener(this);
    }

    @Override
    public void initData() {
        mTextViewAnim = TextViewAnim.getInstance(this);
        mTextViewAnimList.clear();
        mTextViewAnimList.add(R.drawable.ic_play_recordor_wave_receive_v1);
        mTextViewAnimList.add(R.drawable.ic_play_recordor_wave_receive_v2);
        mTextViewAnimList.add(R.drawable.ic_play_recordor_wave_receive_v3);
    }

    @Override
    protected BasePresenter bindPresenter() {
        return mComposeMessagePresenter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        String language = intent.getStringExtra("Language");
        mLanguageName = intent.getStringExtra("LanguageName");

        mComposeMessagePresenter = new ComposeMessagePresenterImpl(language, this, this);

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.id_text_size_big: {
                Logger.v(TAG, "Size");
                float mTextSize = mComposeMessagePresenter.getTextSize();
                if (AppContext.TEXT_SIZE_SMALL == mTextSize) {
                    mTextSize = AppContext.TEXT_SIZE_NORMAL;
                } else if (AppContext.TEXT_SIZE_NORMAL == mTextSize) {
                    mTextSize = AppContext.TEXT_SIZE_LARGE;
                } else {
                    mTextSize = AppContext.TEXT_SIZE_SMALL;
                }
                mComposeMessagePresenter.setTextSize(mTextSize);
                mComposeLiseAdapter.updateTextSize(mTextSize);
                mComposeLiseAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.id_auto_img: {
                Logger.v(TAG, "Auto");
                mComposeMessagePresenter.setAuto();
                break;
            }

            case R.id.id_compose_end_img: {
                finish();
                break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        mComposeMessagePresenter.onResume();
        super.onResume();
        checkPermission();
    }

    @Override
    protected void onPause() {
        mComposeMessagePresenter.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected void checkPermission() {
        permissions.clear();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (!permissions.isEmpty()) {
            String[] ps = new String[permissions.size()];
            permissions.toArray(ps);
            ActivityCompat.requestPermissions(this, ps, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void callbackSTT(MessageBean messageBean) {
        if (messageBean != null) {
            mComposeLiseAdapter.addItem(messageBean);
            mComposeList.smoothScrollToPosition(mDatas.size() - 1);
            mAudioRecorderButton.dismissRecordingDialog();
        } else {
            Toast.makeText(getApplicationContext(), R.string.final_response_error, Toast.LENGTH_LONG).show();
            mAudioRecorderButton.dismissRecordingDialog();
        }
    }

    @Override
    public void updateVoiceLevel(int level) {
        mAudioRecorderButton.updateVoiceLevel(level);
    }

    @Override
    public void receiveMessage(MessageBean messageBean) {
        Message message = new Message();
        message.what = MSG_RECEIVED;
        message.obj = messageBean;
        H.sendMessage(message);
    }

    @Override
    public void callbackTTS(int status) {
        switch (status) {
            case TTSConstants.STATE_SPEAK_SUCCESS: {
                H.sendEmptyMessage(MSG_SPEAK_SUCCESS);
                break;
            }

            case TTSConstants.STATE_SPEAK_ERROR: {
                break;
            }

            case TTSConstants.STATE_TIMEOUT: {
                break;
            }

            case TTSConstants.STATE_SYN_SUCCESS: {
                break;
            }

            case TTSConstants.STATE_SYN_ERROR: {
                break;
            }
        }
    }

    private Handler H = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RECEIVED: {
                    MessageBean messageBean = (MessageBean) msg.obj;
                    mComposeLiseAdapter.addItem(messageBean);
                    int currentPos = mDatas.size() - 1;
                    Logger.w(TAG, "currentPos = " + currentPos);
                    mComposeList.smoothScrollToPosition(currentPos);
                    if (mComposeMessagePresenter.getAuto()) {
                        Message message = new Message();
                        message.what = MSG_PLAY_ANIMI;
                        message.arg1 = currentPos;
                        sendMessageDelayed(message, 200);
                    }
                    break;
                }

                case MSG_PLAY_ANIMI: {
                    View item = mLayoutManager.findViewByPosition(msg.arg1);
                    //播放动画
                    if (item == null) {
                        Logger.w(TAG, "itemView is NULL");
                    } else {
                        mTextViewAnim.startPlayAnim((TextView) item.findViewById(R.id.id_msg_txt),
                                mDatas.get(msg.arg1).getText(), mTextViewAnimList, R.drawable.ic_play_recordor_wave_receive_v3);
                    }
                    break;
                }
                case MSG_SPEAK_SUCCESS: {
                    mTextViewAnim.stopPlayAnim();
                    break;
                }
            }
        }
    };
}