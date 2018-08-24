package br.com.californiamobile.whatsappudemy.ui.activity;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import br.com.californiamobile.whatsappudemy.R;
import br.com.californiamobile.whatsappudemy.config.ConfiguracaoFirebase;
import br.com.californiamobile.whatsappudemy.helper.Base64Custom;
import br.com.californiamobile.whatsappudemy.helper.UsuarioFirebase;
import br.com.californiamobile.whatsappudemy.model.Usuario;

public class CadastroActivity extends AppCompatActivity {

    //Atributos
    private TextInputEditText campoNome, campoEmail, campoSenha;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        //Inicializações
        campoNome  = findViewById(R.id.edtNome);
        campoEmail = findViewById(R.id.edtEmail);
        campoSenha = findViewById(R.id.edtSenha);

    }

    public void validarCadastroUsuario(View view){

        //Recuperar textos dos campo
        String textoNome  = campoNome.getText().toString();
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        //Validando se os campos foram digitados
        if(!textoNome.isEmpty()){//Verificando o nome
            if(!textoEmail.isEmpty()){//Verificando o email
                if(!textoSenha.isEmpty()){//Verificando a senha

                    Usuario usuario = new Usuario();
                    usuario.setNome(textoNome);
                    usuario.setEmail(textoEmail);
                    usuario.setSenha(textoSenha);

                    cadastrarUsuario(usuario);

                }else{
                    //Usuario não digitou a senha
                    Toast.makeText(this, "Digite sua senha", Toast.LENGTH_SHORT).show();
                }
            }else{
                //Usuario nao digitou o email
                Toast.makeText(this, "Digite o Email", Toast.LENGTH_SHORT).show();
            }
        }else {
            //Usuario não digitou o nome
            Toast.makeText(this, "Preencha o nome", Toast.LENGTH_SHORT).show();
        }

    }

    private void cadastrarUsuario(final Usuario usuario) {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(usuario.getEmail(), usuario.getSenha())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    Toast.makeText(CadastroActivity.this,
                           "Cadastrado criado com sucesso", Toast.LENGTH_SHORT).show();
                    UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());



                    /*
                    * Iniciando a rotina p/ salvarmos os dados do usuario no database
                    * Vamos pegar o email e codificar para base 64 e salvar no
                    * atributo do objeto usuario
                    *
                    * */
                    String identificarUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                    usuario.setId(identificarUsuario);
                    usuario.salvar(); //Metodo para salvar os dados do usuario no Firebase


                    finish();

                }else{
                    //Verificando o motivo do cadastro com erro
                    String excecao = "";
                    try{
                        throw task.getException();
                    }catch (FirebaseAuthWeakPasswordException e){
                        excecao = "Digite uma senha mais forte";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "Digite um e-mail valido";
                    }catch (FirebaseAuthUserCollisionException e){
                        excecao = "Essa conta ja foi cadastrada";
                    }catch (Exception e){
                        excecao = "Erro ao cadastrar o usuario " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(CadastroActivity.this, excecao, Toast.LENGTH_SHORT).show();

                }

            }
        });
    }
}
