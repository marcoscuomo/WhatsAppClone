package br.com.californiamobile.whatsappudemy.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import br.com.californiamobile.whatsappudemy.R;
import br.com.californiamobile.whatsappudemy.ui.adapter.AdapterGrupoSelecionado;
import br.com.californiamobile.whatsappudemy.config.ConfiguracaoFirebase;
import br.com.californiamobile.whatsappudemy.helper.Common;
import br.com.californiamobile.whatsappudemy.helper.UsuarioFirebase;
import br.com.californiamobile.whatsappudemy.model.Grupo;
import br.com.californiamobile.whatsappudemy.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class CadastroGrupoActivity extends AppCompatActivity {

    //Atributos
    private List<Usuario> listaMembrosSelecionados = new ArrayList<>();
    private TextView txtTotalParticipantes;
    private AdapterGrupoSelecionado adapterGrupoSelecionado;
    private RecyclerView recyclerMembrosSelecionados;
    private CircleImageView imageGrupo;
    private StorageReference storageReference;
    private Grupo grupo;
    private FloatingActionButton fabSalvarGrupo;
    private EditText editNomeGrupo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_grupo);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo Grupo");
        toolbar.setSubtitle("Defina o nome");
        setSupportActionBar(toolbar);


        //Inicializações
        txtTotalParticipantes = findViewById(R.id.txtTotalParticipantes);
        recyclerMembrosSelecionados = findViewById(R.id.recyclerMembrosGrupo);
        imageGrupo = findViewById(R.id.imageGrupo);
        grupo = new Grupo();
        fabSalvarGrupo = findViewById(R.id.fabSalvarGrupo);
        editNomeGrupo = findViewById(R.id.editNomeGrupo);

        //Storange Firebase
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();

        //Evendo de clique na imagem do grupo
        imageGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(i.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(i, Common.REQUEST_CODE_GALERIA);
                }
            }
        });


        //Recuperar lista de membros passada
        if(getIntent().getExtras() != null){
            List<Usuario> listaMembros = (List<Usuario>) getIntent().getExtras().getSerializable("membros");
            listaMembrosSelecionados.addAll(listaMembros);
            txtTotalParticipantes.setText("Participantes: " + listaMembrosSelecionados.size());
        }

        //Configurar o RecyclerView
        adapterGrupoSelecionado =
                new AdapterGrupoSelecionado(listaMembrosSelecionados, getApplicationContext());
        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        );
        recyclerMembrosSelecionados.setLayoutManager(layoutManagerHorizontal);
        recyclerMembrosSelecionados.setHasFixedSize(true);
        recyclerMembrosSelecionados.setAdapter(adapterGrupoSelecionado);

        //Configuracao do FAB
        fabSalvarGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nomeGrupo = editNomeGrupo.getText().toString();

                //adiciona a lista de mebros o usuario que esta logado
                listaMembrosSelecionados.add(UsuarioFirebase.getDadosUsuarioLogado());

                grupo.setMembros(listaMembrosSelecionados);
                grupo.setNome(nomeGrupo);

                grupo.salvar();

                Intent i = new Intent(CadastroGrupoActivity.this, ChatActivity.class);
                i.putExtra("chatGrupo", grupo);
                startActivity(i);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            Bitmap imagem = null;

            try{
                Uri localImagemSelecionada = data.getData();
                imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);

                if(imagem != null){
                    imageGrupo.setImageBitmap(imagem);

                    //Recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //Salvar imagem no firebase
                    StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child("grupos")
                            .child(grupo.getId() + ".jpeg");

                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(CadastroGrupoActivity.this,
                                    "Erro ao fazer o Upload", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(CadastroGrupoActivity.this,
                                    "Sucesso ao fazer o Upload", Toast.LENGTH_SHORT).show();

                            String url = taskSnapshot.getDownloadUrl().toString();

                            grupo.setFoto(url);


                        }
                    });
                }

            }catch (Exception e){

            }
        }
    }
}
