# 📱 Clone do Organizze

Este projeto é um **clone do app Organizze**, desenvolvido em **Java com Android Studio**, com o objetivo de praticar e consolidar conceitos de desenvolvimento mobile nativo para Android, utilizando **Firebase** como backend para autenticação e persistência de dados.

---

## 🚀 Tecnologias Utilizadas

- **Java** → Linguagem principal do projeto.  
- **Android Studio** → IDE de desenvolvimento.  
- **Gradle** → Gerenciador de dependências e build.  
- **Firebase**  
  - Autenticação de usuários  
  - Realtime Database / Firestore (dependendo da implementação)  
  - Google Services (integração com o app)  

---

## ⚙️ Funcionalidades

- Cadastro e login de usuários (Firebase Authentication)  
- Controle de receitas e despesas  
- Saldo atualizado em tempo real  
- Armazenamento de dados financeiros no Firebase  
- Interface inspirada no aplicativo **Organizze**  

---

## 📂 Estrutura do Projeto

```
Organizze/
│── app/ # Código-fonte principal
│ ├── java/ # Classes Java (Activities, Controllers, etc.)
│ ├── res/ # Layouts XML, drawables e valores
│ └── google-services.json
│
├── build.gradle # Configuração do Gradle (projeto)
├── settings.gradle # Configuração de módulos
└── README.md # Este arquivo
```

---

## 📲 Como Rodar o Projeto

1. Clone o repositório:
```bash
    git clone https://github.com/seu-usuario/organizze-clone.git
```

2. Abra o projeto no **Android Studio**.

3. Configure o **Firebase** no projeto:
   - Acesse o Firebase Console (https://console.firebase.google.com/).
   - Crie um novo projeto (ou use um existente).
   - Registre o app Android usando o **Application ID / package name** exato do seu projeto (o mesmo que está em `app/build.gradle` -> `namespace` ou `applicationId`).
   - Baixe o arquivo `google-services.json` para o app correto.

4. Coloque o `google-services.json` dentro da pasta `app/` do projeto (substitua o existente, se houver).

5. Sincronize o Gradle: no Android Studio clique em **File > Sync Project with Gradle Files**.

6. Rode o app em um emulador ou dispositivo físico.

---

## ⚙️ Dependências Utilizadas

- Firebase (Auth e Realtime Database / Firestore via Firebase BoM) — autenticação e persistência.  
- AndroidX AppCompat, ConstraintLayout, Activity KTX — base do app Android.  
- Material Components — componentes de UI seguindo Material Design.  
- `com.heinrichreimersoftware.materialintro` — usado para o onboarding/slider. Import usado no código: `import com.heinrichreimersoftware.materialintro.app.IntroActivity;` (para estender `IntroActivity` e montar os slides).  
- Outras bibliotecas utilitárias conforme `build.gradle` do projeto.

---

## 📝 Funcionalidades

- Cadastro e login de usuários com Firebase Authentication.  
- Registro, edição e remoção de transações (receitas e despesas).  
- Cálculo e exibição do saldo atual.  
- Histórico/listagem de transações.  
- Tela(s) de onboarding com slider (implementadas usando `IntroActivity` / MaterialIntro).  
- Interface inspirada no aplicativo Organizze para fins de estudo/portfólio.

---

## 📸 Screenshots

Vou adicionar depois imagens do app em funcionamento na pasta `/docs` ou no próprio README usando Markdown:

    ![Tela principal](docs/screenshot_main.png)

_(Substituir o path pelas suas screenshots)_

---

## 📌 Observações importantes

- **Application ID / package name precisa bater** com o registro no Firebase. Se receber erro `No matching client found for package name '...'`, baixe o `google-services.json` correto ou ajuste o Application ID.  
- Para o slider de onboarding, uso a dependência MaterialIntro e estendo `IntroActivity` no Activity de introdução. Exemplo de import:  
  `import com.heinrichreimersoftware.materialintro.app.IntroActivity;`  
- Se utilizar slides customizados com fragments, utilize `FragmentSlide.Builder().fragment(R.layout.seu_fragment)`; para slides simples `SimpleSlide.Builder()` costuma ser suficiente.

---

## 🤝 Como contribuir

1. Fork o repositório.  
2. Crie uma branch com a sua feature: `feature/nome-da-feature`.  
3. Faça commits pequenos e descritivos.  
4. Abra um Pull Request descrevendo as mudanças.

---

## 📝 Licença

Projeto para fins educacionais e de portfólio. Não é afiliado ao aplicativo oficial Organizze. Use livremente para aprendizado.

---

Sincronize o Gradle e rode o app em um emulador ou dispositivo físico.
