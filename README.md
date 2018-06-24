# ChatRabbitMQ

Este README vai explicar algumas pequenas mudanças realizadas pelo aluno Luis Felipe em relação ao que foi sugerido pelo professor.
Estas mudanças dizem respeito aos comandos utilizados para a criação e gerenciamento de grupos no chat. Também vai contar porque 
a etapa 2 do projeto ainda não está completa.

## Comandos

Com o objetivo de fazer comparações com strings menores (usando o método startsWith()), foram realizadas as seguintes modificações:

### Criar grupo

O sugerido foi utilizar o comando `!addGroup` mas, por questões de nomenclatura, aqui se usa o comando `!newGroup`, que será 
atribuído à variável `msg` e comparado na linha `msg.startsWith("!new")`. Esta medida foi dadotada também para evitar confusão
com o comando seguinte.

### Comandos de usuários e apagar grupo

O sugerido foi utilizar os comandos `!addUser`, `!delFromGroup` e `!removeGroup`. Estes comandos foram mantidos, e serão 
atribuídos à variável `msg` e comparados nas linhas `msg.startsWith("!add")`, `msg.startsWith("!del")` e `msg.startsWith("!rem")`,
respectivamente.

## Por que ainda não está pronto

Ainda não foi possível testar todas as mudanças por conta de um problema com o servidor do RabbitMQ hospedado em uma máquina virtual
da Amazon Web Services. A conta do estudante teve seu laboratório fechado aparentemente sem motivo e por isso não é possível acessar
o console de gerenciamento da máquina. Para completar, a máquina parece ter caído da rede no momento em que este README foi escrito
(23/06/2018, 23h52). Logo, não foi possível para o programa acessar o servidor e realizar os devidos testes.