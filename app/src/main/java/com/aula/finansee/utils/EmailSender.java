package com.aula.finansee.utils;

import android.util.Log;

import com.aula.finansee.BuildConfig;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {

    public static boolean enviarEmail(String destinatario, String assunto, String corpo) {
        // Recupera as credenciais definidas no build.gradlear
        final String EMAIL_SENDER = BuildConfig.EMAIL_SENDER;
        final String EMAIL_APP_PASSWORD = BuildConfig.EMAIL_APP_PASSWORD;

        Log.d("EMAIL_DEBUG", "EMAIL_SENDER=" + EMAIL_SENDER);
        Log.d("EMAIL_DEBUG", "EMAIL_APP_PASSWORD=" + EMAIL_APP_PASSWORD);

        // Verifica se as credenciais foram carregadas
        if (EMAIL_SENDER == null || EMAIL_SENDER.isEmpty() ||
                EMAIL_APP_PASSWORD == null || EMAIL_APP_PASSWORD.isEmpty()) {
            Log.e("EMAIL_DEBUG", "Credenciais de email não configuradas corretamente!");
            return false;
        }

        try {
            // Configura as propriedades do servidor SMTP
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            // Cria sessão autenticada
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_SENDER, EMAIL_APP_PASSWORD);
                }
            });

            // Monta o e-mail
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_SENDER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject(assunto);

            // Suporte a HTML no corpo do email (para deixar o código em negrito)
            message.setContent(corpo, "text/html; charset=utf-8");

            // Envia
            Transport.send(message);

            Log.d("EMAIL_DEBUG", "E-mail enviado com sucesso para " + destinatario);
            return true;

        } catch (MessagingException e) {
            Log.e("EMAIL_DEBUG", "Erro ao enviar e-mail: " + e.getMessage(), e);
            return false;
        }
    }
}
