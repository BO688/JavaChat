package com.company.Utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.util.Base64;

/**
 * Base64 工具类
 *
 */
public class Base64Util {
    private static final char last2byte = (char) Integer.parseInt("00000011", 2);
    private static final char last4byte = (char) Integer.parseInt("00001111", 2);
    private static final char last6byte = (char) Integer.parseInt("00111111", 2);
    private static final char lead6byte = (char) Integer.parseInt("11111100", 2);
    private static final char lead4byte = (char) Integer.parseInt("11110000", 2);
    private static final char lead2byte = (char) Integer.parseInt("11000000", 2);
    private static final char[] encodeTable = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};

    public Base64Util() {
    }
/*加密base64 从3变到4MB*/
    public static String encode(byte[] from) {/*获得图片字节*/
        StringBuilder to = new StringBuilder((int) ((double) from.length * 1.34D) + 3);
        int num = 0;
        char currentByte = 0;

        int i;
        for (i = 0; i < from.length; ++i) {
            for (num %= 8; num < 8; num += 6) {
                switch (num) {
                    case 0:
                        currentByte = (char) (from[i] & lead6byte);
                        currentByte = (char) (currentByte >>> 2);
                    case 1:
                    case 3:
                    case 5:
                    default:
                        break;
                    case 2:
                        currentByte = (char) (from[i] & last6byte);
                        break;
                    case 4:
                        currentByte = (char) (from[i] & last4byte);
                        currentByte = (char) (currentByte << 2);
                        if (i + 1 < from.length) {
                            currentByte = (char) (currentByte | (from[i + 1] & lead2byte) >>> 6);
                        }
                        break;
                    case 6:
                        currentByte = (char) (from[i] & last2byte);
                        currentByte = (char) (currentByte << 4);
                        if (i + 1 < from.length) {
                            currentByte = (char) (currentByte | (from[i + 1] & lead4byte) >>> 4);
                        }
                }

                to.append(encodeTable[currentByte]);
            }
        }

        if (to.length() % 4 != 0) {
            for (i = 4 - to.length() % 4; i > 0; --i) {
                to.append("=");
            }
        }

        return to.toString();
    }
    public static boolean generateImage(String imgStr, String path) {/*传入图片和保存路径，转化成base64*/
        if (imgStr == null)return false;
//        BASE64Decoder decoder = new BASE64Decoder();//这里把1.8转成12JDK后期
        Base64.Decoder decoder= Base64.getDecoder();
        try {
// 解密
            byte[] b = decoder.decode(imgStr);
            // 处理数据
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            OutputStream out = new FileOutputStream(path);
            out.write(b);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    /**
     * 将文件转成base64 字符串
     * @param path
     * @return  *
     * @throws Exception
     */

    public static Base64File encodeBase64File(String path) throws Exception {
        Base64File base64File=new Base64File();
        File file = new File(path);
        base64File.setFileName(file.getName());
        FileInputStream inputFile = new FileInputStream(file);
        byte[] buffer = new byte[(int) file.length()];
        inputFile.read(buffer);
        inputFile.close();
        base64File.setBase64(new BASE64Encoder().encode(buffer));
        return base64File;
    }
    /**
     * 将base64字符解码保存文件
     * @param base64File
     * @param targetDir
     * @throws Exception
     */

    public static void decoderBase64File(Base64File base64File, String targetDir)
            throws Exception {
        byte[] buffer = new BASE64Decoder().decodeBuffer(base64File.getBase64());
        FileOutputStream out = new FileOutputStream(targetDir+ File.separator+base64File.getFileName());
        out.write(buffer);
        out.close();

    }
    public static void decoderBase64File(Base64File base64File,File target)
            throws Exception {
        byte[] buffer = new BASE64Decoder().decodeBuffer(base64File.getBase64());
        FileOutputStream out = new FileOutputStream(target);
        out.write(buffer);
        out.close();

    }
    public static void decoderBase64File(String base64,File target)
            throws Exception {
        byte[] buffer = new BASE64Decoder().decodeBuffer(base64);
        FileOutputStream out = new FileOutputStream(target);
        out.write(buffer);
        out.close();

    }

//    public static void main(String[] args) throws IOException {
//        generateImage(encode(FileUtil.readFileByBytes("test.jpg")),"testt.jpg");
//    }
}
