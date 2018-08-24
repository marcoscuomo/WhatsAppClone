package br.com.californiamobile.whatsappudemy.activity;

import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import br.com.californiamobile.whatsappudemy.R;
import br.com.californiamobile.whatsappudemy.config.ConfiguracaoFirebase;
import br.com.californiamobile.whatsappudemy.model.Usuario;

public class LoginActivity extends AppCompatActivity {

    //Atributos
    private TextInputEditText campoEmail, campoSenha;
    private FirebaseAuth autenticacao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Inicializações
        campoEmail = findViewById(R.id.login_edtEmail);
        campoSenha = findViewById(R.id.login_edtSenha);

        //Iniciando o FirebaseAuth
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

    }

    public void validarAutenticacaoUsuario(View view){

        //recuperar textos dos campos
        String email = campoEmail.getText().toString();
        String senha = campoSenha.getText().toString();

        //Validar se o email e senha foram digitados
        if(!email.isEmpty()){//Verificando o email
            if(!senha.isEmpty()){//Verificando a senha

                Usuario usuario = new Usuario();
                usuario.setEmail(email);
                usuario.setSenha(senha);

                logarUsuario(usuario);

            }else{
                //Usuario não digitou a senha
                Toast.makeText(this, "Digite sua senha", Toast.LENGTH_SHORT).show();
            }
        }else{
            //Usuario nao digitou o email
            Toast.makeText(this, "Digite o Email", Toast.LENGTH_SHORT).show();
        }

    }

    private void logarUsuario(Usuario usuario) {
        autenticacao.signInWithEmailAndPassword(usuario.getEmail(), usuario.getSenha())
        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                }else {
                    String ex = "";
                    try{
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                        ex = "Usuário não cadastrado";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        ex = "E-mail e senha não correspondem";
                    }catch(Exception e){
                        ex = "Erro ao se cadastrar " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(LoginActivity.this, ex , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void abrirTelaCadastro(View view){

        Intent i = new Intent(LoginActivity.this, CadastroActivity.class);
        startActivity(i);

    }


    /*
    * Verificar no metodo onStart se o usuario ja está logado
    * Se ja estiver direcionar para a home
    * */
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser usuarioAtual = autenticacao.getCurrentUser();

        if( usuarioAtual != null ){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }
    }
}
