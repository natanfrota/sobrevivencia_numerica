package sobrevivencia_numerica;

import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import java.io.*;

public class Juiz {
    final int NUMERO_DE_JOGADORES = 3;
    final int PONTUACAO_DE_ELIMINACAO = -6;
    private int PONTUACAO_DE_VITORIA = 10;
    private Jogador[] jogadores;
    private DatagramSocket soqueteServidor;
    private int contadorDeJogadores;

    public Juiz(DatagramSocket soqueteServidor) {
        this.soqueteServidor = soqueteServidor;
        this.jogadores = new Jogador[NUMERO_DE_JOGADORES];
        this.contadorDeJogadores = 0;
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

    public void aguardarJogadores() throws IOException {
        System.out.println("Aguardando os jogadores...");

        while (this.contadorDeJogadores < this.NUMERO_DE_JOGADORES) {
            byte[] dado = new byte[50]; // armazena o nome do jogador que vem na requisição
            DatagramPacket requisicao = new DatagramPacket(dado, dado.length);
            this.soqueteServidor.receive(requisicao);

            // cria um novo jogador
            Jogador novoJogador = new Jogador(new String(requisicao.getData()), requisicao.getAddress(), requisicao.getPort());

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
            Jogador jogador = null;

            for (Jogador j : jogadoresAtuais){
                if(j.getEnderecoIP().equals(pacote.getAddress()) && j.getPorta() == pacote.getPort())
                    jogador = j;
            }

            if (jogador != null) {
                jogador.setNumeroEscolhido(numero); // armazena o numero que ele enviou no objeto jogador
                contadorDeNumeros++; // contar quantos enviaram e serve para parar o for
                System.out.println("Jogador(a) " + jogador.getNome() + " escolheu o número: " + jogador.getNumeroEscolhido());
            }
        }
        System.out.println("");
    }

    public void iniciarJogo() throws IOException {
        if (this.contadorDeJogadores != this.NUMERO_DE_JOGADORES)
            return;
        
        enviarConfirmacao(); // avisa aos jogadores que o jogo vai começar

        System.out.println("\nQue comecem os jogos...");

        // converte o vetor para lista para remover os eliminados enquanto os mantém no vetor original
        List<Jogador> jogadoresAtuais = new ArrayList<>(Arrays.asList(this.jogadores));

        System.out.println("\nAguardando os números de cada jogador...");

        while (true) {

            receberNumerosEscolhidos(jogadoresAtuais);

            System.out.println("\nRealizando cálculo da média...");
            double media = this.calcularMedia(jogadoresAtuais);
            System.out.println("Média encontrada...");
            System.out.printf("Média: %.2f\n", media);

            System.out.println("\nRealizando cálculo do valor alvo...");
            double valorAlvo = this.calcularValorAlvo(media);
            System.out.println("Valor alvo encontrado...");
            System.out.printf("Valor alvo: %.2f\n", valorAlvo);

            System.out.println("\nRealizando cálculo do placar do jogo...\n");
            this.calcularPlacar(jogadoresAtuais, valorAlvo);

            System.out.println("\nAtualizando quantidade de jogadores...");
            for(Jogador j : jogadoresAtuais){
                if(j.getPontuacao() <= this.PONTUACAO_DE_ELIMINACAO){
                    this.contadorDeJogadores--;
                }
            }

            /* se restar apenas um jogador, a repetição termina e ele será considerado o vencedor */
            if(this.contadorDeJogadores == 1){

                System.out.println("Fim de Jogo!");

                /* busca o jogador vencedor */
                Jogador vencedor = null;
                for (Jogador jogador : jogadoresAtuais) {
                    if(jogador.getPontuacao() != this.PONTUACAO_DE_ELIMINACAO)
                        vencedor = jogador;
                }

                System.out.println("Jogador(a) " + vencedor.getNome() + " venceu o jogo!!!");
                System.out.println("Enviando placar " + this.PONTUACAO_DE_VITORIA +" para o(a) primeiro(a) jogador(a), o(a) vencedor(a).");
                byte[] placar = String.valueOf(this.PONTUACAO_DE_VITORIA).getBytes();
                DatagramPacket resposta = new DatagramPacket(placar, placar.length, vencedor.getEnderecoIP(), vencedor.getPorta());
                soqueteServidor.send(resposta);

                enviarPlacarDosJogadores(jogadoresAtuais);
                
                break;
            }

            enviarPlacarDosJogadores(jogadoresAtuais);

            System.out.println("\nVerificando se algum jogador foi eliminado...\n");

            Iterator<Jogador> it = jogadoresAtuais.iterator();
            while (it.hasNext()) {
                Jogador jogador = it.next();
                if (jogador.getPontuacao() <= this.PONTUACAO_DE_ELIMINACAO) {
                    System.out.println("Jogador(a) " + jogador.getNome() + " foi eliminado(a).");
                    it.remove();
                }
            }
        }
        
        System.out.println("\nPlacar final: ");
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
            DatagramPacket resposta = new DatagramPacket(placar, placar.length, jogador.getEnderecoIP(), jogador.getPorta());
            soqueteServidor.send(resposta);

            i++;
        }
    }

    private void enviarConfirmacao() throws IOException {
        byte[] confirmacao = "Jogadores oponentes encontrados...\n\nQue comecem os jogos...\n".getBytes();

        String[] posicoes = { "primeiro", "segundo", "terceiro" };
        int i = 0;

        for (Jogador jogador : jogadores) {
            System.out.println("Enviando autorização de início de jogo para " + posicoes[i] + " jogador.");
            DatagramPacket resposta2 = new DatagramPacket(confirmacao, confirmacao.length, jogador.getEnderecoIP(), jogador.getPorta());
            this.soqueteServidor.send(resposta2);

            i++;
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
        System.out.println("");

        /* ordena os jogadores de acordo com a diferença do valor alvo de cada um */
        for (int i = 0; i < tempJogadores.length - 1; i++) {
            for (int j = i + 1; j < tempJogadores.length; j++) {
                if (tempJogadores[i].calcularDiferença(valorAlvo) > tempJogadores[j].calcularDiferença(valorAlvo)) {
                    Jogador temp = tempJogadores[i];
                    tempJogadores[i] = tempJogadores[j];
                    tempJogadores[j] = temp;
                } 
                else if(tempJogadores[i].calcularDiferença(valorAlvo) == tempJogadores[j].calcularDiferença(valorAlvo)){
                    System.out.println("Diferenças iguais.");
                    return;
                }
            }
        }

        /* mudar a pontuacao de cada jogador */
        if (jogadoresAtuais.size() == this.NUMERO_DE_JOGADORES) { // cálculo para três jogadores
            // jogador com maior distância
            System.out.println("Jogador(a) " + tempJogadores[2].getNome() + " receberá -2 pontos.");
            tempJogadores[2].setPontuacao(tempJogadores[2].getPontuacao() - 2);

            // jogador com menor distância
            System.out.println("Jogador(a) " + tempJogadores[0].getNome() + " receberá 0 ponto.");
            tempJogadores[0].setPontuacao(tempJogadores[0].getPontuacao() - 0);

            // jogador com distância média
            System.out.println("Jogador(a) " + tempJogadores[1].getNome() + " receberá -1 ponto.");
            tempJogadores[1].setPontuacao(tempJogadores[1].getPontuacao() - 1);

        } else { // se houver apenas dois jogadores, o último perde um ponto

            System.out.println("Jogador(a) " + tempJogadores[1].getNome() + " receberá -1 ponto.");
            tempJogadores[1].setPontuacao(tempJogadores[1].getPontuacao() - 1);
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