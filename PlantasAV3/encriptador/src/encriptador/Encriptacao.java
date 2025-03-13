package encriptador;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.sound.sampled.*;

public class Encriptacao {

    private static final String VIDEO_PATH = "C:\\Users\\Cliente\\OneDrive\\Área de Trabalho\\PlantasAV3\\encriptador\\video\\audio.wav";
    private static final String CHAVE_PUBLICA_PATH = "C:\\Users\\Cliente\\OneDrive\\Área de Trabalho\\PlantasAV3\\cliente\\chave\\publica.chv";
    private static final String CHAVE_PRIVADA_PATH = "C:\\Users\\Cliente\\OneDrive\\Área de Trabalho\\PlantasAV3\\servidor\\chave\\privada.chv";

    public static void main(String[] args) {
        try {
            byte[] seed = extrairAleatoriedadeDoAudio(VIDEO_PATH);
            gerarChavesRSA(seed);
            System.out.println("Chaves geradas com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Complexidade: O(n)
    private static byte[] extrairAleatoriedadeDoAudio(String videoPath) throws Exception {
        File videoFile = new File(videoPath);
        if (!videoFile.exists()) {
            throw new IOException("Arquivo de vídeo não encontrado: " + videoPath);
        }

        AudioInputStream audioStream = AudioSystem.getAudioInputStream(videoFile);
        byte[] audioBytes = new byte[audioStream.available()];
        audioStream.read(audioBytes);
        audioStream.close();

        return audioBytes;
    }

    // Complexidade: O(n)
    private static void gerarChavesRSA(byte[] seed) throws NoSuchAlgorithmException, IOException {
        SecureRandom random = new SecureRandom(seed);
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048, random); 
        KeyPair keyPair = keyGen.generateKeyPair(); 

        System.out.println("Chave pública: ");
        System.out.println(bytesParaHex(keyPair.getPublic().getEncoded())); 

        System.out.println("Chave privada: ");
        System.out.println(bytesParaHex(keyPair.getPrivate().getEncoded())); 

        salvarChave(CHAVE_PUBLICA_PATH, keyPair.getPublic().getEncoded()); 
        salvarChave(CHAVE_PRIVADA_PATH, keyPair.getPrivate().getEncoded()); 
    }
    
    // Complexidade: O(n)
    private static String bytesParaHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b)); 
        }
        return sb.toString().toUpperCase();
    }


    // Complexidade: O(n)
    private static void salvarChave(String caminho, byte[] chave) throws IOException {
        Files.createDirectories(Paths.get(caminho).getParent()); 
        Files.write(Paths.get(caminho), chave);
    }
}
