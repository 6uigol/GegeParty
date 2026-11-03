# 🎈 GegeParty

App Android (Kotlin + Jetpack Compose + Firebase) para organizar o aniversário da Baixinha a dois.

## O que tem
- **Início**: contagem regressiva, % da festa pronta, painel *Falta* × *Já está certo* gerado ao vivo a partir dos dados, progresso por área e divisão de tarefas por pessoa.
- **Tarefas**: responsável (💗 Baixinha / 💙 Querido / 💞 Nós dois), status (A fazer → Em andamento → Pronto), áreas personalizáveis, prazo, prioridade, filtros e visão por área ou por status. Tem um plano sugerido que você gera com um toque.
- **Agenda** com lembretes que chegam mesmo com o app fechado.
- **Lugares**: opções com preço, nota, mapa, link e telefone, curtidas dos dois e status *escolhido/descartado*.
- **Ideias** (curtir e aprovar), **Bloco de notas** (cores e fixar), **Convidados** (status de convite, acompanhantes, WhatsApp), **Orçamento** (pago/a pagar e limite).
- **Notificações**: lembretes da agenda, avisos quando a outra pessoa adiciona algo e contagem regressiva diária.
- Sincroniza em tempo real e funciona offline (o Firestore guarda cache local).

## Configuração do Firebase (uma vez só)
1. **Authentication → Método de login → E-mail/senha → Ativar.** Não precisa de verificação de e-mail.
2. **Authentication → Usuários → Adicionar usuário** e crie:
   - `baixinha@gegeparty.app`
   - `querido@gegeparty.app`

   No app, cada pessoa só toca no próprio nome e digita a senha.
3. **Authentication → Configurações → Ações do usuário**: desmarque *Ativar criação (inscrição)* para ninguém mais criar conta.
4. **Firestore Database → Criar banco** e cole o conteúdo de [`firestore.rules`](firestore.rules) na aba **Regras**.

## APK
Cada push na `main` gera o APK em **Releases** (GitHub Actions → *Build APK*). Baixe o `GegeParty.apk` no celular e instale. Se pedir, permita instalar de fontes desconhecidas.
