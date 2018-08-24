package br.com.californiamobile.whatsappudemy.model;

import com.google.firebase.database.DatabaseReference;
import br.com.californiamobile.whatsappudemy.config.ConfiguracaoFirebase;
public class Conversa {

    private String idRemetente, idDestinatario, ultimaMensagem, isGroup;
    private Usuario usuarioExibicao;
    private Grupo grupo;


    //Construtor vazio
    public Conversa() {
        this.setIsGroup("false");
    }



    //Getters e Setters


    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public String getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(String isGroup) {
        this.isGroup = isGroup;
    }

    public Usuario getUsuarioExibicao() {
        return usuarioExibicao;
    }

    public void setUsuarioExibicao(Usuario usuarioExibicao) {
        this.usuarioExibicao = usuarioExibicao;
    }


    public String getIdRemetente() {
        return idRemetente;
    }

    public void setIdRemetente(String idRemetente) {
        this.idRemetente = idRemetente;
    }

    public String getIdDestinatario() {
        return idDestinatario;
    }

    public void setIdDestinatario(String idDestinatario) {
        this.idDestinatario = idDestinatario;
    }

    public String getUltimaMensagem() {
        return ultimaMensagem;
    }

    public void setUltimaMensagem(String ultimaMensagem) {
        this.ultimaMensagem = ultimaMensagem;
    }


    //Metodo para savlar a conversa
    public void salvar() {

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference conversaRef = database.child("conversas");

        conversaRef
                .child(this.getIdRemetente())
                .child(this.getIdDestinatario())
                .setValue(this);

    }
}
