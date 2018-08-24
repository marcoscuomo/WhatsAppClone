package br.com.californiamobile.whatsappudemy.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import br.com.californiamobile.whatsappudemy.R;
import br.com.californiamobile.whatsappudemy.activity.ChatActivity;
import br.com.californiamobile.whatsappudemy.adapter.AdapterConversas;
import br.com.californiamobile.whatsappudemy.config.ConfiguracaoFirebase;
import br.com.californiamobile.whatsappudemy.helper.RecyclerItemClickListener;
import br.com.californiamobile.whatsappudemy.helper.UsuarioFirebase;
import br.com.californiamobile.whatsappudemy.model.Conversa;
import br.com.californiamobile.whatsappudemy.model.Usuario;

public class ConversasFragment extends Fragment {

    //Atributos
    private RecyclerView recyclerViewConversas;
    private AdapterConversas adapter;
    private List<Conversa> listaConversas = new ArrayList<>();
    private DatabaseReference database, conversasRef;
    private ChildEventListener childEventListener;


    public ConversasFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_conversas, container, false);


        //Inicializações
        recyclerViewConversas = view.findViewById(R.id.recyclerViewListaConversas);

        //Firabase
        //conversaRef = ConfiguracaoFirebase.getFirebaseDatabase().child("conversas");//Fiz errado
        String identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        database = ConfiguracaoFirebase.getFirebaseDatabase();
        conversasRef =  database.child("conversas").child(identificadorUsuario);


        //RecyclerView
        //Adapter
        adapter = new AdapterConversas(getContext(), listaConversas);

        //Configuracoes gerais do RV
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewConversas.setLayoutManager(layoutManager);
        recyclerViewConversas.setHasFixedSize(true);
        recyclerViewConversas.setAdapter(adapter);


        //Configurando evento de clique
        recyclerViewConversas.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(), recyclerViewConversas,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                Conversa conversaSelecionada = listaConversas.get(position);

                                if(conversaSelecionada.getIsGroup().equals("true")){
                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                    i.putExtra("chatGrupo", conversaSelecionada.getGrupo());
                                    startActivity(i);
                                }else{
                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                    i.putExtra("chatContato", conversaSelecionada.getUsuarioExibicao());
                                    startActivity(i);
                                }

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
                        }
                )
        );


        return view;
    }

    //Metodo que sera usado no MainActivity para a ação de pesquisar
    public void pesquisarConversa(String texto){

        //Log.d("pesquisa", texto);

        List<Conversa> listaConversasBusca = new ArrayList<>();

        for (Conversa conversa :
                listaConversas) {

            String nome = conversa.getUsuarioExibicao().getNome().toLowerCase();
            String ultimaMsg = conversa.getUltimaMensagem().toLowerCase();

            if(nome.contains(texto) || ultimaMsg.contains(texto)){
                listaConversasBusca.add(conversa);
            }
        }

        adapter = new AdapterConversas(getActivity(), listaConversasBusca);
        recyclerViewConversas.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    //Metodo chamado pelo metodo de pesquisa do MainActivity
    //Ira retornar a listagem original, quando o botao de pesquisar for fechado
    public void recarregarConversas(){

        adapter = new AdapterConversas(getActivity(), listaConversas);
        recyclerViewConversas.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }


    public void listaConversas(){


        /*minha logica
        * for (DataSnapshot dados:
                        dataSnapshot.getChildren()) {

                    Conversa conversa = dados.getValue(Conversa.class);
                    listaConversas.add(conversa);

                }
                adapter.notifyDataSetChanged();
        * */

        childEventListener = conversasRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //Recuperar conversas
                Conversa conversa = dataSnapshot.getValue(Conversa.class);
                listaConversas.add(conversa);
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
    public void onStart() {
        super.onStart();
        listaConversas();
    }

    @Override
    public void onStop() {
        super.onStop();
        conversasRef.removeEventListener(childEventListener);
    }
}
