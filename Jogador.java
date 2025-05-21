package sobrevivencia_numerica;

import java.net.SocketAddress;

public class Jogador {
    private String nome;
    private SocketAddress enderecoSoquete;
    private int pontuacao;
    private int numeroEscolhido;
    
    public Jogador(String nome, SocketAddress enderecoSoquete){
        this.nome = nome;
        this.enderecoSoquete = enderecoSoquete;
    }

    public double calcularDiferença(double valorAlvo){
        return Math.abs(valorAlvo - this.numeroEscolhido);
    }

    public String getNome() {
        return nome;
    }
    
    public SocketAddress getEnderecoSoquete() {
        return enderecoSoquete;
    }
    
    public int getNumeroEscolhido() {
        return numeroEscolhido;
    }
    
    public void setNumeroEscolhido(int numeroEscolhido) {
        this.numeroEscolhido = numeroEscolhido;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public void setPontuacao(int pontuacao) {
        this.pontuacao = pontuacao;
    }
}
