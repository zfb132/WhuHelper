package sign;

import org.apache.commons.codec.binary.Hex;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


import java.math.BigInteger;

/**
 * Created by zfb15 on 2017/10/7.
 */


public class WebURL {
    public static final String LOGINURL_NETEASE="http://music.163.com/weapi/login/";

    public static void loginAPI(String username, String password) throws Exception {
        password = EncryptTool.md5(password);
        // 私钥，随机16位字符串（自己可改）
        String secKey = "cd859f54539b24b7";
        String text = "{\"username\": \"" + username + "\", \"rememberLogin\": \"true\", \"password\": \"" + password
                + "\"}";
        String modulus = "00e0b509f6259df8642dbc35662901477df22677ec152b5ff68ace615bb7b725152b3ab17a876aea8a5aa76d2e417629ec4ee341f56135fccf695280104e0312ecbda92557c93870114af6c9d05c4f7f0c3685b7a46bee255932575cce10b424d813cfe4875d3e82047b97ddef52741d546b8e289dc6935b3ece0462db0a22b8e7";
        String nonce = "0CoJUm6Qyw8W8jud";
        String pubKey = "010001";
        // 2次AES加密，得到params
        String params = EncryptTool.encrypt(EncryptTool.encrypt(text, nonce), secKey);

        /*
        StringBuffer stringBuffer = new StringBuffer(secKey);
        // 逆置私钥
        secKey = stringBuffer.reverse().toString();
        String hex = Hex.encodeHex(secKey.getBytes()).toString();
        BigInteger bigInteger1 = new BigInteger(hex, 16);
        BigInteger bigInteger2 = new BigInteger(pubKey, 16);
        BigInteger bigInteger3 = new BigInteger(modulus, 16);
        // RSA加密计算
        BigInteger bigInteger4 = bigInteger1.pow(bigInteger2.intValue()).remainder(bigInteger3);
        String encSecKey = Hex.encodeHex(bigInteger4.toByteArray()).toString();
        // 字符填充
        encSecKey = EncryptTool.zfill(encSecKey, 256);
        */
        // 登录请求
        String encSecKey="257348aecb5e556c066de214e531faadd1c55d814f9be95fd06d6bff9f4c7a41f831f6394d5a3fd2e3881736d94a02ca919d952872e7d0a50ebfa1769a7a62d512f5f1ca21aec60bc3819a9c3ffca5eca9a0dba6d6f7249b06f5965ecfff3695b54e1c28f3f624750ed39e7de08fc8493242e26dbc4484a01c76f739e135637c";
        Document document = Jsoup.connect("http://music.163.com/weapi/login/").cookie("appver", "1.5.0.75771")
                .header("Referer", "http://music.163.com/").data("params", params).data("encSecKey", encSecKey)
                .ignoreContentType(true).post();
        System.out.println("登录结果：" + document.text());
    }



}
