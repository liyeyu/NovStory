package com.liyeyu.novstory.play;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.KeyEvent;

import liyeyu.support.utils.utils.LogUtil;

/**
 * 线控设备按钮监听
 * Created by Liyeyu on 2016/7/25.
 */
public class RemoteControlReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            int action = event.getAction();
            if(action==KeyEvent.ACTION_UP){
                if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == event.getKeyCode()
                        ||KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
                    NovPlayController.get().play();
                }else if(KeyEvent.KEYCODE_MEDIA_NEXT == event.getKeyCode()){
                    NovPlayController.get().onSkipToPrevious();
                }else if(KeyEvent.KEYCODE_MEDIA_PREVIOUS == event.getKeyCode()){
                    NovPlayController.get().onSkipToPrevious();
                }
            }
            LogUtil.i("RemoteControlReceiver:"+event);
        }else if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
            // 耳机拔出或蓝牙中断，应暂停播放
            NovPlayController.get().pause();
        }
    }
}
