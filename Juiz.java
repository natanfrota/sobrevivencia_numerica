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

    public void receberNumeroEscolhidos() throws IOException{
        System.out.println("Aguardando os numeros escolhidos...");
        int jogadorComNumero = 0;
        while(jogadorComNumero < NUMERO_DE_JOGADORES){
           byte[] buffer = new byte[50]; 
           DatagramPacket pacote = new DatagramPacket(buffer, buffer.length); // cria um pacote UDP para receber a mensagem e liga esse pacote a um vetor
           this.soqueteServidor.receive(pacote); //espera a msg e quando chegar armazena dentro do vetor num
           
          
           String mensagem = new String(pacote.getData()).trim();
           int numero = Integer.parseInt(mensagem); 
           SocketAddress endereço = pacote.getSocketAddress();
           Jogador jogador = jogadores.get(endereco);

           if(jogador != null && jogador.getNumeroEscolhido() == 0){
                jogador.setNumeroEscolhido(numero); // armazena o numero que ele enviou no objeto jogador
                jogadorComNumero++; // contar quantos envaram e serve para parar o for 
                System.out.println(jogador.getNome() + " escolheu: " + numero);
                 // msg de confirmaçao
                 byte[] confirmacao = "Numero recebido".getBytes();
                 // envia o pacote udp de volta para o endereço do jogador que enviou o numero
                 soqueteServidor.send(new DatagramPacket(confirmacao, confirmacao.length, endereco));

           }

        }
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