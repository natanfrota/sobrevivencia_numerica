import java.net.InetAddress;

public class Competidor {
    private String nome;
    private InetAddress enderecoIP;
    private int porta;
    private int pontuacao;
    private int numeroEscolhido;
    
    public Competidor(String nome, InetAddress enderecoIP, int porta){
        this.nome = nome;
        this.enderecoIP = enderecoIP;
        this.porta = porta;
    }

    public double calcularDiferença(double valorAlvo){
        return Math.abs(valorAlvo - this.numeroEscolhido);
    }

    public String getNome() {
        return nome;
    }
    
    public InetAddress getEnderecoIP() {
        return enderecoIP;
    }

    public int getPorta() {
        return porta;
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
