package com.liyeyu.novstory.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import com.liyeyu.novstory.Constants;
import com.liyeyu.novstory.entry.Audio;
import com.liyeyu.novstory.manager.AppConfig;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Liyeyu on 2016/7/22.
 */
public class MediaUtils {
    //获取专辑封面的Uri
    private static final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");

    public static final String[] AUDIO_KEYS = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.TITLE_KEY,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.ARTIST_KEY,
            MediaStore.Audio.Media.COMPOSER,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM_KEY,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.IS_RINGTONE,
            MediaStore.Audio.Media.IS_PODCAST,
            MediaStore.Audio.Media.IS_ALARM,
            MediaStore.Audio.Media.IS_MUSIC,
            MediaStore.Audio.Media.IS_NOTIFICATION,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATA
    };

    public static List<Audio> getAllAudioList(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                AUDIO_KEYS,
                MediaStore.Audio.Media.DURATION + "> ? ",
                new String[]{AppConfig.get(Constants.SETTING_DURATION, String.valueOf(30 * 1000))},
                null);
        return getAudioList(cursor);
    }

    public static List<Audio> getAudioListLimit (Context context,int limit,int page) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                AUDIO_KEYS,
                MediaStore.Audio.Media.DURATION + "> ? ",
                new String[]{AppConfig.get(Constants.SETTING_DURATION, String.valueOf(30 * 1000))},
                MediaStore.Audio.Media._ID +" limit "+ limit + " offset " + page);

        return getAudioList(cursor);
    }

    private static List<Audio> getAudioList(Cursor cursor) {
        List<Audio> audioList = new ArrayList<>();
        if(cursor==null){
            return audioList;
        }
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Bundle bundle = new Bundle();
            for (int i = 0; i < AUDIO_KEYS.length; i++) {
                final String key = AUDIO_KEYS[i];
                final int columnIndex = cursor.getColumnIndex(key);
                final int type = cursor.getType(columnIndex);
                switch (type) {
                    case Cursor.FIELD_TYPE_BLOB:
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        float floatValue = cursor.getFloat(columnIndex);
                        bundle.putFloat(key, floatValue);
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        int intValue = cursor.getInt(columnIndex);
                        bundle.putInt(key, intValue);
                        break;
                    case Cursor.FIELD_TYPE_NULL:
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        String strValue = cursor.getString(columnIndex);
                        bundle.putString(key, strValue);
                        break;
                }
            }
            Audio audio = new Audio(bundle);
            audioList.add(audio);
        }
        cursor.close();
        return audioList;
    }

    public static Bitmap getDefaultArtwork(String filePath) {
        Bitmap albumArt = createAlbumArt(filePath);
        if(albumArt==null){
            albumArt = ImageLoader.get().getDefBitmap();
        }
        return albumArt;
    }

    public static Bitmap getArtwork(Context context, long songId, long albumId) {
        Bitmap bitmap = getArtworkFromFile(context, songId, albumId);
        if(bitmap==null){
            bitmap = ImageLoader.get().getDefBitmap();
        }
        return bitmap;
    }

    /**
     * 从文件当中获取专辑封面位图
     *
     * @param songid
     * @param albumid
     * @return
     */
    private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
        Bitmap bm = null;
        if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            FileDescriptor fd = null;
            if (albumid < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/"
                        + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            } else {
                Uri uri = ContentUris.withAppendedId(albumArtUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            }
            options.inSampleSize = 1;
            // 只进行大小判断
            options.inJustDecodeBounds = true;
            // 调用此方法得到options得到图片大小
            BitmapFactory.decodeFileDescriptor(fd, null, options);
            // 我们的目标是在800pixel的画面上显示
            // 所以需要调用computeSampleSize得到图片缩放的比例
            // 我们得到了缩放的比例，现在开始正式读入Bitmap数据
            options.inSampleSize = ImageLoader.calculateInSampleSize(options,ImageLoader.DEF_W,ImageLoader.DEF_H);
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inJustDecodeBounds = false;
            //根据options参数，减少所需要的内存
            bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
        } catch (FileNotFoundException e) {
//            e.printStackTrace();
        }
        return bm;
    }

    public static Uri getArtworkUri(Context context, long songid, long albumid) {
        if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
        Uri uri;
        if (albumid < 0) {
            uri = Uri.parse("content://media/external/audio/media/"
                    + songid + "/albumart");
        } else {
            uri = ContentUris.withAppendedId(albumArtUri, albumid);
        }
        return uri;
    }

    /**
     * @Description 获取专辑封面
     * @param filePath 文件路径，like XXX/XXX/XX.mp3
     * @return 专辑封面bitmap
     */
    public static Bitmap createAlbumArt(final String filePath) {
        Bitmap bitmap = null;
        //能够获取多媒体文件元数据的类
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath); //设置数据源
            byte[] embedPic = retriever.getEmbeddedPicture(); //得到字节型数据
            if(embedPic==null){
                return bitmap;
            }
            BitmapFactory.Options ops = new BitmapFactory.Options();
            ops.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(embedPic, 0, embedPic.length,ops); //转换为图片
            ops.inSampleSize = ImageLoader.calculateInSampleSize(ops,ImageLoader.DEF_W,ImageLoader.DEF_H);
            ops.inPreferredConfig = Bitmap.Config.RGB_565;
            ops.inPurgeable = true;
            ops.inInputShareable = true;
            ops.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeByteArray(embedPic, 0, embedPic.length,ops);
//            bitmap = ThumbnailUtils.extractThumbnail(bitmap, 50, 50,
//                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return bitmap;
    }

}
