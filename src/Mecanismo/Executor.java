package Mecanismo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Executor {

    private final String captureComment = "\\/\\/.*|\\(\\*(?:.|\\s)*\\*\\)";
    private final String captureNumbers = "(?<!\\w)(?:-?\\d+(?:\\.\\d+)?)(?!\\w)";
    private final String captureLiteral = "'(?:[^']|'')*'";
    private final String captureWords   = "\\w+";
    private final String captureCharacters = "(?::=|>=|<=|<>|>|<|=|\\+|\\-|\\*|\\/|[:;,.()\\[\\]{}])";
    private final String captureIdentifier = "^[A-Za-z][A-Za-z0-9_]*$";
    private String capture;

    private BufferedReader reader;
    private ArrayList<String> bufferPrimario;
    private ArrayList<String> bufferSecundario;

    private HashMap<String, Token> tabelaSimbolosPrograma;

    private boolean IsNumber(String valor)
    {
        Pattern pattern = Pattern.compile(this.captureNumbers); //ARMAZENA A EXPRESSAO
        Matcher matcher = pattern.matcher(valor); //AVALIAR A EXPRESSAO REGULAR
        if (matcher.find() == true){
            return true;
        }
        return false;
    }

    private boolean IsLiteral(String valor)
    {
        Pattern pattern = Pattern.compile(this.captureLiteral);
        Matcher matcher = pattern.matcher(valor);
        if (matcher.find() == true){
            return true;
        }
        return false;
    }

    private boolean IsCharacter(String valor)
    {
        Pattern pattern = Pattern.compile(this.captureCharacters);
        Matcher matcher = pattern.matcher(valor);
        if (matcher.find() == true){
            return true;
        }
        return false;
    }

    private boolean IsIdentifier(String valor)
    {
        Pattern pattern = Pattern.compile(this.captureIdentifier);
        Matcher matcher = pattern.matcher(valor);
        if (matcher.find() == true){
            return true;
        }
        return false;
    }

    public void CarregarArquivo(){
        System.out.println("----------------------------------------");
        System.out.println("##### Carregar Arquivo Pascal #####");
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite o diretório do arquivo: ");
        String diretorio = scanner.next();
        System.out.print("Digite o nome do arquivo (com extensão .pas): ");
        String nomeArquivo = scanner.next();
        String caminhoCompleto = diretorio + "/" + nomeArquivo;
        this.CarregarBufferPrimario(caminhoCompleto);
        scanner.close();        
    }

    public void CarregarArquivo(String caminhoCompleto){
        System.out.println("----------------------------------------");
        System.out.println("##### Carregar Arquivo Pascal #####");
        this.CarregarBufferPrimario(caminhoCompleto);
    }

    private void CarregarBufferPrimario(String caminhoCompleto){
        this.reader = null;
        try {
            this.reader = new BufferedReader(new FileReader(caminhoCompleto));
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }

    public void ProcessarBufferPrimario(){
        this.bufferPrimario = new ArrayList<>();
        try {
            String linha;
            while ((linha = this.reader.readLine()) != null) {
                bufferPrimario.add(linha);
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println("Erro ao fechar o arquivo: " + e.getMessage());
                }
            }
        }
    }

    public void ImprimirBufferPrimario(){
        System.out.println("----------------------------------------");
        System.out.println("##### Conteúdo do Buffer primário: #####");
        for (String texto : this.bufferPrimario) {
            System.out.println(texto);
        }
        System.out.println("----------------------------------------");        
    }

    public void ProcessarBufferSecundario()
    {        
        this.capture = captureComment.concat("|")
            .concat(captureNumbers).concat("|")
            .concat(captureLiteral).concat("|")
            .concat(captureWords).concat("|")
            .concat(captureCharacters);

        this.bufferSecundario = new ArrayList<>();

        Pattern pattern = Pattern.compile(this.capture);

        for (String texto : bufferPrimario) { //olha linha por linha que esta armazenado no
                                            //bufferprimario pra confirmar se é lexema
            Matcher matcher = pattern.matcher(texto);
            while(matcher.find()){
                String lexema = matcher.group();
                if (this.bufferSecundario.contains(lexema) == false){
                    this.bufferSecundario.add(lexema);
                }
            }
        }
        bufferSecundario.removeIf(value -> value.startsWith("//") || value.startsWith("(*"));
    } 
    
    public void ImprimirBufferSecundario(){
        System.out.println("----------------------------------------");
        System.out.println("##### Conteúdo do Buffer secundário: #####");
        for (String texto : this.bufferSecundario) {
            System.out.println(texto);
        }
        System.out.println("----------------------------------------");        
    }

    public void AnalisarMontandoTabelaSimbolos(){
        //AQUI É QUE ENTRA O TRABALHO DE VCS.
        //1 - Precisa da Tabela de Simbolos do programa. (vazio) montar a nossa comparando com a tabela ja preenchida, linha por linha
        //2 - Precisa da Tabela de Simbolos da linguagem. (ja preenchida)
        //3 - Precisa varrer o buffer secundário, para localizar os tokens, definindo o que é cada um dos lexemas. (precisamo preencher a nossa tabela)
        
        this.tabelaSimbolosPrograma = new HashMap<>();
        TabelaSimbolosLinguagem tabelaLinguagem = new TabelaSimbolosLinguagem();

        int end = 0;
        for (String texto : this.bufferSecundario) { //passa por todo buffer

            end++;
            Token token = new Token();

            if (tabelaLinguagem.getTabela().containsKey(texto.toLowerCase())) { //verifica na tabela linguagem se contem a palavra chave
                String chave = texto.toUpperCase();
                token = tabelaLinguagem.getTabela().get(texto.toLowerCase()); //verificou na tabela ent ele busca qual a keyword
                token.setToken(texto.toUpperCase());
                this.tabelaSimbolosPrograma.put(chave, token);
            }
            else if (IsNumber(texto)) { //se ela for numero cria esse token
                token = new Token("<" + texto + ", NUM>", texto, "Numero", texto, end);
            }
            else if (IsLiteral(texto)) { //se ela for literal cria esse token
                token = new Token("<" + texto + ", LIT>", texto, "Literal", texto, end);
            }
            else if (IsCharacter(texto)) { //se ela for char cria esse token
                token = new Token("<" + texto + ", SIMB>", texto, "Símbolo", texto, end);
            }
            else if (IsIdentifier(texto)) { // se for id cria esse
                token = new Token("<" + texto + ", ID>", texto, "Identificador", texto, end);
            } else {
                token = new Token(texto, texto, tabelaLinguagem.getUndefined(), texto, end);
            }

            this.tabelaSimbolosPrograma.put(texto, token);

        }
    }

    public void ImprimirTabelaSimbolosPrograma(){
        //A parte final, na qual vc imprime todas as entradas da Tabela de Simbolos do programa, após o processamento.
        //abrir o hashmap
        System.out.println("\n##### Tabela de Simbolos do Programa: #####\n");
        for (Token t : tabelaSimbolosPrograma.values()) {
            System.out.println(t);
        }
    }
}

