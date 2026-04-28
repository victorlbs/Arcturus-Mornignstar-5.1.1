# 🌌 Arcturus Morningstar 5.1.1 - Extended Edition

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://www.oracle.com/java/)
[![Netty](https://img.shields.io/badge/Netty-4.1.x-10b981?style=for-the-badge&logo=opsgenie&logoColor=white)](https://netty.io/)
[![Version](https://img.shields.io/badge/Version-5.1.1-blue?style=for-the-badge)](https://github.com/victorlbs/Arcturus-Mornignstar-5.1.1)

O **Arcturus Morningstar 5.1.1 Extended** é uma evolução do emulador original, projetada para oferecer uma experiência superior em estabilidade e recursos. Esta versão foca na modernização da engine Wired e na automação total de eventos, facilitando a gestão de hotéis de médio e grande porte.

---

## 🛠️ Modificações e Destaques

Diferente das versões convencionais, este projeto traz melhorias profundas em diversos módulos:

### ⚙️ Engenharia Wired (Recursos Exclusivos)
* **Sincronização Binária de UI:** Implementação total de pacotes para suporte a Sliders de tempo (Minutos/Segundos) nativos no Nitro.
* **Comunicação Remota:** Novos Wireds de **Mandar Sinal** e **Recebe Sinal**, permitindo automação sem fios físicos no quarto.
* **Controle de Jogos:** Efeito de **Ajustar Tempo do Contador** com suporte a modos de aumentar, diminuir ou definir valores fixos em cronômetros.

### 🤖 Automação & Eventos
* **Eventos Automáticos:** Sistema nativo para execução de atividades sem necessidade de Staff online.
* **Gestão de Bots:** Melhoria na interação de NPCs com mobis de jogo e comandos Wired.
* **Sistema de Recompensas:** Entrega automática de moedas e emblemas integrada ao core.

### ⚡ Performance & Segurança
* **Otimização do Room Cycle:** Refatoração do processamento de quartos para suportar maior densidade de mobis e usuários com menor latência.
* **Persistência JSON:** Salvamento de dados Wired via GSON para maior flexibilidade e compatibilidade futura.
* **ByteBuf Security:** Tratamento rigoroso na classe `ServerMessage` para prevenir ataques de overflow e pacotes malformados.

---

## 🚀 Como Começar

### Pré-requisitos
* **JDK 17 ou superior**
* **MariaDB 10.6+** ou **MySQL 8.0+**
* **IntelliJ IDEA** (Recomendado) ou Maven CLI

### Instalação Rápida
1. **Clone o repositório:**
   ```bash
   git clone [https://github.com/victorlbs/Arcturus-Mornignstar-5.1.1.git](https://github.com/victorlbs/Arcturus-Mornignstar-5.1.1.git)
