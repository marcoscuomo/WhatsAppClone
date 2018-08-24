package br.com.californiamobile.whatsappudemy.helper;

import android.util.Base64;


// codificarBase64 decodificarBase64
public class Base64Custom {

    public static String codificarBase64 (String texto){
        return Base64.encodeToString(texto.getBytes(),
                Base64.DEFAULT).replaceAll("(\\n|\\r)","");
    }

    public static String decodificarBase64Custom(String textoCodificado){

        return new String(Base64.decode(textoCodificado, Base64.DEFAULT));

    }

}
