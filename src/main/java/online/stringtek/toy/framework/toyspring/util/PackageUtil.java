package online.stringtek.toy.framework.toyspring.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class PackageUtil {
    /**
     *
     * @param path 文件或路径
     * @param classPath 类路径
     * @return 扫描出来的路径下的所有的Class文件集合
     */
    public static List<Class<?>> scan(File path,String classPath) throws ClassNotFoundException {
        List<Class<?>> classFileList=new ArrayList<>();
        if(path.isDirectory()){//是目录
            File[] files = path.listFiles();
            if(files!=null){
                for (File file : files) {
                    //file可能是文件也可能是目录
                    classFileList.addAll(scan(file,classPath+"."+file.getName()));
                }
            }

        }else {//是文件
            if(classPath.endsWith(".class")){
                Class<?> clazz = Class.forName(classPath.substring(0,classPath.length()-6));
                classFileList.add(clazz);
            }
        }
        return classFileList;
    }
}
