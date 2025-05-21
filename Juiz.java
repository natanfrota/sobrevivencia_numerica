package sobrevivencia_numerica;

import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

public class Juiz {
    final int NUMERO_DE_JOGADORES = 3;
    //Jogador[] jogadores = new Jogador[NUMERO_DE_JOGADORES]; // usando vetor
    private Map<SocketAddress, Jogador> jogadores = null; // usando map
    private DatagramSocket soqueteServidor = null;
    
    public Juiz(DatagramSocket soqueteServidor){
        this.soqueteServidor = soqueteServidor;
        this.jogadores = new HashMap<>();
    }

    public void aguardarJogadores() throws IOException {
        System.out.println("Aguardando os jogadores...");

        while (this.jogadores.size() < this.NUMERO_DE_JOGADORES) {

            byte[] dado = new byte[50]; // armazena o nome do jogador que vem na requisição
            DatagramPacket requisicao = new DatagramPacket(dado, dado.length);
            this.soqueteServidor.receive(requisicao);

            // cria um novo jogador
            Jogador novoJogador = new Jogador(new String(requisicao.getData()), requisicao.getSocketAddress());

            // armazena-o
            // jogadores[contJogadores] = novoJogador; // usando vetor
            this.jogadores.put(requisicao.getSocketAddress(), novoJogador); // usando map

            System.out.println("Jogador(a): " + novoJogador.getNome() + " entrou no jogo.");
            System.out.println("IP: " + requisicao.getAddress());
            System.out.println("Porta: " + requisicao.getPort());

            byte[] mensagem = "Aguardando jogadores oponentes...".getBytes();
            DatagramPacket resposta = new DatagramPacket(mensagem, mensagem.length, novoJogador.getEnderecoSoquete());

            // envia a mensagem para o cliente
            this.soqueteServidor.send(resposta);
        }
    }




    
    public void finalizarJogo(){
        this.soqueteServidor.close();
    }




    public static void main(String[] args) {
        final int NUMERO_DA_PORTA = 57_651;
        Juiz juiz = null;

        try {
            DatagramSocket soqueteServidor = new DatagramSocket(NUMERO_DA_PORTA);
            juiz = new Juiz(soqueteServidor);
            juiz.aguardarJogadores();
            

            juiz.finalizarJogo();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}