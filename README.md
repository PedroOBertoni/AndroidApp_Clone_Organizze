# 💸 Finansee

O **FinanSee** é um aplicativo mobile de **gestão financeira pessoal**, desenvolvido de forma **100% original em Java com Android Studio**, com o objetivo de oferecer uma experiência moderna, intuitiva e prática para o controle de receitas, despesas e saldo mensal.

O projeto foi criado com foco em **performance, organização de código e boas práticas de desenvolvimento Android**, fazendo uso de **diversas bibliotecas** para aprimorar a experiência do usuário e a arquitetura do app.

---

## 🚀 Sobre o projeto

O **FinanSee** nasceu como um estudo de caso em desenvolvimento mobile, mas evoluiu para um **aplicativo autoral completo**, com design, funcionalidades e código totalmente reformulados.  
Hoje, o app oferece recursos avançados de controle financeiro e uma interface refinada, adaptada para **modo escuro**, **animações fluidas** e **feedbacks visuais interativos**.

---

## ✨ Principais Funcionalidades

- 📊 **Gerenciamento de transações** — registre receitas, despesas e veja o saldo atualizado em tempo real.  
- 🧾 **Histórico detalhado** — acompanhe todas as movimentações financeiras com filtros por data, tipo e categoria.  
- 👤 **Autenticação de usuários** — login e cadastro com Firebase Authentication.  
- ☁️ **Sincronização em nuvem** — dados armazenados e recuperados com segurança usando o Firebase.  
- 🕹️ **Interface intuitiva e moderna** — design limpo, responsivo e otimizado para o tema escuro.  
- 🌀 **Tela de splash animada** — experiência visual aprimorada no início do app.  
- 🧭 **Sistema de navegação fluido** — navegação entre telas otimizada com animações suaves.  
- 🔒 **Validação e segurança de dados** — estrutura voltada à confiabilidade e consistência das informações.

---

## 🧠 Conceitos aplicados

Durante o desenvolvimento do **FinanSee**, foram aplicados conceitos importantes de:
- Estrutura e ciclo de vida de Activities e Fragments.  
- Integração com serviços externos via Firebase.  
- Persistência de dados e manipulação de estados.  
- Boas práticas de UX/UI e Material Design.  
- Organização de código seguindo padrões de arquitetura Android.  
- Manipulação de animações e elementos visuais personalizados.

---

## ⚙️ Estrutura do Projeto

```
FinanSee/
│── app/
│ ├── java/
│ │ ├── com.aula.finansee/
│ │ │ ├── activity/ # Activities principais (Login, Cadastro, Principal, etc.)
│ │ │ ├── model/ # Classes de modelo (Transação, Usuário, etc.)
│ │ │ ├── helper/ # Utilitários e classes auxiliares
│ │ │ └── config/ # Configurações do Firebase e constantes
│ │
│ ├── res/
│ │ ├── layout/ # Layouts XML
│ │ ├── drawable/ # Ícones e backgrounds
│ │ └── values/ # Cores, strings e estilos
│ │
│ └── google-services.json # Configuração do Firebase
│
├── build.gradle # Configuração do Gradle (app)
├── settings.gradle # Configuração do projeto
└── README.md # Documentação do projeto
```

---

## 📲 Como executar o projeto

### 1. Clone o repositório:
   ```bash
   git clone https://github.com/seu-usuario/finansee.git
   ```
### 2. Abra o projeto no Android Studio.

### 3. Configure o Firebase:

  - Acesse o Firebase Console.
  - Crie um novo projeto (ou use um existente).
  - Registre o app Android com o mesmo Application ID definido em build.gradle.
  - Baixe o arquivo google-services.json e coloque na pasta app/.
  - Sincronize o Gradle (File > Sync Project with Gradle Files).

### 4. Execute o app em um emulador ou dispositivo físico.

---

## 🎨 Design e Identidade Visual

O FinanSee adota uma identidade visual moderna e minimalista, com:
- Fundo escuro (tons de preto e cinza) para conforto visual e economia de energia.  
- Destaques em azul ciano (#2196F3), reforçando o foco financeiro e tecnológico.  
- Ícones e componentes seguindo o padrão Material Design 3 (MD3).
- Animações suaves e coerentes com o estilo da aplicação.

---

## 🧭 Estrutura de Navegação
O fluxo principal do FinanSee é estruturado para garantir uma navegação fluida e intuitiva:

  ```ngix
  Splash Screen → Login / Cadastro → Dashboard → Nova Transação → Histórico → Perfil / Configurações
  ```

Cada tela foi projetada com foco em clareza, simplicidade e feedback visual rápido.

---

## 📦 Dependências e bibliotecas

O projeto utiliza diversas bibliotecas modernas para aprimorar a experiência e simplificar a implementação de recursos como animações, autenticação, persistência e interface.
(Esta seção será detalhada posteriormente com a lista completa de dependências do build.gradle.)

---

## 📸 Screenshots (em breve)

Imagens das principais telas do aplicativo serão adicionadas aqui:

```
docs/
 ├── screenshot_login.png
 ├── screenshot_dashboard.png
 └── screenshot_historico.png
```

## 🧩 Próximos Passos

- 📈 Implementar gráficos de análise financeira.
- 💾 Adicionar exportação de dados (CSV / PDF). 
- 🔔 Implementar lembretes de pagamento.
- 🌎 Sincronização multi-dispositivo e backup automático.

---

## 🤝 Contribuição

Sinta-se à vontade para contribuir com o projeto:

1.Faça um fork do repositório.
2. Crie uma branch para sua feature:
  ```bash
  git checkout -b feature/nova-funcionalidade
  ```
3. Faça commits descritivos e objetivos.
4. Push para a branch (`git push origin feature/nova-feature`)
5. Envie um Pull Request com suas alterações.

---

## 🪪 Licença

Este projeto é de autoria original e foi desenvolvido para fins de estudo e portfólio.
Você pode utilizá-lo livremente para aprendizado, sem fins comerciais diretos.
