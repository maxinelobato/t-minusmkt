# T-Minus Mkt — Integração da Logomarca Oficial e Ícones no Android

Plano para incorporar a identidade visual oficial (logomarca em alta resolução e ícone do aplicativo com cronômetro e vetor de lançamento) na interface do aplicativo e no sistema operacional Android, alternando de forma reativa entre os temas claro e escuro.

### User Review & Critical Decisions

> [!IMPORTANT]
> Decisões confirmadas com base nas respostas do usuário:
> - **Localização da Logomarca**: Integrada com destaque no topo da **Tela de Bloqueio** e no **Cabeçalho Principal** da tela de contagem regressiva.
> - **Ícone Principal do Android**: Versão escura oficial com cronômetro em neon ciano e magenta (`Dark Icon`) com a inscrição `T-Mkt`, aplicada em todas as resoluções do sistema (`mipmap-mdpi` até `mipmap-xxxhdpi`).
> - **Comportamento Adaptativo**: A logomarca se adapta dinamicamente:
>   - No **Modo Claro**: Exibe a logomarca clara com relevo neumórfico suave, tipografia `T-Minus` em preto/cinza e `Mkt` em ciano suave.
>   - No **Modo Escuro**: Exibe a logomarca escura com cronômetro em contorno neon ciano/magenta e texto com brilho suave.

---

### 1. Overview & Core Concept

- **O que faz**: O aplicativo exibe a logomarca oficial do **T-Minus Mkt** nos pontos centrais de contato com o usuário. Na tela de bloqueio (lock screen), a logomarca posiciona-se no topo acima do relógio ou no cartão de autenticação, reforçando a assinatura da marca. Na tela principal, ela coroa o cabeçalho de contagem regressiva. No lançador do Android (home screen), o ícone exibe o símbolo de contagem regressiva em alta fidelidade.
- **Público-alvo**: Gestores e profissionais de marketing que desejam uma experiência de aplicativo coesa, profissional e com forte identidade de marca.
- **Valor central**: Elevar a percepção de produto premium através do alinhamento visual rigoroso com as peças gráficas oficiais do projeto.

---

### 2. User Experience & Visual Design

#### Posicionamento da Logomarca na Interface

1. **Tela de Bloqueio & Ambiente (`NeumorphicLoginScreen`)**:
   - No topo da tela, uma pílula neumórfica estilizada renderiza a logomarca oficial em alta definição.
   - No tema claro, exibe a pílula branca com sombra suave (`logo_light.png`).
   - No tema escuro, exibe a pílula grafite com cronômetro em neon ciano/magenta (`logo_dark.png`).
   - Acompanha o relógio digital e o cartão de notificação ativa com harmonia espacial.

2. **Tela Principal de Contagem Regressiva (`TMinusMainScreen`)**:
   - Integrada ao cartão de cabeçalho da contagem regressiva (`COUNTDOWN: MARKETING PLANNING`), exibindo a marca gráfica oficial como selo do projeto.
   - Toque na logomarca fornece feedback tátil suave.

3. **Ícone do Android (`Launcher Icon`)**:
   - Ícone adaptativo quadrado arredondado (squircle) e redondo (`ic_launcher_round`), garantindo compatibilidade com todas as fabricantes e versões do Android.
   - Fundo escuro aveludado (`#171B22`) com cronômetro em vetor luminoso neon ciano e magenta, e texto `T-Mkt`.

---

### 3. Key Product Decisions & Trade-Offs

- **Decisão 1: Vetorização e Assets Rasterizados em Alta Resolução**:
  - *Abordagem*: Recorte de precisão milimétrica dos assets de alta resolução extraídos da arte fornecida (`2400x1792`) diretamente para `res/drawable/` e geração de bitmaps sem distorção para todas as densidades do launcher (`mdpi: 48dp`, `hdpi: 72dp`, `xhdpi: 96dp`, `xxhdpi: 144dp`, `xxxhdpi: 192dp`).
  - *Por que*: Garante nitidez absoluta sem artefatos de renderização ou bordas serrilhadas.
- **Decisão 2: Componente Reutilizável `TMinusBrandLogo`**:
  - *Abordagem*: Criar um componente Compose dedicado (`TMinusBrandLogo.kt`) que observa o estado do tema (`LocalNeumorphicColors.current.isDark`) e seleciona automaticamente a versão correta do asset com dimensões e proporções fiéis (altura de 38dp a 46dp).
  - *Por que*: Centraliza a lógica de identidade visual e garante que qualquer alteração de tema ocorra instantaneamente sem piscar a tela.

---

### 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                 TMinusBrandLogo Component                   │
│   (Reativo ao tema: isDark -> logo_dark vs logo_light)      │
└──────────────────────────────┬──────────────────────────────┘
                               │
                ┌──────────────┴──────────────┐
                ▼                             ▼
┌──────────────────────────────┐ ┌──────────────────────────────┐
│    NeumorphicLoginScreen     │ │      TMinusMainScreen        │
│ - Brand Header no Topo       │ │ - Logomarca no Cabeçalho     │
│ - Relógio e Notificação      │ │ - Cartões de Contagem        │
│ - Biometria Holográfica      │ │ - Pílulas de Ação Rápida     │
└──────────────────────────────┘ └──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│             Android System Integration                      │
│ - res/drawable/app_icon_dark.png (High-Res Asset)           │
│ - res/drawable/ic_launcher_foreground.xml (Adaptive FG)     │
│ - res/mipmap-{density}/ic_launcher.png (Raster Icons)       │
│ - res/mipmap-{density}/ic_launcher_round.png (Circular)     │
└─────────────────────────────────────────────────────────────┘
```
