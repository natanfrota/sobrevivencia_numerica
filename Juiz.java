package sobrevivencia_numerica;

import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.*;

public class Juiz {
    final int NUMERO_DE_JOGADORES = 3;
    final int PONTUACAO_DE_ELIMINACAO = -6;
    private Jogador[] jogadores;
    private DatagramSocket soqueteServidor;
    private int contadorDeJogadores;

    public Juiz(DatagramSocket soqueteServidor) {
        this.soqueteServidor = soqueteServidor;
        this.jogadores = new Jogador[NUMERO_DE_JOGADORES];
        this.contadorDeJogadores = 0;
    }

    public static void mostrarRegras(){
        System.out.println("=============== REGRAS DO JOGO SOBREVIVÊNCIA NUMÉRICA ===============");
        System.out.println("No início três jogadores jogam, escolhendo um número entre 0 e 100.");
        System.out.println("O Servidor do jogo receberá os três números escolhidos e calculará a média dos valores recebidos.");
        System.out.println("O resultado das médias é então multiplicado por 0,8.")
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
        System.out.println("=====================================================================");
    }

    public void aguardarJogadores() throws IOException {
        System.out.println("Aguardando os jogadores...");

        while (this.contadorDeJogadores < this.NUMERO_DE_JOGADORES) {
            byte[] dado = new byte[50]; // armazena o nome do jogador que vem na requisição
            DatagramPacket requisicao = new DatagramPacket(dado, dado.length);
            this.soqueteServidor.receive(requisicao);

            // cria um novo jogador
            Jogador novoJogador = new Jogador(new String(requisicao.getData()), requisicao.getSocketAddress());

            // armazena-o
            this.jogadores[contadorDeJogadores] = novoJogador;

            System.out.println("Jogador(a): " + novoJogador.getNome() + " entrou no jogo.");
            System.out.println("IP: " + requisicao.getAddress());
            System.out.println("Porta: " + requisicao.getPort() + "\n");

            this.contadorDeJogadores++;
        }
    }

    private void receberNumerosEscolhidos(List<Jogador> jogadoresAtuais) throws IOException {
        int contadorDeNumeros = 0;
        while (contadorDeNumeros < jogadoresAtuais.size()) {
            byte[] buffer = new byte[50];
            DatagramPacket pacote = new DatagramPacket(buffer, buffer.length); // cria um pacote UDP para receber a mensagem e liga esse pacote a um vetor
            this.soqueteServidor.receive(pacote); // espera a msg e quando chegar armazena dentro do vetor num

            String mensagem = new String(pacote.getData()).trim();
            int numero = Integer.parseInt(mensagem);
            SocketAddress endereco = pacote.getSocketAddress();
            Jogador jogador = null;

            for (Jogador j : jogadoresAtuais){
                if(j.getEnderecoSoquete().equals(endereco))
                    jogador = j;
            }

            if (jogador != null) {
                jogador.setNumeroEscolhido(numero); // armazena o numero que ele enviou no objeto jogador
                contadorDeNumeros++; // contar quantos enviaram e serve para parar o for
                System.out.println("Jogador(a) " + jogador.getNome() + " escolheu o número: " + jogador.getNumeroEscolhido());
            }

        }
    }

    public void iniciarJogo() throws IOException {
        if (this.contadorDeJogadores != this.NUMERO_DE_JOGADORES)
            return;
        
        enviarConfirmacao(); // avisa aos jogadores que o jogo vai começar

        // converte o vetor para lista para remover os eliminados enquanto os mantém no vetor original
        List<Jogador> jogadoresAtuais = new ArrayList<>(Arrays.asList(this.jogadores));

        System.out.println("Aguardando os números de cada jogador...");

        while (jogadoresAtuais.size() > 1) { // o jogo continua enquanto houver mais de um jogador

            receberNumerosEscolhidos(jogadoresAtuais);

            System.out.println("Realizando cálculo da média...");
            double media = this.calcularMedia(jogadoresAtuais);
            System.out.println("Média encontrada...");
            System.out.printf("\nMédia: %.2f\n", media);

            System.out.println("\nRealizando cálculo do valor alvo...");
            double valorAlvo = this.calcularValorAlvo(media);
            System.out.println("Valor alvo encontrado...");
            System.out.printf("\nValor alvo: %.2f\n", valorAlvo);

            System.out.println("Realizando cálculo do placar do jogo...");
            this.calcularPlacar(jogadoresAtuais, valorAlvo);

            System.out.println("Atualizando quantidade de jogadores...");

            

            enviarPlacarDosJogadores(jogadoresAtuais);

            System.out.println("Verificando se algum jogador foi eliminado...");

            Jogador elemento = null;
            for (Jogador jogador : jogadoresAtuais) {
                if (jogador.getPontuacao() <= this.PONTUACAO_DE_ELIMINACAO) {
                    elemento = jogador;
                }
            }
            if (elemento != null) {
                jogadoresAtuais.remove(elemento);
                System.out.println("Jogador(a) " + elemento.getNome() + " foi eliminado(a).");
            }
        }
        
        System.out.println("Placar final: ");
        for (Jogador jogador : this.jogadores) {
            System.out.println("Jogador " + jogador.getNome() + " = " + jogador.getPontuacao());
        }
    }

