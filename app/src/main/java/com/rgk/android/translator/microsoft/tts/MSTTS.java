package com.rgk.android.translator.microsoft.tts;

import android.content.Context;

import com.rgk.android.translator.R;
import com.rgk.android.translator.tts.ITTS;
import com.rgk.android.translator.tts.ITTSListener;
import com.rgk.android.translator.utils.Logger;

import java.util.HashMap;

public class MSTTS implements ITTS {

    private static final String TAG = "RTranslator/MTTTS";
    private Synthesizer m_syn;
    ITTSListener mListener;
    MSDataModel model;

    public MSTTS(Context context) {
        if (m_syn == null) {
            model = new MSDataModel();
            m_syn = Synthesizer.getInstance(context.getString(R.string.primaryKey));
        }
        m_syn.setServiceStrategy(Synthesizer.ServiceStrategy.AlwaysService);
    }


    @Override
    public void setVoice(String language, boolean male, boolean isServiceVoice) {
        if (model == null) return;
        String voiceName =  model.getVoiceName(language,male);
        Logger.d(TAG," setVoice language : " + language + " voiceName : " + voiceName);
        Voice v = new Voice(language, voiceName, male ? Voice.Gender.Male : Voice.Gender.Female, isServiceVoice);
        if (m_syn != null) {
            m_syn.setVoice(v, null);
        }
    }

    @Override
    public void speak(String text,String name,String path) {
        if (m_syn != null) {
            m_syn.speakToAudio(text, name, path, mListener,new Runnable() {
                @Override
                public void run() {
                    //Logger.d(TAG,"speak finished");
                    //mListener.onTTSEvent(MSConstants.STATE_SPEAK_SUCCESS,null);
                }
            });
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void stop() {
        if (m_syn != null) {
            m_syn.stopSound();
        }
    }

    @Override
    public void release() {
        if (m_syn != null) {
            m_syn.release();
        }
        if (m_syn != null) {
            m_syn.stopSound();
        }
    }

    @Override
    public String synthesizeToUri(String text, String fileName, String filePath) {
        if (m_syn != null) {
            return m_syn.synthesizeToUri(text, fileName, filePath,mListener);
        }
        return null;
    }

    @Override
    public byte[] getSpeak(String text) {
        if (m_syn != null) {
            return m_syn.speak(text);
        }
        return null;
    }

    @Override
    public void setTTSEventListener(ITTSListener ittsListener) {
        this.mListener = ittsListener;
    }

}
