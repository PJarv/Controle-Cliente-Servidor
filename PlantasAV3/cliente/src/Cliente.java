import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.DecimalFormat;
import java.util.*;
import javax.crypto.Cipher;
import Modelo.Planta;

public class Cliente {

    private static final String CAMINHO_CHAVE_PUBLICA = "C:\\Users\\Cliente\\OneDrive\\Área de Trabalho\\PlantasAV3\\cliente\\chave\\publica.chv";

    // Complexidade: O(n)
    private static PublicKey carregarChavePublica(String caminho) throws IOException {
        byte[] bytesChave = Files.readAllBytes(Paths.get(caminho));
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(bytesChave);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (Exception e) {
            throw new IOException("Falha ao carregar chave pública", e);
        }
    }

    public static void main(String[] args) {
        List<String> nomesPlantas = Arrays.asList(
                "Rosa", "Girassol", "Orquídea", "Lírio", "Hibisco",
                "Tulipa", "Lavanda", "Dente-de-leão", "Cravo", "Bromélia"
        );

        Map<Planta, List<Integer>> leituras = new HashMap<>();
        Random random = new Random();
        for (int i = 1; i <= 10; i++) {
            List<Integer> crescimento = new ArrayList<>();
            for (int j = 1; j <= 5; j++) {
                crescimento.add((int) (Math.random() * 10) + 5); 
            }
            String nomePlanta = nomesPlantas.get(random.nextInt(nomesPlantas.size()));
            leituras.put(new Planta("ID" + i, nomePlanta), crescimento);
        }

        List<Map<Planta, List<Integer>>> combinacoes = gerarCombinacoesDePlantas(leituras);
        List<String> resultadosParaServidor = new ArrayList<>();
        List<String> mensagensExibicao = new ArrayList<>();


        List<Thread> threads = new ArrayList<>();
        for (Map<Planta, List<Integer>> combinacao : combinacoes) {
            Thread thread = new Thread(() -> {
                List<Planta> plantas = new ArrayList<>(combinacao.keySet());
                double mediaCombinada = calcularMedia(combinacao);

                String mensagem = "Média de crescimento entre " +
                        plantas.get(0).getNome() + " (" + plantas.get(0).getIdentificacao() + "), " +
                        plantas.get(1).getNome() + " (" + plantas.get(1).getIdentificacao() + ") e " +
                        plantas.get(2).getNome() + " (" + plantas.get(2).getIdentificacao() + "): " +
                        new DecimalFormat("#.00").format(mediaCombinada) + " cm.";

                if (mediaCombinada <= 9) {
                    resultadosParaServidor.add(mensagem);
                    mensagensExibicao.add(mensagem + " (Enviado ao servidor)");
                } else {
                    mensagensExibicao.add(mensagem + " (Crescimento aceitável ignorado)");
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join(); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (String mensagem : mensagensExibicao) {
            System.out.println(mensagem);
        }

        try (Socket socket = new Socket("localhost", 12345);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            if (socket.isConnected() && !socket.isClosed()) {
                PublicKey chavePublica = carregarChavePublica(CAMINHO_CHAVE_PUBLICA);
                for (String mensagem : resultadosParaServidor) {
                    String mensagemEncriptada = encriptar(mensagem, chavePublica);
                    out.writeObject(mensagemEncriptada);
                    out.flush();
                }

                out.writeObject(null); 
                out.flush();
                System.out.println("Crescimentos enviados perfeitamente.");

            } else {
                System.out.println("Erro: Socket não conectado.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Complexidade: O(n³)
    private static List<Map<Planta, List<Integer>>> gerarCombinacoesDePlantas(Map<Planta, List<Integer>> leituras) {
        List<Map<Planta, List<Integer>>> combinacoes = new ArrayList<>();
        List<Planta> plantas = new ArrayList<>(leituras.keySet());

        for (int i = 0; i < plantas.size(); i++) {
            for (int j = i + 1; j < plantas.size(); j++) {
                for (int k = j + 1; k < plantas.size(); k++) {
                    Map<Planta, List<Integer>> combinacao = new HashMap<>();
                    combinacao.put(plantas.get(i), leituras.get(plantas.get(i)));
                    combinacao.put(plantas.get(j), leituras.get(plantas.get(j)));
                    combinacao.put(plantas.get(k), leituras.get(plantas.get(k)));
                    combinacoes.add(combinacao);
                }
            }
        }
        return combinacoes;
    }

    // Complexidade: O(n)
    private static double calcularMedia(Map<Planta, List<Integer>> combinacao) {
        double mediaCombinada = 0.0;
        for (Planta planta : combinacao.keySet()) {
            List<Integer> leiturasPlanta = combinacao.get(planta);
            double soma = 0.0;
            for (Integer valor : leiturasPlanta) {
                soma += valor;
            }
            mediaCombinada += soma / leiturasPlanta.size(); 
        }
        return mediaCombinada / 3.0; 
    }

    // Complexidade: O(n²)
    private static String encriptar(String dados, PublicKey chavePublica) {
        try {
            Cipher cifrador = Cipher.getInstance("RSA");
            cifrador.init(Cipher.ENCRYPT_MODE, chavePublica);
            byte[] bytesEncriptados = cifrador.doFinal(dados.getBytes()); 
            return Base64.getEncoder().encodeToString(bytesEncriptados); 
        } catch (Exception e) {
            throw new RuntimeException("Falha ao encriptar dados", e);
        }
    }
}
