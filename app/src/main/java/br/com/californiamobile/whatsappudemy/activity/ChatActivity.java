package br.com.californiamobile.whatsappudemy.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.californiamobile.whatsappudemy.R;
import br.com.californiamobile.whatsappudemy.adapter.AdapterChat;
import br.com.californiamobile.whatsappudemy.config.ConfiguracaoFirebase;
import br.com.californiamobile.whatsappudemy.helper.Base64Custom;
import br.com.californiamobile.whatsappudemy.helper.Common;
import br.com.californiamobile.whatsappudemy.helper.UsuarioFirebase;
import br.com.californiamobile.whatsappudemy.model.Conversa;
import br.com.californiamobile.whatsappudemy.model.Grupo;
import br.com.californiamobile.whatsappudemy.model.Mensagem;
import br.com.californiamobile.whatsappudemy.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    //Atributos
    private TextView txtNome;
    private CircleImageView circleImageView;
    private Usuario destinatario;
    private EditText edtMsg;
    private RecyclerView recyclerViewMensagens;
    private AdapterChat adapter;
    private List<Mensagem> listaMensagem = new ArrayList<>();
    private DatabaseReference database;
    private DatabaseReference mensagensRef;
    private ChildEventListener childEventListenerMensagens;
    private ImageView imgCamera;
    private StorageReference storage;
    private Grupo grupo;

    //Identificar usuarios remetente e destinatario
    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Recuperando dados do usuario remetente
        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();


        //Inicializações
        txtNome               = findViewById(R.id.chat_txtNome);
        circleImageView       = findViewById(R.id.chat_circleImageFoto);
        edtMsg                = findViewById(R.id.chat_editMsg);
        recyclerViewMensagens = findViewById(R.id.recyclerMensagens);
        imgCamera             = findViewById(R.id.imgCamera);


        //Recuperar dados do usuario pela intent
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            //Verificando se é um chat de grupo ou normal
            if(bundle.containsKey("chatGrupo")){

                grupo = (Grupo) bundle.getSerializable("chatGrupo");
                idUsuarioDestinatario = grupo.getId();
                txtNome.setText(grupo.getNome());

                String foto = grupo.getFoto();
                if(foto != null){
                    Uri url = Uri.parse(foto);
                    Glide.with(ChatActivity.this).load(url).into(circleImageView);
                }else {
                    circleImageView.setImageResource(R.drawable.padrao);
                }

            }else{

                /*INICIO - Conversa convernsional. Diferente de grupo*/
                destinatario = (Usuario) bundle.getSerializable("chatContato");
                txtNome.setText(destinatario.getNome());

                String foto = destinatario.getFoto();
                if(foto != null){
                    Uri url = Uri.parse(destinatario.getFoto());
                    Glide.with(ChatActivity.this).load(url).into(circleImageView);
                }else {
                    circleImageView.setImageResource(R.drawable.padrao);
                }

                //Recuperar dados do usuario destinatario
                idUsuarioDestinatario = Base64Custom.codificarBase64(destinatario.getEmail());

                /*FIM - Conversa convernsional. Diferente de grupo*/

            }


        }

        /*Recycler*/
        //Adapter
        adapter = new AdapterChat(listaMensagem, ChatActivity.this);

        //Recycler
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ChatActivity.this);
        recyclerViewMensagens.setLayoutManager(layoutManager);
        recyclerViewMensagens.setHasFixedSize(true);
        recyclerViewMensagens.setAdapter(adapter);

        //Configurando  recuperar mensagens
        database = ConfiguracaoFirebase.getFirebaseDatabase();
        storage  = ConfiguracaoFirebase.getFirebaseStorage();
        mensagensRef = database.child("mensagens")
                .child(idUsuarioRemetente)
                .child(idUsuarioDestinatario);

        //Criancao a ação no botão de enviar foto
        imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(i.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(i, Common.REQUEST_CODE_CAMERA);
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
                if(requestCode == Common.REQUEST_CODE_CAMERA){
                    imagem = (Bitmap) data.getExtras().get("data");
                }

                if(imagem != null){

                    //Recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //Criando o nome da imagem
                    String nomeImgagem = UUID.randomUUID().toString();


                    //Salvar imagem no Firebase
                     StorageReference imagemRef = storage
                             .child("imagens")
                             .child("fotos")
                             .child(idUsuarioRemetente)
                             //.child(nomeImgagem + ".jpeg");
                             .child(nomeImgagem);

                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ChatActivity.this,
                                    "Erro ao fazer o upload da imagem", Toast.LENGTH_SHORT).show();
                            Log.d("Erro", "Erro ao fazer o upload ");
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            //Recuperando a url da imagem
                            String url = taskSnapshot.getDownloadUrl().toString();

                            //Enviando a urm da imagem
                            Mensagem mensagem = new Mensagem();
                            mensagem.setIdUsuario(idUsuarioRemetente);
                            mensagem.setMensagem("imagem.jpeg");
                            mensagem.setImagem(url);

                            //Salvar para o remetente
                            salvarMsg(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                            //Salvar para o destinatario
                            salvarMsg(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                            Toast.makeText(ChatActivity.this,
                                    "Sucesso ao fazer o upload da imagem", Toast.LENGTH_SHORT).show();


                        }
                    });
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    public void enviarMsg(View view){

        String msg = edtMsg.getText().toString();

        if(!msg.isEmpty()){

            //Tratando para quando e exibição for de um grupo
            if(idUsuarioDestinatario != null){

                Mensagem mensagem = new Mensagem();
                mensagem.setIdUsuario(idUsuarioRemetente);
                mensagem.setMensagem(msg);

                //Salvar mensagem para o remetente
                salvarMsg(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                //Salvar mensagem para o destinatario
                salvarMsg(idUsuarioDestinatario, idUsuarioRemetente, mensagem);


                //Salvar conversa p/ exibir na aba Conversas
                //Salvar conversa para o remetente
                salvarConversa(idUsuarioRemetente, idUsuarioDestinatario, destinatario ,mensagem, false);

                //Salvar conversa para o destinatario
                Usuario usuarioRemetente = UsuarioFirebase.getDadosUsuarioLogado();
                salvarConversa(idUsuarioDestinatario, idUsuarioRemetente, usuarioRemetente, mensagem, false);

            }else{

                for (Usuario membro :
                        grupo.getMembros()) {
                    String idRemetenteGrupo = Base64Custom.codificarBase64(membro.getEmail());
                    String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario(idUsuarioLogadoGrupo);
                    mensagem.setMensagem(msg);

                    //Salvar mensgagem para o membro
                    salvarMsg(idRemetenteGrupo, idUsuarioDestinatario, mensagem);

                    //Salvar Conversa
                    salvarConversa(idRemetenteGrupo, idUsuarioDestinatario, destinatario, mensagem, true);
                }

            }




        }else{
            Toast.makeText(this, "Digite uma mensgagem para enviar", Toast.LENGTH_SHORT).show();
        }
    }

    private void salvarConversa(String idRemetente, String idDestinatario, Usuario usuarioExibicao ,Mensagem mensagem, boolean isGroup) {

        //Salvar conversa remetente
        Conversa conversaRemetente = new Conversa();
        conversaRemetente.setIdRemetente(idRemetente);
        conversaRemetente.setIdDestinatario(idDestinatario);
        conversaRemetente.setUltimaMensagem(mensagem.getMensagem());

        if(isGroup){//Conversa de grupo
            conversaRemetente.setIsGroup("true");
            conversaRemetente.setGrupo(grupo);
        }else{//Conversa normal
            conversaRemetente.setUsuarioExibicao(usuarioExibicao);
            conversaRemetente.setIsGroup("false");
        }
        conversaRemetente.salvar();

    }

    private void salvarMsg(String idRemetente, String idDestinatario, Mensagem mensagem) {

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference msgRef = database.child("mensagens");

        msgRef.child(idRemetente)
                .child(idDestinatario)
                .push()
                .setValue(mensagem);

        //Limpar caixa de texto
        limparCaixadeTexto();

    }

    private void limparCaixadeTexto() {
         edtMsg.setText("");
    }

    private void recuperarMensagens(){

        childEventListenerMensagens =
                mensagensRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Mensagem mensagem = dataSnapshot.getValue(Mensagem.class);
                        listaMensagem.add(mensagem);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarMensagens();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagensRef.removeEventListener(childEventListenerMensagens);
    }
}
