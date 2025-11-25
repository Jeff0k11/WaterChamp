# Documentação de Integração Supabase - WaterChamp

## Índice
1. [Arquitetura do Projeto](#arquitetura-do-projeto)
2. [Configuração e Inicialização](#configuração-e-inicialização)
3. [Schema das Tabelas](#schema-das-tabelas)
4. [Modelos de Dados](#modelos-de-dados)
5. [Fluxo de Dados](#fluxo-de-dados)
6. [Padrões de Integração](#padrões-de-integração)
7. [Operações Comuns](#operações-comuns)

---

## Arquitetura do Projeto

### Nome da Arquitetura
**MVC/MVP com Repository Pattern e Offline-First**

O WaterChamp utiliza uma arquitetura em camadas bem definida que combina padrões Android modernos para garantir separação de responsabilidades, testabilidade e manutenibilidade.

### Estrutura em Camadas

```
┌─────────────────────────────────────────────────────────┐
│         PRESENTATION LAYER (UI)                         │
│  ├── Activities/Fragments                               │
│  ├── Adapters                                           │
│  └── ViewModels (se aplicável)                          │
│  Usa: Java POJOs (User, Group, HistoryRecord)           │
└────────────────────┬────────────────────────────────────┘
                     │ Comunica com
                     ▼
┌─────────────────────────────────────────────────────────┐
│         PRESENTATION LOGIC LAYER                        │
│  ├── UserRepository                                     │
│  ├── ConsumoRepository                                  │
│  ├── RankingRepository                                  │
│  ├── GrupoRepository                                    │
│  └── Implementa callbacks async para Java               │
│  Bridge entre UI (Java) e Services (Kotlin)             │
└────────────────────┬────────────────────────────────────┘
                     │ Delega para
                     ▼
┌─────────────────────────────────────────────────────────┐
│         BUSINESS LOGIC LAYER (Services)                 │
│  ├── UserService (Auth + CRUD)                          │
│  ├── ConsumoService (Sincronização)                     │
│  ├── GrupoService (Gerenciamento de grupos)             │
│  ├── RankingService (Queries com JOINs)                 │
│  └── Usa: Kotlin Coroutines                             │
│  Chamadas diretas Postgrest API                         │
└────────────────────┬────────────────────────────────────┘
                     │ Utiliza
                     ▼
┌─────────────────────────────────────────────────────────┐
│         DATA ACCESS LAYER (Local Cache)                 │
│  ├── PreferencesManager (SharedPreferences)             │
│  ├── HistoryCache (Cache customizado)                   │
│  └── Sincronização offline-first                        │
└────────────────────┬────────────────────────────────────┘
                     │ Comunica com
                     ▼
┌─────────────────────────────────────────────────────────┐
│         REMOTE DATA LAYER                               │
│  └── Supabase PostgreSQL (via Postgrest API)            │
│  ├── Tables: usuarios, consumo_diario, grupos,          │
│  │           membros_grupo                              │
│  └── Auth via Supabase Auth                             │
└─────────────────────────────────────────────────────────┘
```

### Estrutura de Diretórios

```
app/src/main/java/com/example/waterchamp/
├── data/
│   ├── remote/                    # BUSINESS LOGIC LAYER
│   │   ├── SupabaseClient.kt      # Singleton do cliente
│   │   ├── UserService.kt         # Serviço de autenticação
│   │   ├── ConsumoService.kt      # Serviço de consumo
│   │   ├── GrupoService.kt        # Serviço de grupos
│   │   └── RankingService.kt      # Serviço de ranking
│   ├── repository/                # PRESENTATION LOGIC LAYER
│   │   ├── UserRepository.java
│   │   ├── ConsumoRepository.java
│   │   ├── RankingRepository.java
│   │   └── GrupoRepository.java
│   ├── local/                     # DATA ACCESS LAYER
│   │   ├── PreferencesManager.java
│   │   └── HistoryCache.java
│   └── model/                     # Entidades de dados
│       ├── User.java
│       ├── Group.java
│       └── HistoryRecord.java
├── ui/                            # PRESENTATION LAYER
│   ├── activities/
│   ├── fragments/
│   └── adapters/
└── ...
```

### Características da Arquitetura

#### 1. **Separação de Responsabilidades**
- **UI Layer**: Apenas apresentação (Activities/Fragments)
- **Repository Layer**: Lógica de apresentação, callbacks async
- **Service Layer**: Lógica de negócio, coroutines
- **Data Layer**: Acesso a dados locais e remotos

#### 2. **Padrões Implementados**
| Padrão | Aplicação | Benefício |
|--------|-----------|-----------|
| **Singleton** | SupabaseClient | Única instância do cliente reutilizada |
| **Repository** | Repositories | Abstração entre UI e dados |
| **Observer/Callback** | Repository callbacks | Operações async em Java |
| **Offline-First** | Cache local + sync | Funciona sem internet |
| **UPSERT** | syncDailyConsumption | Sincronização sem duplicatas |

#### 3. **Fluxo de Dados**
```
UI Action
  ↓
Repository (Java callback)
  ↓
Service (Kotlin coroutine)
  ↓
SupabaseClient
  ↓
Local Cache / Remote DB
  ↓
Result → Callback → Repository → UI Update
```

#### 4. **Testabilidade**
- Services podem ser testados isoladamente (coroutines)
- Repositories podem ser mockados
- UI separada da lógica de negócio

#### 5. **Offline-First Strategy**
```
User Action
  ├─ Cache Local (imediato)
  │  └─ UI atualizada instantaneamente
  └─ Sync Background (não-bloqueante)
     └─ Remote DB atualizado
```

### Comparação com Outras Arquiteturas

| Aspecto | WaterChamp | Clean Arch | MVVM |
|---------|-----------|-----------|------|
| **Camadas** | 5 | 3+ | 3 |
| **Padrão Principal** | MVC + Repository | Clean | ViewModel |
| **State Management** | Repositories | UseCases | ViewModel |
| **Testabilidade** | Alto | Muito Alto | Alto |
| **Complexidade** | Média | Alta | Média |

---

## Configuração e Inicialização

### Localização do Cliente Supabase
**Arquivo**: `app/src/main/java/com/example/waterchamp/data/remote/SupabaseClient.kt`

### Padrão Singleton
O Supabase é inicializado como um singleton thread-safe usando object declaration do Kotlin:

```kotlin
object SupabaseClient {
    private var supabaseUrl: String = ""
    private var supabaseKey: String = ""
    private var _client: io.github.jan.supabase.SupabaseClient? = null

    fun initialize(context: Context) {
        loadCredentials(context)
        _client = createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            install(Postgrest)  // Para operações de banco de dados
            install(Auth)       // Para autenticação
        }
    }

    val client: io.github.jan.supabase.SupabaseClient
        get() = _client ?: throw IllegalStateException()
}
```

### Credenciais
- **Arquivo de configuração**: `local.properties` (não versionado)
- **Template**: `local.properties.example`
- **Variáveis necessárias**:
  - `SUPABASE_URL`: URL base do projeto
  - `SUPABASE_KEY`: Chave de API anônima
- **Integração com build**: `app/build.gradle.kts` lê credenciais e expõe via `BuildConfig`
- **URL atual do projeto**: `https://kajdflcgqthnuhjvfnwy.supabase.co`

### Inicialização na Aplicação
1. App inicia → `WaterChampApplication.onCreate()`
2. Chamada `SupabaseClient.initialize(context)`
3. Credenciais carregadas de `BuildConfig.SUPABASE_URL` e `BuildConfig.SUPABASE_KEY`
4. Clientes Postgrest e Auth criados
5. Pronto para uso em toda a aplicação

---

## Schema das Tabelas

### Visão Geral
A aplicação utiliza **4 tabelas principais** no Supabase PostgreSQL:

```
┌─────────────────────────────────────────────┐
│            usuarios (Usuários)              │
│─────────────────────────────────────────────│
│ id (PK)      │ nome    │ email (UNIQUE)    │
│ senha_hash   │ data_criacao                │
└────────┬──────────────────────────────────┬─┘
         │                                  │
         │ 1:N                         1:N  │
         └──────────────┬──────────────┘    │
                        │                   │
        ┌───────────────┴───────────────┐   │
        │                               │   │
    ┌───▼─────────────────────────┐    │   │
    │  consumo_diario             │    │   │
    │  (Consumo Diário)           │    │   │
    │───────────────────────────   │    │   │
    │ id (PK)     │ usuario_id (FK)   │    │
    │ data        │ total_ml           │    │
    └─────────────────────────────┘    │   │
                                       │   │
                                   ┌───▼───▼────────────────┐
                                   │  grupos (Grupos)       │
                                   │─────────────────────────│
                                   │ id (PK) │ nome          │
                                   │ descricao │ criador_id  │
                                   │ data_criacao            │
                                   └───┬──────────────────────┘
                                       │
                                       │ N:N
                                       │
                                   ┌───▼──────────────────┐
                                   │ membros_grupo        │
                                   │ (Junction Table)     │
                                   │──────────────────────│
                                   │ grupo_id (FK)        │
                                   │ usuario_id (FK)      │
                                   └──────────────────────┘
```

### 1. Tabela `usuarios`

**Propósito**: Armazenar informações de conta e dados de autenticação dos usuários

| Coluna | Tipo | Restrições | Descrição |
|--------|------|-----------|-----------|
| `id` | INTEGER | PRIMARY KEY, AUTO-INCREMENT | Identificador único do usuário |
| `nome` | TEXT | NOT NULL | Nome completo do usuário |
| `email` | TEXT | NOT NULL, UNIQUE | Email do usuário (em minúsculas) |
| `senha_hash` | TEXT | NULLABLE | Hash da senha (gerenciado pelo Auth) |
| `data_criacao` | TIMESTAMP | DEFAULT NOW() | Data de criação da conta |

**Relacionamentos**:
- 1:N com `consumo_diario` (um usuário tem vários registros de consumo)
- 1:N com `membros_grupo` (um usuário pode participar de vários grupos)
- 1:N com `grupos` (um usuário pode criar vários grupos)

**Operações principais**:
- Registrar: `UserService.registerUser()` → cria usuário em Auth + insere na tabela
- Login: `UserService.login()` → valida com Auth + busca dados da tabela
- Buscar por email: `getUserByEmail(email)`
- Buscar por ID: `getUserById(id)`

---

### 2. Tabela `consumo_diario`

**Propósito**: Rastrear o consumo diário de água por usuário

| Coluna | Tipo | Restrições | Descrição |
|--------|------|-----------|-----------|
| `id` | INTEGER | PRIMARY KEY, AUTO-INCREMENT | Identificador do registro |
| `usuario_id` | INTEGER | NOT NULL, FK(usuarios.id) | Referência ao usuário |
| `data` | DATE | NOT NULL | Data do consumo (formato: yyyy-MM-dd) |
| `total_ml` | INTEGER | NOT NULL | Total de ml consumido naquele dia |

**Restrição Única**: `UNIQUE(usuario_id, data)` - um registro por usuário por dia

**Características principais**:
- Padrão **UPSERT**: `ConsumoService.syncDailyConsumption()` usa `upsert()` para inserir ou atualizar
- Permite múltiplos registros por usuário em datas diferentes
- Suporta rastreamento histórico e cálculo de sequências (streaks)

**Operações principais**:
- Sincronizar total diário: `syncDailyConsumption(usuarioId, data, totalMl)`
- Obter consumo de data específica: `getConsumptionByDate(usuarioId, data)`
- Obter histórico: `getConsumptionHistory(usuarioId, days=30)`
- Calcular sequência: `calculateStreak(usuarioId, metaDiaria)`
- Obter total: `getTotalConsumption(usuarioId, days=30)`

**Sincronização Local**:
- Histórico detalhado armazenado em cache via `HistoryCache` com registros individuais
- Total diário sincronizado com Supabase (background)
- Timestamp da última sincronização rastreado em `PreferencesManager`

---

### 3. Tabela `grupos`

**Propósito**: Armazenar informações de grupos/equipes para desafios coletivos

| Coluna | Tipo | Restrições | Descrição |
|--------|------|-----------|-----------|
| `id` | INTEGER | PRIMARY KEY, AUTO-INCREMENT | Identificador único do grupo |
| `nome` | TEXT | NOT NULL | Nome do grupo |
| `descricao` | TEXT | NULLABLE | Descrição do grupo |
| `criador_id` | INTEGER | NOT NULL, FK(usuarios.id) | ID do usuário criador |
| `data_criacao` | TIMESTAMP | DEFAULT NOW() | Data de criação do grupo |
| `total_membros` | INTEGER | DEFAULT 0 | Contagem de membros (opcional) |

**Relacionamentos**:
- Um criador por grupo (FK para usuarios)
- Vários membros por grupo (via tabela de junção membros_grupo)

**Operações principais**:
- Criar grupo: `GrupoService.createGroup()` → adiciona criador como membro automaticamente
- Deletar grupo: `deleteGroup()` → deleta em cascata membros_grupo
- Obter grupo por ID: `getGroupById(grupoId)`
- Contar membros: `countGroupMembers(grupoId)`

---

### 4. Tabela `membros_grupo`

**Propósito**: Rastrear quais usuários pertencem a quais grupos (relacionamento muitos-para-muitos)

| Coluna | Tipo | Restrições | Descrição |
|--------|------|-----------|-----------|
| `grupo_id` | INTEGER | NOT NULL, FK(grupos.id) | Referência ao grupo |
| `usuario_id` | INTEGER | NOT NULL, FK(usuarios.id) | Referência ao usuário |

**Restrição Única**: `UNIQUE(grupo_id, usuario_id)` - usuário não pode entrar no mesmo grupo duas vezes

**Características principais**:
- Tabela de junção pura (sem dados adicionais)
- Suporta rankings por grupo
- Força a regra "um grupo por usuário" em nível de aplicação

**Operações principais**:
- Adicionar membro: `addMemberToGroup(grupoId, usuarioId)`
- Remover membro: `removeMemberFromGroup(grupoId, usuarioId)`
- Obter membros do grupo: `getGroupMembers(grupoId)` → retorna lista de IDs
- Obter grupos do usuário: `getUserGroups(usuarioId)` → busca todos os grupos do usuário

---

## Modelos de Dados

### Classes de Dados Kotlin (Service Layer)

Localizadas em `/data/remote/` e usadas para serialização/desserialização com Supabase.

#### **Usuario**
```kotlin
@Serializable
data class Usuario(
    val id: Int? = null,
    val nome: String,
    val email: String,
    val senha_hash: String? = null,
    val data_criacao: String? = null
)
```

#### **ConsumoDiario**
```kotlin
@Serializable
data class ConsumoDiario(
    val id: Int? = null,
    val usuario_id: Int,
    val data: String,          // formato: yyyy-MM-dd
    val total_ml: Int
)
```

#### **GrupoData**
```kotlin
@Serializable
data class GrupoData(
    val id: Int? = null,
    val nome: String,
    val descricao: String,
    val criador_id: Int,
    val data_criacao: String? = null,
    val total_membros: Int? = 0
)

@Serializable
private data class MembrosGrupoData(
    val grupo_id: Int,
    val usuario_id: Int
)
```

#### **RankingEntry e ConsumoComUsuario**
```kotlin
@Serializable
data class RankingEntry(
    val id: Int,
    val nome: String,
    val consumo_hoje: Int? = null,          // Ranking diário
    val total_30_dias: Long? = null,        // Ranking global
    val posicao: Long                       // Posição/rank
)

@Serializable
data class ConsumoComUsuario(
    val usuario_id: Int,
    val total_ml: Int,
    val usuarios: UsuarioSimples? = null    // Relacionamento JOIN aninhado
)

@Serializable
data class UsuarioSimples(
    val nome: String
)
```

### Classes Java (Application Layer)

Localizadas em `/model/` e usadas na camada de UI.

#### **User.java**
```java
public class User implements Comparable<User> {
    private String name;
    private int waterIntake;                    // Ingestão atual diária
    private String email;
    private int rank;
    private List<HistoryRecord> historyList;

    // Campos de perfil
    private int dailyGoal = 2000;              // Meta padrão: 2000ml
    private int defaultCupSize = 250;          // Tamanho padrão do copo: 250ml
    private long totalConsumedAllTime = 0;
    private int streak = 0;
    private long creationDate;
    private String profilePictureUri;
    private boolean notificationsEnabled = true;
}
```

#### **HistoryRecord.java**
```java
public class HistoryRecord {
    private long timestamp;
    private int amount;                        // Quantidade em ml
    private String action;                     // "Adicionado" ou "Removido"
}
```

#### **Group.java**
```java
public class Group {
    private int id;
    private String nome;
    private String descricao;
    private int criadorId;
    private String dataCriacao;
    private int totalMembros;
}
```

---

## Fluxo de Dados

### Arquitetura em Camadas

```
┌─────────────────────────────────────┐
│     UI Layer (Activities/Fragments)  │
│  ↓ Usa Java POJOs (User, Group)     │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│  Repository Layer (/data/repository)│
│  • UserRepository                   │
│  • ConsumoRepository                │
│  • RankingRepository                │
│  • GrupoRepository                  │
│  ↓ Implementa callbacks async       │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│  Service Layer (/data/remote)       │
│  • UserService (Auth + CRUD)        │
│  • ConsumoService                   │
│  • GrupoService                     │
│  • RankingService (JOINs)           │
│  ↓ Chamadas diretas Postgrest API   │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│  Local Cache Layer (/data/local)    │
│  • PreferencesManager               │
│  • HistoryCache                     │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│  Supabase PostgreSQL                │
│  (Banco de dados remoto)            │
└─────────────────────────────────────┘
```

### Fluxo de Sincronização de Consumo

O aplicativo utiliza uma estratégia híbrida com cache local e sincronização em background:

```
┌─────────────────────────────────────────────────┐
│ Usuário realiza ação (Adiciona água)            │
└──────────┬──────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────┐
│ HistoryCache.addRecord()      [LOCAL - Imediato]│
│ • Adiciona timestamp + quantidade                │
│ • UI atualizada instantaneamente                │
└──────────┬──────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────┐
│ PreferencesManager.update()   [LOCAL - Imediato]│
│ • Atualiza total diário em SharedPreferences    │
│ • Cache de ranking atualizado                   │
└──────────┬──────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────┐
│ ConsumoService.syncDaily()    [BACKGROUND]      │
│ • Executa em thread IO (Dispatchers.IO)         │
│ • Não bloqueia UI                               │
│ • Usa padrão UPSERT                             │
└──────────┬──────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────┐
│ consumo_diario (Banco Supabase)                 │
│ • Registro armazenado permanentemente           │
│ • Disponível para queries de ranking            │
└─────────────────────────────────────────────────┘
```

### Fluxo de Autenticação

```
┌────────────────────────────────────┐
│ Usuário preenche email + senha     │
└──────────┬─────────────────────────┘
           │
           ▼
┌────────────────────────────────────┐
│ signUpWith(email, password)        │
│ → Supabase Auth                    │
└──────────┬─────────────────────────┘
           │
           ├─ Falha? → Erro retornado
           │
           ▼
┌────────────────────────────────────┐
│ INSERT INTO usuarios               │
│ (nome, email, senha_hash, ...)     │
└──────────┬─────────────────────────┘
           │
           ▼
┌────────────────────────────────────┐
│ Retorna user ID                    │
│ Usuário autenticado                │
└────────────────────────────────────┘
```

### Fluxo de Ranking (com JOIN)

```sql
SELECT
    usuario_id,
    total_ml,
    usuarios(nome)
FROM consumo_diario
WHERE data = TODAY
ORDER BY total_ml DESC
```

**Mapeamento de resposta**:
- Serializa para `ConsumoComUsuario`
- Campo `usuarios` mapeado como objeto aninhado `UsuarioSimples`
- Converte para Java `RankingEntry` para exibição

### Padrão Offline-First

```
┌───────────────────────────┐
│ UI solicita ranking       │
└──────────┬────────────────┘
           │
           ▼
┌───────────────────────────┐
│ PreferencesManager        │
│ Carrega cache             │
│ Exibe imediatamente       │
└──────────┬────────────────┘
           │
           ▼
┌───────────────────────────┐
│ RankingService            │
│ getDailyRanking()         │
│ Executa em background     │
│ Não bloqueia UI           │
└──────────┬────────────────┘
           │
           ├─ Cache hit?
           │  ↓
           │  Atualiza cache + Refresh UI
           │
           └─ Cache miss/erro?
              ↓
              Mostra cache ou mensagem de erro
```

---

## Padrões de Integração

### 1. Padrão Singleton para Cliente
O cliente Supabase é inicializado uma única vez e reutilizado em toda a aplicação através de um singleton.

```kotlin
val client = SupabaseClient.client  // Sempre a mesma instância
```

### 2. Padrão UPSERT para Sincronização
Inserir ou atualizar registros de consumo sem duplicação:

```kotlin
suspend fun syncDailyConsumption(usuarioId: Int, data: String, totalMl: Int) {
    val consumo = ConsumoDiario(usuario_id = usuarioId, data = data, total_ml = totalMl)
    SupabaseClient.client.from("consumo_diario").upsert(consumo)
}
```

### 3. Padrão Repository com Callbacks
Abstrair operações assíncronas para Java usando callbacks:

```kotlin
// Service (Kotlin coroutines)
suspend fun login(email: String, senha: String): Usuario?

// Repository (Java callbacks)
fun login(email: String, senha: String, callback: (Usuario?, String?) -> Unit) {
    runAsync({ loginService(email, senha) }, callback)
}
```

### 4. Padrão Offline-First
Manter cache local e sincronizar em background:

1. **Escrita**: Salva localmente imediatamente, sincroniza depois
2. **Leitura**: Carrega cache primeiro, atualiza em background
3. **Conflitos**: UPSERT resolve automaticamente

### 5. Padrão de JOIN com Relacionamentos
Aproveitar relacionamentos PostgreSQL para queries eficientes:

```kotlin
// Retorna usuário com dados de consumo
val consumos = SupabaseClient.client
    .from("consumo_diario")
    .select("""*, usuarios(nome)""")
    .eq("data", today)
    .decodeList<ConsumoComUsuario>()
```

---

## Operações Comuns

### Usuários

#### Registrar novo usuário
```kotlin
// Service
suspend fun registerUser(nome: String, email: String, senha: String): Usuario? {
    val userAuth = SupabaseClient.client.auth.signUpWith(Email) {
        this.email = email
        this.password = senha
    }

    val usuario = Usuario(
        nome = nome,
        email = email,
        senha_hash = userAuth.user?.id
    )

    return SupabaseClient.client.from("usuarios").insert(usuario).decodeSingle()
}
```

#### Login
```kotlin
suspend fun login(email: String, senha: String): Usuario? {
    SupabaseClient.client.auth.signInWith(Email) {
        this.email = email
        this.password = senha
    }

    return getUserByEmail(email)
}
```

### Consumo de Água

#### Sincronizar consumo diário
```kotlin
suspend fun syncDailyConsumption(usuarioId: Int, data: String, totalMl: Int) {
    val consumo = ConsumoDiario(
        usuario_id = usuarioId,
        data = data,
        total_ml = totalMl
    )

    SupabaseClient.client.from("consumo_diario").upsert(consumo)
}
```

#### Obter histórico
```kotlin
suspend fun getConsumptionHistory(usuarioId: Int, days: Int = 30): List<ConsumoDiario> {
    val fromDate = LocalDate.now().minusDays(days.toLong())

    return SupabaseClient.client
        .from("consumo_diario")
        .select()
        .eq("usuario_id", usuarioId)
        .gte("data", fromDate.toString())
        .order("data", Order.DESCENDING)
        .decodeList()
}
```

### Grupos

#### Criar grupo
```kotlin
suspend fun createGroup(nome: String, descricao: String?, criadorId: Int): GrupoData? {
    val grupo = GrupoData(
        nome = nome,
        descricao = descricao ?: "",
        criador_id = criadorId
    )

    val criadoGrupo = SupabaseClient.client
        .from("grupos")
        .insert(grupo)
        .decodeSingle<GrupoData>()

    // Adicionar criador como membro
    criadoGrupo?.id?.let {
        addMemberToGroup(it, criadorId)
    }

    return criadoGrupo
}
```

#### Adicionar membro ao grupo
```kotlin
suspend fun addMemberToGroup(grupoId: Int, usuarioId: Int) {
    val membro = MembrosGrupoData(grupo_id = grupoId, usuario_id = usuarioId)
    SupabaseClient.client.from("membros_grupo").insert(membro)
}
```

### Rankings

#### Ranking diário (com JOIN)
```kotlin
suspend fun getDailyRanking(): List<RankingEntry> {
    val today = LocalDate.now().toString()

    val consumos = SupabaseClient.client
        .from("consumo_diario")
        .select("""*, usuarios(nome)""")
        .eq("data", today)
        .order("total_ml", Order.DESCENDING)
        .decodeList<ConsumoComUsuario>()

    return consumos.mapIndexed { index, consumo ->
        RankingEntry(
            id = consumo.usuario_id,
            nome = consumo.usuarios?.nome ?: "Desconhecido",
            consumo_hoje = consumo.total_ml,
            posicao = (index + 1).toLong()
        )
    }
}
```

---

## Resumo Técnico

| Aspecto | Detalhes |
|---------|----------|
| **Banco de Dados** | Supabase PostgreSQL |
| **Tabelas** | 4 (usuarios, consumo_diario, grupos, membros_grupo) |
| **SDK Cliente** | `io.github.jan.supabase` (Kotlin) |
| **Módulos Usados** | Postgrest (queries), Auth (autenticação) |
| **Tipo de Autenticação** | Email/Senha via Supabase Auth |
| **Configuração** | `local.properties` + `BuildConfig` |
| **Cache Local** | SharedPreferences + HistoryCache customizado |
| **Async/Coroutines** | Kotlin Coroutines + Adapters para callbacks |
| **Arquitetura** | Service → Repository → UI (MVC/MVP) |
| **Serialização** | `kotlinx.serialization` para classes Kotlin |
| **Gerenciamento de Schema** | Manual via dashboard Supabase (sem arquivos de migração) |
| **Padrão de Sync** | Offline-first com sincronização em background |

---

## Referências Rápidas

### Arquivos principais
- Cliente Supabase: `app/src/main/java/com/example/waterchamp/data/remote/SupabaseClient.kt`
- Serviços: `app/src/main/java/com/example/waterchamp/data/remote/`
  - `UserService.kt`
  - `ConsumoService.kt`
  - `GrupoService.kt`
  - `RankingService.kt`
- Repositórios: `app/src/main/java/com/example/waterchamp/data/repository/`
- Modelos: `app/src/main/java/com/example/waterchamp/model/`
- Cache local: `app/src/main/java/com/example/waterchamp/data/local/`

### URL do Supabase
https://kajdflcgqthnuhjvfnwy.supabase.co

### Dashboard
Acesse através da plataforma Supabase para gerenciar dados, ver logs e configurar políticas RLS.
