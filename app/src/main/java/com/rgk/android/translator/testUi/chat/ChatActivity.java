package com.rgk.android.translator.testUi.chat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mpush.api.Client;
import com.mpush.api.http.HttpResponse;
import com.rgk.android.translator.R;
import com.rgk.android.translator.database.beans.MessageBean;
import com.rgk.android.translator.mpush.HttpClientListener;
import com.rgk.android.translator.mpush.HttpProxyCallback;
import com.rgk.android.translator.mpush.IMPushApi;
import com.rgk.android.translator.mpush.MPushApi;

import java.util.Random;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    TextView codeFrom;
    TextView info;
    EditText codeTo;
    Button bindBtn;
    EditText text;
    Button sendBtn;

    String mUserId;
    IMPushApi mPushApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initView();

        if (getApplicationContext()
                .checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED){
            requestPermissions();
        }

        mUserId = getRandomCode();
        codeFrom.setText(mUserId);

        mPushApi = MPushApi.get(this);
        mPushApi.startPush(mUserId);
        mPushApi.bindUser(mUserId);
        mPushApi.setHttpCallBack(new HttpProxyCallback() {
            @Override
            public void onResponse(HttpResponse httpResponse) {
                Log.i("jingyi", "ChatActivity httpResponse="+httpResponse.toString());
            }

            @Override
            public void onCancelled() {
            }
        });
        mPushApi.setHttpClientListener(new HttpClientListener(){
            @Override
            public void onReceivePush(Client client, MessageBean messageBean, int i) {
                super.onReceivePush(client, messageBean, i);
//                String message = new String(messageBean.getText(), Constants.UTF_8);
//                Gson gson = new Gson();
//                MessageResponse messageRes = gson.fromJson(message, new TypeToken<MessageResponse>(){}.getType());
//                MessageContent messageContent = gson.fromJson(messageRes.getContent(), new TypeToken<MessageContent>(){}.getType());
//                String content = messageContent.getContent();


                Log.i("jingyi", "ChatActivity content="+messageBean.getText());
                info.setText(messageBean.getText());
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_44: {

//                mPushApi.

                break;
            }

            case R.id.server_list: {

                MessageBean messageBean = new MessageBean(1);
                messageBean.setText("123");
                mPushApi.sendPush(messageBean);

                break;
            }
            case R.id.bind_user: {

//                getWeatherData();

                if (mPushApi == null) {
                    mPushApi = MPushApi.get(this);
                }
                String pairUserId = codeTo.getText().toString();

                Log.i("jingyi", "pairUser="+pairUserId);
                mPushApi.setPairUser(pairUserId);

                break;
            }
            case R.id.send: {
                if (mPushApi == null) {
                    mPushApi = MPushApi.get(this);
                }
                String textStr = text.getText().toString();

                MessageBean messageBean = new MessageBean(1);
                messageBean.setText(textStr);
                messageBean.setLanguage("zh");
                mPushApi.sendPush(messageBean);
                break;
            }
            default:
                break;
        }
    }

    private void requestPermissions() {
        this.requestPermissions(new String[]{
                        Manifest.permission.READ_PHONE_STATE},
                1);
    }

    private String getRandomCode() {
        StringBuilder codeStr = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 4; i++) {
            codeStr.append(random.nextInt(9));
        }
        return codeStr.toString();
    }

    private void initView() {
        codeFrom = (TextView)findViewById(R.id.code_from);
        codeTo = (EditText)findViewById(R.id.code_to);
        bindBtn = (Button)findViewById(R.id.bind_user);
        text = (EditText)findViewById(R.id.send_text);
        sendBtn = (Button)findViewById(R.id.send);
        info = (TextView)findViewById(R.id.info);

        Button button1 = (Button)findViewById(R.id.server_list);
        Button button2 = (Button)findViewById(R.id.btn_11);
        Button button3 = (Button)findViewById(R.id.btn_22);
        Button button4 = (Button)findViewById(R.id.btn_33);
        Button button5 = (Button)findViewById(R.id.btn_44);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        button5.setOnClickListener(this);


        codeTo.setInputType(EditorInfo.TYPE_CLASS_PHONE);

        bindBtn.setOnClickListener(this);
        sendBtn.setOnClickListener(this);
    }

    public interface GetWeatherApi {
        @GET("58321.js")
        Call<ResponseBody> getCall();
    }

    public void getWeatherData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://tianqi.2345.com/t/7day_tq_js/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GetWeatherApi request = retrofit.create(GetWeatherApi.class);

        Call<ResponseBody> call = request.getCall();

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        String data = new String(response.body().string().getBytes(),"GBK");// response.body().string().getBytes(Constants.UTF_8);
                        Log.i("jingyi", "Result is :" + data);
                    } catch (Exception e) {

                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });

    }




}
