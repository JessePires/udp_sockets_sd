## Como executar?

1. Compile os arquivos
```bash
javac file_upload/*.java 
```
2. Execute o servidor em um terminal
```bash
javac file_upload.Server 
```

3. Execute o cliente em outro terminal 
```bash
javac file_upload.Cliente 
```

## Como fazer upload de arquivos?

1. O arquivo a ser feito upload deve estar na pasta raiz do projeto, ou seja, fora da pasta `file_upload`
2. Para realizar o upload, basta executar o seguinte comando no `Cliente`:
```bash
UPLOAD <nome_do_arquivo>

# Exemplo
# UPLOAD calendario.pdf
# UPLOAD java.java
# UPLOAD CM.zip
```
