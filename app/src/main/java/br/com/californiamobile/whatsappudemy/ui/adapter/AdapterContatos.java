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

public class AdapterContatos extends RecyclerView.Adapter<AdapterContatos.MyViewHolder> {

    private List<Usuario> listaContatos;
    private Context context;

    public AdapterContatos(List<Usuario> listaContatos, Context context) {
        this.listaContatos = listaContatos;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View itemLista = LayoutInflater.from(viewGroup.getContext() ).
                inflate(R.layout.lista_contatos, viewGroup, false);

        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {

        Usuario usuario = listaContatos.get(i);
        boolean cabecalho = usuario.getEmail().isEmpty(); //True - se tiver um email | False - caso nao tenha u email

        holder.txtNome.setText(usuario.getNome());
        holder.txtEmail.setText(usuario.getEmail());

        if(usuario.getFoto() != null){
            Uri url = Uri.parse(usuario.getFoto());
            Glide.with( context ).load(url).into(holder.imgFoto);
        }else{
            if(cabecalho){
                holder.imgFoto.setImageResource(R.drawable.icone_grupo);
                holder.txtEmail.setVisibility(View.GONE);
            }else{
                holder.imgFoto.setImageResource(R.drawable.padrao);
            }
        }

    }

    @Override
    public int getItemCount() {
        return listaContatos.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

    CircleImageView imgFoto;
    TextView txtNome;
    TextView txtEmail;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        imgFoto = itemView.findViewById(R.id.contatos_imgContato);
        txtNome = itemView.findViewById(R.id.contatos_txtNome);
        txtEmail = itemView.findViewById(R.id.contatos_txtEmail);
    }
}

}


