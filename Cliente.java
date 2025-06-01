package sobrevivencia_numerica;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Jogo da Sobrevivência Numérica");
        System.out.print("Digite seu nickname: ");
        String nome = sc.nextLine();

        System.out.println("Bem-vindo(a), " + nome);

        int escolha;
        do {
            System.out.println("Digite 1 - para ver as regras do jogo.");
            System.out.println("Digite 2 - para iniciar o jogo.");
            System.out.println("Digite 3 - para sair do jogo.");
            System.out.println("O que deseja?: ");
            escolha = sc.nextInt();

            switch (escolha) {
                case 1:
                    mostrarRegras();
                    break;
                case 2:
                    iniciarJogo(nome, sc);
                    break;
                case 3:
                    System.out.println("Saindo do jogo...");
                    break;
            
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        } while (escolha != 2 && escolha != 3);
        
        sc.close();
    }
    public static void iniciarJogo(String nome, Scanner sc){
        int PONTUACAO_DE_ELIMINACAO = -6;
        int PONTUACAO_DE_VITORIA = 10;
        
        DatagramSocket soquete = null;

        try {
            System.out.println("Iniciando o jogo...");
            soquete = new DatagramSocket();
            InetAddress enderecoServidor = InetAddress.getByName("localhost");
            int portaServidor = 57_651;
            
            int placar;
           
            DatagramPacket requisicao = new DatagramPacket(nome.getBytes(), nome.getBytes().length, enderecoServidor, portaServidor);

            System.out.println("Enviando seu cadastro para o servidor do jogo...");
            soquete.send(requisicao);
            
            System.out.println("Aguardando jogadores oponentes...");

            byte[] conteudo = new byte[1000];
            DatagramPacket resposta = new DatagramPacket(conteudo, conteudo.length);
            soquete.receive(resposta);
            
            System.out.println(new String(resposta.getData()));

            do {
                int numero;
                do {
                    System.out.println("Escolha um número entre 0 e 100: ");
                    numero = sc.nextInt();
                } while(numero < 0 || numero > 100);

                System.out.println("Você escolheu o número: " + numero + ".");

                System.out.println("Enviando o número escolhido para o servidor do jogo...");
                // ......... mais coisa para imprimir
                byte[] conteudo2 = String.valueOf(numero).getBytes();
                DatagramPacket requisicao2 = new DatagramPacket(conteudo2, conteudo2.length, enderecoServidor, portaServidor);
                soquete.send(requisicao2);

                System.out.println("Aguardando a atualização de seu placar...");
                byte[] conteudo3 = new byte[10];
                DatagramPacket resposta2 = new DatagramPacket(conteudo3, conteudo3.length);
                soquete.receive(resposta2);

                placar = Integer.parseInt(new String(resposta2.getData()).trim());
            
                System.out.println("Seu placar é: " + placar);

            } while(placar > PONTUACAO_DE_ELIMINACAO && placar != PONTUACAO_DE_VITORIA);

            if(placar <= PONTUACAO_DE_ELIMINACAO){
                System.out.println("Você foi eliminado(a)!");
            } else {
                System.out.println("Você foi o(a) vencedor(a)!");
            }


        } catch (SocketException e) {
            System.out.println("Socket " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO " + e.getMessage());
        } finally {
            if (soquete != null)
                soquete.close();
            sc.close();
        }
    }

    public static void mostrarRegras(){
        System.out.println("\n=====================================================================");
        System.out.println("REGRAS DO JOGO SOBREVIVÊNCIA NUMÉRICA");
        System.out.println("=====================================================================");
        System.out.println("No início três jogadores jogam, escolhendo um número entre 0 e 100.");
        System.out.println("O Servidor do jogo receberá os três números escolhidos e calculará a média dos valores recebidos.");
        System.out.println("O resultado das médias é então multiplicado por 0,8.");
        System.out.println("Este novo valor resultante será o valor alvo.");
        System.out.println("O valor alvo é comparado com os valores que cada jogador escolheu.");
        System.out.println("O jogador que mais se distanciou do valor alvo, perde dois pontos.");
        System.out.println(" O jogador que mais se aproximou do valor alvo, não perde pontos.");
        System.out.println("O outro jogador perde apenas um ponto.");
        System.out.println("O jogador que chegar a menos seis pontos, primeiro, será eliminado definitivamente do jogo.");
        System.out.println("Quando restarem apenas dois jogadores, as regras do jogo mudam.");
        System.out.println("O jogador que mais se distanciar do valor alvo, perde um ponto.");
        System.out.println("O outro jogador, não perde pontos.");
        System.out.println("O jogador que primeiro chegar a menos seis pontos, será eliminado do jogo.");
        System.out.println("O último jogador é declarado vencedor do Jogo da Sobrevivência Numérica.");
        System.out.println("=====================================================================\n");
    }
}
