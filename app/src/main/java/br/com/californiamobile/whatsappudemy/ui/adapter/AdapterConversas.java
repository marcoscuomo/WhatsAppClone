package br.com.californiamobile.whatsappudemy.ui.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import br.com.californiamobile.whatsappudemy.R;
import br.com.californiamobile.whatsappudemy.model.Conversa;
import br.com.californiamobile.whatsappudemy.model.Grupo;
import br.com.californiamobile.whatsappudemy.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterConversas extends RecyclerView.Adapter<AdapterConversas.MyViewHolder> {

    Context context;
    List<Conversa> listaConversa;

    public AdapterConversas(Context context, List<Conversa> listaConversa) {
        this.context = context;
        this.listaConversa = listaConversa;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View itemLista = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.lista_conversas, viewGroup, false);

        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {

        Conversa conversa = listaConversa.get(i);
        //Usuario usuario = conversa.getUsuarioExibicao();

        if(conversa.getIsGroup().equals("true")){

            Grupo grupo = conversa.getGrupo();
            holder.txtNome.setText(grupo.getNome());


            if(grupo.getFoto() != null){
                Uri url = Uri.parse(grupo.getFoto());
                Glide.with(context).load(url).into(holder.imgContato);
            }else{
                holder.imgContato.setImageResource(R.drawable.padrao);
            }



        }else{
            Usuario usuario = conversa.getUsuarioExibicao();

            if(usuario != null){
                holder.txtNome.setText(usuario.getNome());
                //holder.txtUltimaConversa.setText(conversa.getUltimaMensagem());
                if(usuario.getFoto() != null){
                    Uri url = Uri.parse(usuario.getFoto());
                    Glide.with(context).load(url).into(holder.imgContato);
                }else{
                    holder.imgContato.setImageResource(R.drawable.padrao);
                }
            }
        }



    }

    @Override
    public int getItemCount() {
        return listaConversa.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView imgContato;
        TextView txtNome, txtUltimaConversa;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgContato = itemView.findViewById(R.id.conversas_imgContato);
            txtNome = itemView.findViewById(R.id.conversas_txtNome);
            txtUltimaConversa = itemView.findViewById(R.id.coversas_txtUltimaComversa);
        }
    }

}
