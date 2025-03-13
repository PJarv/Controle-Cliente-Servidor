import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;

public class Servidor {

    private static final String CAMINHO_CHAVE_PRIVADA = "C:\\Users\\Cliente\\OneDrive\\Área de Trabalho\\PlantasAV3\\servidor\\chave\\privada.chv";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("\nAguardando resultados de Médias de Crescimentos...");

            while (true) {
                try (Socket socket = serverSocket.accept();
                     ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                    List<String> mensagens = new ArrayList<>();
                    String dadosEncriptados;

                    while ((dadosEncriptados = (String) in.readObject()) != null) {
                        PrivateKey chavePrivada = carregarChavePrivada(CAMINHO_CHAVE_PRIVADA);
                        String dadosDecriptados = desencriptar(dadosEncriptados, chavePrivada);
                        mensagens.add(dadosDecriptados);
                    }

                    System.out.println("Crescimentos recebidos:");
                    for (String mensagem : mensagens) {
                        System.out.println(mensagem);
                    }

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Complexidade: O(n)
    private static PrivateKey carregarChavePrivada(String caminho) throws IOException {
        byte[] bytesChave = Files.readAllBytes(Paths.get(caminho));
        try {
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytesChave); 
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new IOException("Falha ao carregar chave privada", e);
        }
    }

    // Complexidade: O(n²)
    private static String desencriptar(String dadosEncriptados, PrivateKey chavePrivada) {
        try {
            Cipher cifrador = Cipher.getInstance("RSA");
            cifrador.init(Cipher.DECRYPT_MODE, chavePrivada);
            byte[] bytesDecriptados = cifrador.doFinal(Base64.getDecoder().decode(dadosEncriptados));
            return new String(bytesDecriptados);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao desencriptar dados", e);
        }
    }
}
