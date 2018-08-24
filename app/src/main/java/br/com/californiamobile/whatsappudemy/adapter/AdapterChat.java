package br.com.californiamobile.whatsappudemy.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.List;
import br.com.californiamobile.whatsappudemy.R;
import br.com.californiamobile.whatsappudemy.helper.UsuarioFirebase;
import br.com.californiamobile.whatsappudemy.model.Mensagem;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyViewHolder> {

    private List<Mensagem> listaMensagens;
    private Context c;
    private static final int TIPO_REMETENTE    = 0;
    private static final int TIPO_DESTINATARIO = 1;

    public AdapterChat(List<Mensagem> msgs, Context c) {
        this.listaMensagens = msgs;
        this.c = c;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View item = null;
        if( i == TIPO_REMETENTE ){
            item = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.adapter_msg_remetente, viewGroup, false);
        }else if(i == TIPO_DESTINATARIO){
            item = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.adapter_msg_destinatario, viewGroup, false);
        }

        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {

        Mensagem mensagem = listaMensagens.get(i);
        String msg = mensagem.getMensagem();
        String imagem = mensagem.getImagem();

        if(imagem != null){
            Uri url = Uri.parse(imagem);
            Glide.with(c).load(url).into(holder.img);

            //Esconder o texto
            holder.msg.setVisibility(View.GONE);
        }else{
            holder.msg.setText(msg);
            //Esconder o texto
            holder.img.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return listaMensagens.size();
    }

    @Override
    public int getItemViewType(int position) {

        Mensagem mensagem = listaMensagens.get(position);
        String idUsuario = UsuarioFirebase.getIdentificadorUsuario();

        if(idUsuario.equals(mensagem.getIdUsuario())){
            return TIPO_REMETENTE;
        }else{
            return TIPO_DESTINATARIO;
        }

    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView msg;
        ImageView img;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            msg = itemView.findViewById(R.id.textMensagemTexto);
            img = itemView.findViewById(R.id.imageMensagemFoto);
        }
    }
}
