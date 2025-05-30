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

        System.out.println("Digite 1 - para ver as regras do jogo.");
        System.out.println("Digite 2 - para iniciar o jogo.");
        System.out.println("Digite 3 - para sair do jogo.");

        int PONTUACAO_DE_ELIMINACAO = -6;
        
        DatagramSocket soquete = null;

        try {
            System.out.println("Iniciando o jogo...");
            soquete = new DatagramSocket();
            InetAddress enderecoServidor = InetAddress.getByName("localhost");
            int portaServidor = 57_651;
            
            String status;
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
                System.out.println("Escolha um número entre 0 e 100: ");
                int numero;
                do {
                    numero = sc.nextInt();
                } while(numero < 0 || numero > 100);

                System.out.println("Você escolheu o número: " + numero + ".");

                System.out.println("Enviando o número escolhido para o servidor do jogo...");
                // ......... mais coisa para imprimir
                byte[] conteudo2 = String.valueOf(numero).getBytes();
                DatagramPacket requisicao2 = new DatagramPacket(conteudo2, conteudo2.length, enderecoServidor, portaServidor);
                soquete.send(requisicao2);

                System.out.println("Aguardando a atualização de seu placar...");
                byte[] conteudo3 = new byte[100];
                DatagramPacket resposta2 = new DatagramPacket(conteudo3, conteudo3.length);
                soquete.receive(resposta2);

                String[] mensagem = new String(resposta2.getData()).trim().split(" ");
                placar = Integer.parseInt(mensagem[0]);
                status = mensagem[1];
            
                System.out.println("Seu placar é: " + placar);

            } while(status.equals("CONTINUE"));

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
}
