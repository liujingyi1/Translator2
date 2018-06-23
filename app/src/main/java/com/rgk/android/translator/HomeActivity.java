package com.rgk.android.translator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.rgk.android.translator.composemessage.ComposeMessageActivity;
import com.rgk.android.translator.database.beans.UserBean;
import com.rgk.android.translator.languagemodel.HomeLanguageItem;
import com.rgk.android.translator.languagemodel.ILanguageModel;
import com.rgk.android.translator.languagemodel.LanguageModelImpl;
import com.rgk.android.translator.mpush.IMPushApi;
import com.rgk.android.translator.mpush.MPushApi;
import com.rgk.android.translator.permission.RequestPermissionsActivity;
import com.rgk.android.translator.settings.SettingsActivity;
import com.rgk.android.translator.storage.TStorageManager;
import com.rgk.android.translator.utils.Logger;
import com.rgk.android.translator.utils.NetUtil;
import com.rgk.android.translator.utils.Utils;
import com.rgk.android.translator.view.CircleIndicatorView;

import java.util.ArrayList;
import java.util.List;

/*
add somethind
*/

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "RTranslator/HomeActivity";
    public static final String PREF_NAME = "Translator";
    private static final int PAGE_ROW_COUNT = 3;//行
    private static final int PAGE_COLUMNS_COUNT = 4;//列
    private static final int PAGE_ITEMS_COUNT = PAGE_ROW_COUNT * PAGE_COLUMNS_COUNT;

    private List<RecyclerView> mPages = new ArrayList<>();
    private ViewPager mViewPager;
    private CircleIndicatorView mIndicatorView;
    private ImageButton mSettingsBtn;

    private ILanguageModel mLanguageModel;
    private List<HomeLanguageItem> mLanguageItems;
    private int pageNum;
    private int lastPageItemNum;
    private TStorageManager mTStorageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);


        setContentView(R.layout.activity_main);

        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            Logger.i(TAG, "[onCreate]startPermissionActivity,return.");
            return;
        }

        setContentView(R.layout.activity_home);
        //初始化数据
        mTStorageManager = TStorageManager.getInstance();
        mLanguageModel = new LanguageModelImpl(this);
        mLanguageItems = mLanguageModel.getLanguageList();
        pageNum = mLanguageItems.size() / PAGE_ITEMS_COUNT + 1;
        lastPageItemNum = mLanguageItems.size() - (pageNum - 1) * PAGE_ITEMS_COUNT;
        Logger.v(TAG, "pageNum=" + pageNum + ", lastPageItemNum=" + lastPageItemNum);

        //push
        IMPushApi pushApi = MPushApi.get(this);
        SharedPreferences sp = this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String deviceId = sp.getString("PAIR_DEVICE_ID", null);
        pushApi.startPush(Utils.getDeviceId(getApplicationContext()));
        pushApi.setPairUser(deviceId);


        //初始化View
        LayoutInflater inflater = LayoutInflater.from(this);
        mViewPager = findViewById(R.id.id_language_items_container);

        for (int i = 0; i < pageNum; i++) {
            RecyclerView view = (RecyclerView) inflater.inflate(R.layout.layout_home_language_page, null);
            GridLayoutManager layoutManager = new GridLayoutManager(this, PAGE_COLUMNS_COUNT);
            view.setLayoutManager(layoutManager);
            LanguageRecyclerAdapter adapter = new LanguageRecyclerAdapter(this, i, mLanguageItems);
            adapter.setOnItemClickListener(mPagedItemOnClickListener);
            view.setAdapter(adapter);
            view.addItemDecoration(mItemDecoration);
            view.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
            mPages.add(view);
        }
        mViewPager.setAdapter(mPagerAdapter);
        mIndicatorView = findViewById(R.id.id_home_page_indicator);
        mIndicatorView.setUpWithViewPager(mViewPager);
        mSettingsBtn = findViewById(R.id.id_floating_action_button);
        mSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.v(TAG, "Settings click");
                Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
    }

    private OnItemClickListener mPagedItemOnClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(View view, int index) {
            Logger.v(TAG, "onItemClick:" + index + " - " + mLanguageItems.get(index).getCode());
            if (!NetUtil.isNetworkConnected(HomeActivity.this)) {
                //TODO start network settings
                Logger.i(TAG, "No network !");
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
            } else if (TextUtils.isEmpty(mTStorageManager.getPairedId())) {
                //TODO start paired settings
                Logger.i(TAG, "No paired !");
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
            } else {
                UserBean user = mTStorageManager.getUser();
                user.setLanguage(mLanguageItems.get(index).getCode());
                Intent composeActivityIntent = new Intent(HomeActivity.this, ComposeMessageActivity.class);
                composeActivityIntent.putExtra("LanguageName", mLanguageItems.get(index).getLanguageName());
                composeActivityIntent.putExtra("Language", mLanguageItems.get(index).getCode());
                startActivity(composeActivityIntent);
            }
        }
    };

    public interface OnItemClickListener {
        void onItemClick(View view, int index);
    }

    class LanguageRecyclerAdapter extends RecyclerView.Adapter<LanguageRecyclerHolder> {
        private int pageIndex;
        private List<HomeLanguageItem> datas;
        private LayoutInflater inflater;

        private OnItemClickListener mOnItemClickListener;

        public void setOnItemClickListener(OnItemClickListener listener) {
            mOnItemClickListener = listener;
        }

        public LanguageRecyclerAdapter(Context context, int pageIndex, List<HomeLanguageItem> datas) {
            this.pageIndex = pageIndex;
            this.datas = datas;
            inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public LanguageRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            view = inflater.inflate(R.layout.layout_home_language_item, parent, false);
            LanguageRecyclerHolder holder = new LanguageRecyclerHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull LanguageRecyclerHolder holder, final int position) {
            //Logger.v(TAG, "onBindViewHolder-pageIndex="+pageIndex+", position="+position);
            final int realIndex = pageIndex * PAGE_ITEMS_COUNT + position;
            holder.icon.setImageResource(datas.get(realIndex).getIconRes());
            holder.name.setText(datas.get(realIndex).getLanguageName());
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(v, realIndex);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            if ((pageIndex + 1) < pageNum) {
                return PAGE_ITEMS_COUNT;
            } else {
                return lastPageItemNum;
            }
        }
    }

    class LanguageRecyclerHolder extends RecyclerView.ViewHolder {
        private View view;
        private ImageView icon;
        private TextView name;

        public LanguageRecyclerHolder(View itemView) {
            super(itemView);
            view = itemView;
            icon = itemView.findViewById(R.id.id_home_language_item_icon);
            name = itemView.findViewById(R.id.id_home_language_item_name);
        }
    }

    PagerAdapter mPagerAdapter = new PagerAdapter() {

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            container.addView(mPages.get(position));
            return mPages.get(position);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView(mPages.get(position));
        }

        @Override
        public int getCount() {
            return mPages.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    };

    RecyclerView.ItemDecoration mItemDecoration = new RecyclerView.ItemDecoration() {
        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDraw(c, parent, state);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.top = 3;
                outRect.left = 3;
                outRect.right = 3;
                outRect.bottom = 3;
        }
    };
}
