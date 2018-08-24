package br.com.californiamobile.whatsappudemy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.com.californiamobile.whatsappudemy.R;
import br.com.californiamobile.whatsappudemy.adapter.AdapterContatos;
import br.com.californiamobile.whatsappudemy.adapter.AdapterGrupoSelecionado;
import br.com.californiamobile.whatsappudemy.config.ConfiguracaoFirebase;
import br.com.californiamobile.whatsappudemy.helper.RecyclerItemClickListener;
import br.com.californiamobile.whatsappudemy.helper.UsuarioFirebase;
import br.com.californiamobile.whatsappudemy.model.Usuario;

public class GrupoActivity extends AppCompatActivity {

    //Atributos
    private RecyclerView recyclerMembrosSelecionados, recyclerMembros;
    private AdapterContatos adapterContatos;
    private AdapterGrupoSelecionado adapterGrupoSelecionado;
    private List<Usuario> listaMembros = new ArrayList<>();
    private List<Usuario> listaMembrosSelecionados = new ArrayList<>();
    private ValueEventListener valueEventListenerMembros;
    private DatabaseReference usuariosRef;
    private FirebaseUser usuarioAtual;
    private Toolbar toolbar;
    private FloatingActionButton fabAvancarCadastro;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupo);

        //Toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo Grupo");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Inicializações
        recyclerMembrosSelecionados = findViewById(R.id.recyclerMembrosSelecionados);
        recyclerMembros = findViewById(R.id.recyclerMembros);
        fabAvancarCadastro = findViewById(R.id.fabAvancarCadastro);

        //Firabase
        usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();

        //Configurar o Adapter
        adapterContatos = new AdapterContatos(listaMembros, getApplicationContext());

        //Configurar o RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(GrupoActivity.this);
        recyclerMembros.setLayoutManager(layoutManager);
        recyclerMembros.setHasFixedSize(true);
        recyclerMembros.setAdapter(adapterContatos);

        //Evento de clique no RecylcerView
        recyclerMembros.addOnItemTouchListener(new RecyclerItemClickListener(
                getApplication(), recyclerMembros,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Usuario usuarioSelecionado
                                = listaMembros.get(position);

                        //Remover o usuario selecionado da lista
                        listaMembros.remove(usuarioSelecionado);
                        adapterContatos.notifyDataSetChanged();

                        //Adicionar o usuario selecionado ao recyclerviewMembrosSelecionado
                        listaMembrosSelecionados.add(usuarioSelecionado);
                        adapterGrupoSelecionado.notifyDataSetChanged();

                        atualizarMembrosToolbar();


                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    }
                }

        ));


        //Configurar o RecyclerView para os membros selecionados
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

        //Configurando clique no RV do Grupo selecionadoR
        recyclerMembrosSelecionados.addOnItemTouchListener(new RecyclerItemClickListener(
                GrupoActivity.this, recyclerMembrosSelecionados,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        Usuario usuarioSelecionado = listaMembrosSelecionados.get(position);

                        //Remover da listagem de mebro selecionado
                        listaMembrosSelecionados.remove(usuarioSelecionado);
                        adapterGrupoSelecionado.notifyDataSetChanged();

                        //Adicionar a listagem de membros
                        listaMembros.add(usuarioSelecionado);
                        adapterContatos.notifyDataSetChanged();
                        atualizarMembrosToolbar();


                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    }
                }
        ));

        //Configurar FAB
        fabAvancarCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(GrupoActivity.this, CadastroGrupoActivity.class);
                i.putExtra("membros", (Serializable) listaMembrosSelecionados);
                startActivity(i);

            }
        });

    }

    //Metodo para configurar as informações da Toolbar
    public void atualizarMembrosToolbar(){

        int totalSelecionados = listaMembrosSelecionados.size();
        int total = listaMembros.size() + totalSelecionados;
        String mostrar = totalSelecionados + " de " + total + " selecionados";
        toolbar.setSubtitle(mostrar);

    }

    public void recuperarContatos(){

        valueEventListenerMembros = usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot dados: dataSnapshot.getChildren()){

                    Usuario usuario = dados.getValue(Usuario.class);

                    //Verificando se for o usuario atual não exibir na lista
                    String emailUsuarioAtual = usuarioAtual.getEmail();
                    if(!emailUsuarioAtual.equals(usuario.getEmail())){
                        listaMembros.add(usuario);
                    }
                }
                adapterContatos.notifyDataSetChanged();
                atualizarMembrosToolbar();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        usuariosRef.removeEventListener(valueEventListenerMembros);
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarContatos();
    }
}
