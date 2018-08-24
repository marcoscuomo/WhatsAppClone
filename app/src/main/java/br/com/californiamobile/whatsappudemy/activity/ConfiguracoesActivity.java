package br.com.californiamobile.whatsappudemy.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import br.com.californiamobile.whatsappudemy.R;
import br.com.californiamobile.whatsappudemy.config.ConfiguracaoFirebase;
import br.com.californiamobile.whatsappudemy.helper.Common;
import br.com.californiamobile.whatsappudemy.helper.Permissao;
import br.com.californiamobile.whatsappudemy.helper.UsuarioFirebase;
import br.com.californiamobile.whatsappudemy.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class ConfiguracoesActivity extends AppCompatActivity {

    //Atributos
    private ImageButton imgBtnCamera, imgBtnGaleria;
    private CircleImageView circleImageViewPerfil;
    private StorageReference storageReference;
    private String identificadorUsuario;
    private EditText edtNome;
    private ImageView btnEditNome;
    private Usuario usuarioLogado;


    //Array para solicitar a autorização do usuario
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);


        //Validar Permissões
        Permissao.validarPermissoes(permissoesNecessarias, this, 1);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Configurações");
        setSupportActionBar(toolbar);

        //Inicializações FindViewById
        imgBtnCamera = findViewById(R.id.imgBtnCamera);
        imgBtnGaleria = findViewById(R.id.imgBtnFoto);
        circleImageViewPerfil = findViewById(R.id.contatos_imgContato);
        edtNome = findViewById(R.id.config_edtNome);
        btnEditNome = findViewById(R.id.config_btnEdit);

        //Exibindo o botao voltar na toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Recuperar Dados usuario
        FirebaseUser usuario = UsuarioFirebase.getUsuarioAtual();
        Uri url = usuario.getPhotoUrl();

        //Verificando se a url não é nula
        if(url != null){
            Glide.with(ConfiguracoesActivity.this)
                    .load(url)
                    .into(circleImageViewPerfil);
        }else{
            circleImageViewPerfil.setImageResource(R.drawable.padrao);
        }

        //Colocando o nome do usuario, definido no cadastro, no editText
        edtNome.setText(usuario.getDisplayName());


        //Configuracoes iniciais
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        //Clique no botao Camera
        imgBtnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(i.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(i, Common.REQUEST_CODE_CAMERA);
                }
            }
        });

        //Clique no botao Galeria
        imgBtnGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(i.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(i, Common.REQUEST_CODE_GALERIA);
                }
            }
        });

        //Evento no botao de edição do nome
        btnEditNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nomeEditado = edtNome.getText().toString();
                boolean retorno = UsuarioFirebase.atualizarNomeUsuario(nomeEditado);
                if(retorno){

                    usuarioLogado.setNome(nomeEditado);
                    usuarioLogado.atualiza();

                    Toast.makeText(ConfiguracoesActivity.this,
                            "Nome atualizado com sucesso", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            Bitmap imagem = null;


            try{

                switch (requestCode){
                    case Common.REQUEST_CODE_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;
                    case Common.REQUEST_CODE_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);
                        break;
                }

                if(imagem != null){
                    circleImageViewPerfil.setImageBitmap(imagem);

                    //Recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //Salvar imagem no firebase
                    StorageReference imagemRef = storageReference.child("imagens")
                            .child("perfil")
                            //.child(identificadorUsuario)
                            .child(identificadorUsuario + ".jpeg");
                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(ConfiguracoesActivity.this,
                                    "Erro ao fazer o Upload", Toast.LENGTH_SHORT).show();
        
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(ConfiguracoesActivity.this,
                                    "Sucesso ao fazer o Upload", Toast.LENGTH_SHORT).show();

                            Uri url = taskSnapshot.getDownloadUrl();
                            atualizaFotoUsuario(url);

                        }
                    });
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    private void atualizaFotoUsuario(Uri url) {
        boolean retorno = UsuarioFirebase.atualizarFotoUsuario(url);
        if(retorno){
            usuarioLogado.setFoto(url.toString());
            usuarioLogado.atualiza();

            Toast.makeText(this, "Foto atualizada com sucesso", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado :
                grantResults) {
            if (permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao() {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Permissões negadas");
        alert.setMessage("Para utilizar o App é necessário aceitar as permissões");
        alert.setCancelable(false);
        alert.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {
                 finish();
             }
         });
    }
}
