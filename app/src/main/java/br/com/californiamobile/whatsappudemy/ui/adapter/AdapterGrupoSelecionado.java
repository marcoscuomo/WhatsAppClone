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
import br.com.californiamobile.whatsappudemy.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterGrupoSelecionado extends RecyclerView.Adapter<AdapterGrupoSelecionado.MyViewHolder> {


    private List<Usuario> listaContatosSelecionados;
    private Context context;

    public AdapterGrupoSelecionado(List<Usuario> listaContatos, Context context) {
        this.listaContatosSelecionados = listaContatos;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View itemLista = LayoutInflater.from(viewGroup.getContext() ).
                inflate(R.layout.adapter_grupo_selecionado, viewGroup, false);

        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {

        Usuario usuario = listaContatosSelecionados.get(i);
        holder.nome.setText(usuario.getNome());

        if(usuario.getFoto() != null){
            Uri url = Uri.parse(usuario.getFoto());
            Glide.with( context ).load(url).into(holder.foto);
        }else{
            holder.foto.setImageResource(R.drawable.padrao);
        }

    }

    @Override
    public int getItemCount() {
        return listaContatosSelecionados.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView foto;
        TextView nome;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            foto = itemView.findViewById(R.id.imgFotoMembroSelecionado);
            nome = itemView.findViewById(R.id.txtNomeMembroSelecionado);
        }
    }
}
