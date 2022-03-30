package com.dc.common.util;

/**
 * todo
 *
 * @author Carl Don
 * @Date 2022/1/28  16:16
 * @Version 1.0
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class weChatImgRevert {

    public static void main(String[] args) {
        String path = "C:\\Users\\Administrator\\Documents\\project\\DMC\\common\\qwert";
        String targetPath = "C:\\Users\\Administrator\\Documents\\project\\DMC\\common\\out\\";
        convert(path, targetPath);
    }

    /**
     * @param path       图片目录地址
     * @param targetPath 转换后目录
     */
    private static void convert(String path, String targetPath) {
        File[] file = new File(path).listFiles();
        if (file == null) {
            return;
        }
        int size = file.length;
        System.out.println("总共" + size + "个文件");
        AtomicReference<Integer> integer = new AtomicReference<>(0);
        AtomicInteger x = new AtomicInteger();
        x.set((int) getXor(file[0])[1]);
        Arrays.stream(file).parallel().forEach(file1 -> {
            Object[] xor = getXor(file1);
            try (InputStream reader = new FileInputStream(file1);
                 OutputStream writer =
                         new FileOutputStream(targetPath + file1.getName().split("\\.")[0] + "." + xor[0])) {
                byte[] bytes = new byte[1024 * 10];
                int b;
                while ((b = reader.read(bytes)) != -1) {//这里的in.read(bytes);就是把输入流中的东西，写入到内存中（bytes）。
                    for (int i = 0; i < bytes.length; i++) {
                        bytes[i] = (byte) (int)(bytes[i] ^ ((int) xor[1] == 0 ? x.get() : (int) xor[1]));
                        if (i == (b - 1)) {
                            break;
                        }
                    }
                    writer.write(bytes, 0, b);
                    writer.flush();
                }
                integer.set(integer.get() + 1);
                System.out.println(file1.getName() + "(大小:" + ((double) file1.length() / 1000) + "kb,异或值:" + xor[1] + ")," +
                        "进度：" + integer.get() +
                        "/" + size);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.out.println("解析完毕！");
    }

    /**
     * 判断特征码所属异或值
     *
     * @param file
     * @return
     */
    private static Object[] getXor(File file) {
        String index = "jpg";
        int xori = 0;
        if (file != null) {
            xori = getJPEGXor(file);
            if (xori == 0) {
                xori = getPNGXor(file);
                index = "png";
            }
            if (xori == 0) {
                xori = getBMPXor(file);
                index = "bmp";
            }
            if (xori == 0) {
                xori = getGIFXor(file);
                index = "gif";
            }
            if (xori == 0) {
                xori = getICOXor(file);
                index = "ico";
            }
            if (xori == 0) {
                index = "jpg";
            }
        }
        return new Object[]{index, xori};
    }

    /**
     * @param file
     * @return
     */
    private static Integer getJPEGXor(File file) {
        int[] xors = new int[4];
        try (InputStream reader = new FileInputStream(file)) {
            xors[0] = reader.read() ^ 0xFF;
            xors[1] = reader.read() ^ 0xD8;
           /* reader.skip(file.length() - 4);
            xors[2] = reader.read() ^ 0xDA;
            xors[3] = reader.read() ^ 0x03;*/
            System.out.println(reader.read());
            System.out.println(xors[0]);
            System.out.println(xors[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (xors[0] == xors[1]/** && xors[1] == xors[2] && xors[2] == xors[3]**/) {
            return xors[0];
        } else {
            return 0;
        }
    }

    /**
     * @param file
     * @return
     */
    private static Integer getPNGXor(File file) {
        int[] xors = new int[4];
        try (InputStream reader = new FileInputStream(file)) {
            xors[0] = reader.read() ^ 0x89;
            xors[1] = reader.read() ^ 0x50;
            xors[2] = reader.read() ^ 0x4E;
            xors[3] = reader.read() ^ 0x47;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (xors[0] == xors[1] && xors[1] == xors[2] && xors[2] == xors[3]) {
            return xors[0];
        } else {
            return 0;
        }
    }

    /**
     * @param file
     * @return
     */
    private static Integer getGIFXor(File file) {
        int[] xors = new int[4];
        try (InputStream reader = new FileInputStream(file)) {
            xors[0] = reader.read() ^ 0x47;
            xors[1] = reader.read() ^ 0x49;
            xors[2] = reader.read() ^ 0x46;
            xors[3] = reader.read() ^ 0x38;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (xors[0] == xors[1] && xors[1] == xors[2] && xors[2] == xors[3]) {
            return xors[0];
        } else {
            return 0;
        }
    }

    /**
     * @param file
     * @return
     */
    private static Integer getBMPXor(File file) {
        int[] xors = new int[2];
        try (InputStream reader = new FileInputStream(file)) {
            xors[0] = reader.read() ^ 0x42;
            xors[1] = reader.read() ^ 0x4D;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (xors[0] == xors[1]) {
            return xors[0];
        } else {
            return 0;
        }
    }

    /**
     * @param file
     * @return
     */
    private static Integer getICOXor(File file) {
        int[] xors = new int[4];
        try (InputStream reader = new FileInputStream(file)) {
            xors[0] = reader.read();
            xors[1] = reader.read();
            xors[2] = reader.read() ^ 0x01;
            xors[3] = reader.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (xors[0] == xors[1] && xors[1] == xors[2] && xors[2] == xors[3]) {
            return xors[0];
        } else {
            return 0;
        }
    }
}
