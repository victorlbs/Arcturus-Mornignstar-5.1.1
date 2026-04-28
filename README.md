# 🌌 Arcturus Morningstar 5.1.1 - Extended Edition

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://www.oracle.com/java/)
[![Version](https://img.shields.io/badge/Version-5.1.1-blue?style=for-the-badge)](https://github.com/victorlbs/Arcturus-Mornignstar-5.1.1)
[![Status](https://img.shields.io/badge/Status-Otimizado-green?style=for-the-badge)]()

Esta é uma versão estendida e otimizada do **Arcturus Morningstar 5.1.1**, desenvolvida para administradores que procuram um servidor robusto, autónomo e com recursos avançados de Wired.

---

## 🚀 Principais Modificações

O foco deste projeto está na autonomia do hotel e na liberdade de criação para os utilizadores através de sistemas avançados:

### 🤖 Eventos Automáticos (Engine Nativa)
* **Autonomia Total:** Sistema de eventos que roda 24/7 sem necessidade de supervisão da Staff.
* **Variedade:** Suporte nativo para jogos automáticos clássicos e customizados.
* **Recompensas:** Entrega automática de moedas, pontos e emblemas configurável via banco de dados.

### ⚙️ Engine Wired Renovada
* **Novos Wireds:** Inclusão de efeitos e gatilhos exclusivos para maior interatividade.
* **Sincronização de UI:** Suporte a Sliders de tempo (Minutos/Segundos) sincronizados diretamente com a interface do cliente (Nitro/Flash).
* **Persistência JSON:** Implementação de salvamento via GSON, garantindo que configurações complexas não se percam.

### ⚡ Código de Alta Performance
* **Refatoração do Core:** Melhorias críticas nas classes de processamento de quartos (`Room.java`) para reduzir o consumo de CPU.
* **Estabilidade de Rede:** Otimização na classe `ServerMessage` para um tráfego de pacotes mais fluido e seguro.
* **Limpeza de Memória:** Correção de vazamentos de memória (Memory Leaks) presentes em versões anteriores.

---

## 🛠️ Instalação e Build

1. **Requisitos:** Java 17+ e MySQL/MariaDB.
2. **Setup:** Importe a SQL da pasta `/sql` e configure o `config.ini`.
3. **Build:**
   ```bash
   mvn clean package
