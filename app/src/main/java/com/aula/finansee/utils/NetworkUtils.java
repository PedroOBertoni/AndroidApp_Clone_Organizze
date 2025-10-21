package com.aula.finansee.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

public class NetworkUtils {

    /* Instanciando o método como estático para que ele possa ser acessados sem a necessidade de
        instanciar a classe */
    public static boolean isNetworkAvailable(Context context) {
        // Obtém o serviço de conectividade do sistema para verificar o estado da rede.
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Verifica se o serviço de conectividade foi obtido com sucesso.
        if (connectivityManager != null) {

            // Tratamento de erro paras as versões mais modernas
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                // Captura a rede ativa (a rede principal que o dispositivo está usando)
                Network network = connectivityManager.getActiveNetwork();

                // Se não houver rede ativa, retorna falso
                if (network == null) {
                    return false;
                }

                // Captura as capacidades (recursos) da rede ativa (tipo de transporte, velocidade, etc)
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);

                /* Retorna verdadeiro se as capacidades não forem nulas e a rede ativa possuir um dos transportes
                abaixo (indicando que é uma conexão utilizável) */
                return capabilities != null &&
                        (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || // Dados móveis
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));

            } else {
                // Tratamento de erro paras as versões mais antigas

                // Para APIs mais antigas, usa o método obsoleto getActiveNetworkInfo().
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

                // Retorna verdadeiro se houver uma NetworkInfo e se ela estiver conectada.
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        }

        // Retorna falso se o ConnectivityManager não puder ser obtido.
        return false;
    }
}