package com.liyeyu.novstory.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import com.liyeyu.novstory.Constants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 错误日记处理
 * Created by Liyeyu on 2016/6/29.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static CrashHandler myCrashHandler = null;

    private Thread.UncaughtExceptionHandler mDefaultHandler;

    private Context mContext = null;

    private SimpleDateFormat dataFormat = null;
    private static final String ERROR_PATH = Constants.LOCAL_PATH;

    private CrashHandler(Context context) {
        initConfig(context);
    }

    public static CrashHandler init(Context context) {
        if (myCrashHandler != null) {
            synchronized (CrashHandler.class) {
                if (myCrashHandler != null) {
                    myCrashHandler = new CrashHandler(context);
                }
            }
        }
        return myCrashHandler;
    }

    public void initConfig(Context context) {
        this.mContext = context;
        dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /* 当UncaughtException发生时会转入该函数来处理 */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        ex.printStackTrace();
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            // 如果自己处理了异常，则不会弹出错误对话框，则需要手动退出app
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
//		CallManager.getInstance().logout();
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 开发者可以根据自己的情况来自定义异常处理逻辑
     *
     * @return true代表处理该异常，不再向上抛异常，
     * false代表不处理该异常(可以将该log信息存储起来)然后交给上层(这里就到了系统的异常处理)去处理，
     * 简单来说就是true不会弹出那个错误提示框，false就会弹出
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        Toast.makeText(mContext, "抱歉,程序出现异常,我们会继续改进!", Toast.LENGTH_SHORT)
                .show();
        // 1.获取当前程序的版本号. 版本的id
        final String versioninfo = getVersionInfo();
        // 2.获取手机的硬件信息.
        final String mobileInfo = getMobileInfo();
        // 3.把错误的堆栈信息 获取出来
        final String errorinfo = getErrorInfo(ex);
        // 使用Toast来显示异常信息
        // 4.把所有的信息 还有信息对应的时间 提交到服务器
        /* 保存到本地 */

        WriteErrorFileData(ERROR_PATH, composeErrors(
                dataFormat.format(new Date()), versioninfo, mobileInfo,
                errorinfo));
        return false;
    }

    /* 合成错误信息 */
    private String composeErrors(String errorTime, String errorVersionMsg,
                                 String errorMobileMsg, String errorMsg) {
        StringBuilder sb = new StringBuilder();
        sb.append("\r\n*********一条崩溃信息了!!!********\r\n");
        sb.append("发生的时间:");
        sb.append(errorTime);
        sb.append("\r\n");
        sb.append("错误版本名:");
        sb.append(errorVersionMsg);
        sb.append("\r\n");
        sb.append("机型信息:");
        sb.append(errorMobileMsg);
        sb.append("\r\n");
        sb.append("错误日志:");
        sb.append(errorMsg);
        sb.append("\r\n");
        return sb.toString();
    }

    /**
     * 获取错误的信息
     *
     * @param arg1
     * @return
     */
    private String getErrorInfo(Throwable arg1) {
        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        arg1.printStackTrace(pw);
        pw.close();
        String error = writer.toString();
        return error;
    }

    /**
     * 获取版本信息
     *
     * @return
     */
    private String getVersionInfo() {
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo info = pm.getPackageInfo(mContext.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "版本号未知";
        }
    }

    /**
     * 获取手机的硬件信息
     *
     * @return
     */
    private String getMobileInfo() {
        StringBuffer sb = new StringBuffer();
        // 通过反射获取系统的硬件信息
        try {
            Field[] fields = Build.class.getDeclaredFields();
            for (Field field : fields) {
                // 暴力反射 ,获取私有的信息
                field.setAccessible(true);
                String name = field.getName();
                String value = field.get(null).toString();
                sb.append(name + "=" + value);
                sb.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /* 写错误日志到一个新的文件中 */
    public static void WriteErrorFileData(String path, String content) {

        String errorLogFilePath = path;
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String datetime = dataFormat.format(new Date());

        FileWriter writer = null;
        try {
            File file = new File(errorLogFilePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            writer = new FileWriter(errorLogFilePath + datetime + ".txt");
            writer.write(content);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                    writer = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
			/* 写完后关闭应用 */
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }
}