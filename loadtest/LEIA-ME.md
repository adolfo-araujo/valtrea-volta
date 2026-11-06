# Teste de carga do Valtrea Volta

Mede quantas pessoas o Valtrea Volta aguenta ao mesmo tempo, **no seu computador**, sem gastar minutos do GitHub.
Nada é enviado para a internet: banco, API e e-mails são cópias locais e apagadas no fim.

## Como rodar (Windows)

1. Instale e abra o **Docker Desktop** (grátis): https://www.docker.com/products/docker-desktop/
2. Dê dois cliques em `loadtest/rodar-teste-de-carga.bat`.
3. Espere. A primeira vez demora mais (baixa e compila a API). O teste em si leva cerca de 5 minutos.
4. O resultado fica em `loadtest/resultado/resumo.md`. Mande para o Claude se quiser que ele analise.

## Cenário

- Por padrão: **100 pessoas** ao mesmo tempo, API com **0,1 CPU e 512 MB** (plano grátis do Render, o atual).
- Para outro cenário, edite as linhas `set USUARIOS=` e `set CPUS=` no começo do `.bat` (ex.: `CPUS=0.5` para o plano Starter).
- Critério para passar (o mesmo do Finance e do Pay): 95% das respostas em menos de 1 s (login: 2 s) e menos de 1% de erros.
- O teste não usa o banco de dados que está no ar nem manda e-mails de verdade.
