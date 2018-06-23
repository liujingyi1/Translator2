package com.rgk.android.translator.languagemodel;

import android.content.Context;

import com.rgk.android.translator.R;

import java.util.ArrayList;
import java.util.List;

public class LanguageModelImpl implements ILanguageModel {
    private Context mContext;
    private List<HomeLanguageItem> mLanguageItems = new ArrayList<>();


    public LanguageModelImpl(Context context) {
        mContext = context;
        initLanguageItems();
    }

    @Override
    public List<HomeLanguageItem> getLanguageList() {
        return mLanguageItems;
    }

    private void initLanguageItems() {
        mLanguageItems.clear();

        mLanguageItems.add(new HomeLanguageItem(R.drawable.ic_zh_cn, "zh-CN", mContext.getString(R.string.language_name_zh_cn)));
        mLanguageItems.add(new HomeLanguageItem(R.drawable.ic_zh_cn, "zh-HK", mContext.getString(R.string.language_name_zh_hk)));
        mLanguageItems.add(new HomeLanguageItem(R.drawable.ic_en_us, "en-US", mContext.getString(R.string.language_name_en_us)));
        mLanguageItems.add(new HomeLanguageItem(R.mipmap.ic_launcher, "en-AU", mContext.getString(R.string.language_name_en_au)));
        mLanguageItems.add(new HomeLanguageItem(R.mipmap.ic_launcher, "en-CA", mContext.getString(R.string.language_name_en_ca)));
        mLanguageItems.add(new HomeLanguageItem(R.mipmap.ic_launcher, "en-GB", mContext.getString(R.string.language_name_en_gb)));
        mLanguageItems.add(new HomeLanguageItem(R.mipmap.ic_launcher, "en-IN", mContext.getString(R.string.language_name_en_in)));
        mLanguageItems.add(new HomeLanguageItem(R.mipmap.ic_launcher, "en-NZ", mContext.getString(R.string.language_name_en_nz)));
        mLanguageItems.add(new HomeLanguageItem(R.mipmap.ic_launcher, "ar-EG", mContext.getString(R.string.language_name_ar_eg)));
        mLanguageItems.add(new HomeLanguageItem(R.mipmap.ic_launcher, "da-DK", mContext.getString(R.string.language_name_da_dk)));
        mLanguageItems.add(new HomeLanguageItem(R.mipmap.ic_launcher, "de-DE", mContext.getString(R.string.language_name_de_de)));
        mLanguageItems.add(new HomeLanguageItem(R.mipmap.ic_launcher, "es-ES", mContext.getString(R.string.language_name_es_es)));
        mLanguageItems.add(new HomeLanguageItem(R.mipmap.ic_launcher, "es-MX", mContext.getString(R.string.language_name_es_mx)));
        mLanguageItems.add(new HomeLanguageItem(R.mipmap.ic_launcher, "fi-FI", mContext.getString(R.string.language_name_fi_fi)));
        mLanguageItems.add(new HomeLanguageItem(R.mipmap.ic_launcher, "fr-CA", mContext.getString(R.string.language_name_fr_ca)));
        mLanguageItems.add(new HomeLanguageItem(R.mipmap.ic_launcher, "fr-FR", mContext.getString(R.string.language_name_fr_fr)));
        mLanguageItems.add(new HomeLanguageItem(R.mipmap.ic_launcher, "it-IT", mContext.getString(R.string.language_name_it_it)));
        mLanguageItems.add(new HomeLanguageItem(R.drawable.ic_jp, "ja-JP", mContext.getString(R.string.language_name_ja_jp)));
        mLanguageItems.add(new HomeLanguageItem(R.mipmap.ic_launcher, "ko-KR", mContext.getString(R.string.language_name_ko_kr)));
        mLanguageItems.add(new HomeLanguageItem(R.mipmap.ic_launcher, "pl-PL", mContext.getString(R.string.language_name_pl_pl)));
        mLanguageItems.add(new HomeLanguageItem(R.mipmap.ic_launcher, "pt-BR", mContext.getString(R.string.language_name_pt_br)));
        mLanguageItems.add(new HomeLanguageItem(R.mipmap.ic_launcher, "pt-PT", mContext.getString(R.string.language_name_pt_pt)));
        mLanguageItems.add(new HomeLanguageItem(R.mipmap.ic_launcher, "ru-RU", mContext.getString(R.string.language_name_ru_ru)));
        mLanguageItems.add(new HomeLanguageItem(R.mipmap.ic_launcher, "sv-SE", mContext.getString(R.string.language_name_sv_se)));


        mLanguageItems.add(new HomeLanguageItem(R.mipmap.ic_launcher, "hi-IN", mContext.getString(R.string.language_name_hi_in)));
    }
}