    private void enviarPlacarDosJogadores(List<Jogador> jogadoresAtuais) throws IOException {
        String[] posicoes = { "primeiro", "segundo", "terceiro" };
        int i = 0;

        for (Jogador jogador : jogadoresAtuais) {
            System.out.println("Enviando placar para " + posicoes[i] + " jogador " + jogador.getPontuacao());

            byte[] placar = String.valueOf(jogador.getPontuacao()).getBytes();
            DatagramPacket resposta = new DatagramPacket(placar, placar.length, jogador.getEnderecoSoquete());
            soqueteServidor.send(resposta);

            i++;
        }
    }

    private void enviarConfirmacao() throws IOException {
        byte[] confirmacao = "Jogadores oponentes encontrados...\n\nQue comecem os jogos...\n".getBytes();

        for (Jogador jogador : jogadores) {
            DatagramPacket resposta2 = new DatagramPacket(confirmacao, confirmacao.length, jogador.getEnderecoSoquete());
            this.soqueteServidor.send(resposta2);
        }
    }

    private double calcularMedia(List<Jogador> jogadoresAtuais) {
        int soma = 0;

        for (Jogador jogador : jogadoresAtuais) {
            soma += jogador.getNumeroEscolhido();
        }

        double media = (double) soma / jogadoresAtuais.size();

        return media;
    }

    private double calcularValorAlvo(double media) {
        return media * 0.8;
    }

    private void calcularPlacar(List<Jogador> jogadoresAtuais, double valorAlvo) {
        Jogador[] tempJogadores = jogadoresAtuais.toArray(new Jogador[jogadoresAtuais.size()]);

        for (Jogador jogador : tempJogadores) {
            System.out.printf("Distância do valor alvo do(a) jogador(a) - %s = %f\n", jogador.getNome(),jogador.calcularDiferença(valorAlvo));
        }

        if (jogadoresAtuais.size() == this.NUMERO_DE_JOGADORES) { // cálculo para três jogadores

            for (int i = 0; i < tempJogadores.length - 1; i++) {
                for (int j = i + 1; j < tempJogadores.length; j++) {
                    if (tempJogadores[i].calcularDiferença(valorAlvo) > tempJogadores[j].calcularDiferença(valorAlvo)) {
                        Jogador temp = tempJogadores[i];
                        tempJogadores[i] = tempJogadores[j];
                        tempJogadores[j] = temp;
                    }
                }
            }

            /* mudar a pontuacao de cada jogador */
            // jogador com maior distância
            System.out.println("Jogador(a) " + tempJogadores[2].getNome() + " receberá -2 pontos.");
            tempJogadores[2].setPontuacao(tempJogadores[2].getPontuacao() - 2);

            // jogador com menor distância
            System.out.println("Jogador(a) " + tempJogadores[0].getNome() + " receberá 0 ponto.");
            tempJogadores[0].setPontuacao(tempJogadores[0].getPontuacao() - 0);

            // jogador com distância média
            System.out.println("Jogador(a) " + tempJogadores[1].getNome() + " receberá -1 ponto.");
            tempJogadores[1].setPontuacao(tempJogadores[1].getPontuacao() - 1);

        } else { // cálculo para dois jogadores

            if (tempJogadores[0].calcularDiferença(valorAlvo) > tempJogadores[1].calcularDiferença(valorAlvo)) {

                System.out.println("Jogador(a) " + tempJogadores[0].getNome() + " receberá -1 ponto.");
                tempJogadores[0].setPontuacao(tempJogadores[0].getPontuacao() - 1);

            } else {
                System.out.println("Jogador(a) " + tempJogadores[1].getNome() + " receberá -1 ponto.");
                tempJogadores[1].setPontuacao(tempJogadores[1].getPontuacao() - 1);
            }

            if(tempJogadores[0].getPontuacao() <= this.PONTUACAO_DE_ELIMINACAO){
                tempJogadores[1].setPontuacao(10);
            } else if(tempJogadores[1].getPontuacao() <= this.PONTUACAO_DE_ELIMINACAO){
                tempJogadores[0].setPontuacao(10);
            }
        }
    }

    public void finalizarJogo() {
        this.soqueteServidor.close();
    }

    public static void main(String[] args) {
        final int NUMERO_DA_PORTA = 57_651;
        Juiz juiz = null;

        try {
            DatagramSocket soqueteServidor = new DatagramSocket(NUMERO_DA_PORTA);
            juiz = new Juiz(soqueteServidor);
            mostrarRegras();
            juiz.aguardarJogadores();
            juiz.iniciarJogo();
            juiz.finalizarJogo();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}