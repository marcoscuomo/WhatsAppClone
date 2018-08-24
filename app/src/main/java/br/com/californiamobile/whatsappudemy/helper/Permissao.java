package br.com.californiamobile.whatsappudemy.helper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityManagerCompat;
import android.support.v4.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class Permissao {

    public static boolean validarPermissoes(String[] permissoes, Activity activity, int requestCode){

        //Verificar se a versão do android do usuario é igual ou superior a Marshmellow, API 23
        if(Build.VERSION.SDK_INT >= 23){
            List<String> listaPermissoes = new ArrayList<>();

            /*
            * Percorrer as permissoes passas
            * verificando uma a uma
            * se ja tem permissao liberada
            * */
            for (String permissao :
                    permissoes) {
                Boolean temPermissao = ContextCompat.checkSelfPermission(activity, permissao) ==
                        PackageManager.PERMISSION_GRANTED;
                if(!temPermissao)
                    listaPermissoes.add(permissao);
            }
            //Caso a lista esteja vazia nao eh necessario solicitar permissoes
            if(listaPermissoes.isEmpty()) return true;

            String[] novasPermissoes = new String[listaPermissoes.size()];
            listaPermissoes.toArray(novasPermissoes);
            //Solicita Permissao
            ActivityCompat.requestPermissions(activity, novasPermissoes, requestCode);



        }
        return true;
    }

}
