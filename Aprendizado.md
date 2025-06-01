Aqui está a explicação completa do jogo **Sobrevivência Numérica** em formato **Markdown (.md)**:

````markdown
# 🕹️ Jogo: Sobrevivência Numérica

## 🎯 Objetivo
Três jogadores enviam números entre 0 e 100. O servidor calcula uma média, transforma em um **valor alvo** e aplica penalizações de acordo com a distância de cada jogador em relação a esse valor. O último jogador sobrevivente vence o jogo.

---

## 🧱 Estrutura do Sistema

### 📁 Classes do Projeto

#### 🔹 `Jogador.java`
Representa cada jogador.

```java
private String nome;
private InetAddress enderecoIP;
private int porta;
private int pontuacao;
private int numeroEscolhido;
````

* Armazena nome, IP, porta, pontuação e número escolhido.
* Método: `calcularDiferença(valorAlvo)` → retorna a diferença do número para o alvo.

---

#### 🔹 `Cliente.java`

Interface do jogador.

### Fluxo:

1. Solicita nome do jogador.
2. Apresenta menu: Regras, Iniciar Jogo ou Sair.
3. Envia o nome para o servidor.
4. Escolhe números entre 0 e 100 por rodada.
5. Recebe o placar.
6. Sai do jogo ao ser eliminado ou vencer.

---

#### 🔹 `Juiz.java`

Servidor que controla o jogo.

### Atributos:

```java
final int NUMERO_DE_JOGADORES = 3;
final int PONTUACAO_DE_ELIMINACAO = -6;
private int PONTUACAO_DE_VITORIA = 10;
private Jogador[] jogadores;
private DatagramSocket soqueteServidor;
private int contadorDeJogadores;
```

---

## 🔄 Etapas do Funcionamento

### 1. **mostrarRegras()**

Exibe as regras do jogo.

---

### 2. **Construtor Juiz(DatagramSocket)**

Cria o vetor de jogadores e armazena o socket do servidor.

---

### 3. **aguardarJogadores()**

* Recebe os nomes via UDP.
* Cria objetos `Jogador` com nome, IP e porta.
* Armazena no array `jogadores`.

---

### 4. **iniciarJogo()**

Executa o jogo por rodadas:

1. Envia confirmação de início para os jogadores.
2. Cria uma `List<Jogador>` com os jogadores ativos.
3. Entra em loop:

   * Recebe números dos jogadores.
   * Calcula média e valor alvo.
   * Aplica penalidades.
   * Envia placares.
   * Elimina jogadores com pontuação ≤ -6.
   * Se restar 1, declara vencedor.

---

### 5. **receberNumerosEscolhidos()**

* Recebe o número de cada jogador via UDP.
* Armazena no objeto `Jogador`.

---

### 6. **calcularMedia()**

```java
double media = somaDosNumeros / jogadoresAtivos.size();
```

---

### 7. **calcularValorAlvo()**

```java
double valorAlvo = media * 0.8;
```

---

### 8. **calcularPlacar()**

* Calcula diferença de cada jogador para o valor alvo.
* Ordena por aproximação.
* Aplica pontuação:

  * 3 jogadores: 0 / -1 / -2.
  * 2 jogadores: 0 / -1.

---

### 9. **enviarPlacarDosJogadores()**

* Envia placar atualizado para cada jogador.

---

### 10. **finalizarJogo()**

* Fecha o `DatagramSocket`.

---

### 11. **main() do servidor**

```java
DatagramSocket soqueteServidor = new DatagramSocket(57651);
Juiz juiz = new Juiz(soqueteServidor);
juiz.mostrarRegras();
juiz.aguardarJogadores();
juiz.iniciarJogo();
juiz.finalizarJogo();
```

---

## 📈 Regras do Jogo (Resumo)

* Todos os jogadores escolhem um número.
* A média dos valores é multiplicada por 0.8 → valor alvo.
* Quem mais se aproxima não perde pontos.
* Penalizações:

  * 3 jogadores: -2 (mais distante), -1 (intermediário), 0 (mais próximo).
  * 2 jogadores: -1 (mais distante), 0 (mais próximo).
* Quem chegar a **-6 pontos é eliminado**.
* Último sobrevivente vence.

---

## 🏁 Fim do Jogo

* Ao restar 1 jogador:

  * Ele recebe pontuação 10.
  * Os outros já estão eliminados.
  * Mostra o placar final.

---

```

Se quiser, posso salvar esse conteúdo como um arquivo `.md` pronto para você baixar. Deseja isso?
```
