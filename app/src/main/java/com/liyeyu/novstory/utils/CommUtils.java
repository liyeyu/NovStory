package com.liyeyu.novstory.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Liyeyu on 2016/8/23.
 */
public class CommUtils {

    public static void writeFile(String path,String content){
        File f =new File(path);//新建一个文件对象
        FileWriter fw;
        try {
            fw = new FileWriter(f);//新建一个FileWriter
            fw.write(content);//将字符串写入到指定的路径下的文件中
            fw.flush();
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
