package com.aula.finansee.utils;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DatabaseError;

import java.net.UnknownHostException;

public class FirebaseErrorHandler {

    private static final String TAG = "FirebaseHandler";

    // Verifica a conexão antes de iniciar uma operação do Firebase
    public static boolean checkConnectionAndNotify(Activity activity, String operationName) {
        // Usa o NetworkUtils para checar a rede
        if (NetworkUtils.isNetworkAvailable(activity)) {
            return true;
        } else {
            // Mostra o erro na snackBar
            showSnackbar(activity, "Sem internet. Não foi possível " + operationName + ".");

            // e no log
            Log.i(TAG, "Operação de " + operationName + " abortada devido à falta de rede (Proativa).");
            return false;
        }
    }

    // Trata erros reportados pelo Realtime Database
    public static boolean handleDatabaseError(Activity activity, @NonNull DatabaseError error, String operationName) {
        // Verifica autenticação
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.i(TAG, "Erro ignorado em " + operationName + ": Usuário deslogado.");
            return true;
        }

        // Verifica erro de rede rede ou desconexão do database
        int errorCode = error.getCode();
        // Códigos -4 (Disconnected) ou -2 (Unavailable/Servidor inalcançável)
        if (errorCode == DatabaseError.DISCONNECTED || errorCode == DatabaseError.UNAVAILABLE) {
            // Mostra erro na snackBar
            showSnackbar(activity, "Sem conexão. Verifique sua rede e tente novamente.");

            // e no log
            Log.e(TAG, "Erro de Rede no Realtime Database (" + operationName + "): " + error.getMessage());
            return true;
        }

        /* Erros Genéricos */
        // Mostra erro na snackBar
        showSnackbar(activity, "Erro ao " + operationName + ". Tente novamente.");
        // e no log
        Log.e(TAG, "Erro geral do Firebase (" + operationName + "): " + error.getMessage(), error.toException());
        return false;
    }

    public static void handleTaskFailure(Activity activity, @NonNull Exception e, String operationName) {
        // Verificação da autenticação
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Se o usuário já deslogou ou não está logado, ignoramos a falha na Task
            Log.i(TAG, "Falha ignorada em " + operationName + ": Usuário deslogado.");
            return;
        }

        /* Tratamento de Exceção de rede da Task, verificando as exceções mais comuns de rede
        (FirebaseNetworkException ou erro parecidos) */
        if (e instanceof FirebaseNetworkException || e.getCause() instanceof UnknownHostException) {
            // Mostra na snackBar
            showSnackbar(activity, "Sem conexão com a internet. Verifique sua rede.");

            // e no log
            Log.e(TAG, "Erro de Rede (Task) em " + operationName + ": " + e.getMessage());
            return;
        }

        // Tratamento de erros específicos do Firebase Auth
        if (e instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) e).getErrorCode();
            String message = getFriendlyAuthMessage(errorCode);
            // Mostra na snackBar
            showSnackbar(activity, message);

            // e no log
            Log.e(TAG, "Erro de Auth em " + operationName + ": " + errorCode);
            return;
        }

        /* Erros Genéricos */
        // Mostra na snackBar
        showSnackbar(activity, "Erro inesperado ao " + operationName + ". Tente novamente.");

        // e no log
        Log.e(TAG, "Erro de Task geral em " + operationName + ": " + e.getMessage(), e);
    }

    // Converte códigos de erro do Firebase em mensagens amigáveis e em português para o usuário
    private static String getFriendlyAuthMessage(String errorCode) {
        switch (errorCode) {
            case "ERROR_INVALID_EMAIL":
            case "auth/invalid-email":
                return "O formato do e-mail é inválido.";
            case "ERROR_WRONG_PASSWORD":
            case "auth/wrong-password":
                return "Senha incorreta.";
            case "ERROR_USER_NOT_FOUND":
            case "auth/user-not-found":
                return "Usuário não encontrado.";
            case "ERROR_EMAIL_ALREADY_IN_USE":
            case "auth/email-already-in-use":
                return "Já existe uma conta com este e-mail.";
            case "ERROR_WEAK_PASSWORD":
            case "auth/weak-password":
                return "A senha deve ter no mínimo 6 caracteres.";
            case "ERROR_USER_DISABLED":
            case "auth/user-disabled":
                return "Este usuário foi desativado.";
            default:
                // Para erros não mapeados (e.g., timeout, token, etc.)
                return "Ocorreu um erro na autenticação. Tente novamente.";
        }
    }

    // Método auxiliar para exibir a Snackbar centralizada e segura.
    private static void showSnackbar(Activity activity, String message) {
        // Instancia o objeto da View
        View rootView = activity.findViewById(android.R.id.content);

        if (rootView != null) {
            // Mostra a snackBar sempre long e com a message definida
            Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
        }
    }
}