# Data Analysis - RegistoBiografico

## Entities and Fields

### 1. DadosRegistoBiografico (Biographical Record - Main Entity: Parliamentarian)

| Field | Type | Description |
|-------|------|-------------|
| CadId | unsignedShort | Unique identifier for the parliamentarian |
| CadNomeCompleto | string | Full legal name |
| CadDtNascimento | date | Date of birth |
| CadSexo | string | Sex/Gender (M/F) |
| CadProfissao | string | Profession/Occupation |
| cadDeputadoLegis | complex | Parliamentary terms across legislatures |
| cadHabilitacoes | complex | Educational qualifications |
| cadCargosFuncoes | complex | Positions and functions held (current and past) |
| cadCondecoracoes | complex | Decorations and awards received |
| cadTitulos | complex | Titles (academic, honorary, etc.) |
| cadObrasPublicadas | complex | Published works |
| cadActividadeOrgaos | complex | Activity in parliamentary committees/bodies |

### 2. DadosDeputadoLegis (Parliamentary Term)

| Field | Type | Description |
|-------|------|-------------|
| DepNomeParlamentar | string | Parliamentary name (often abbreviated/nickname) |
| LegDes | string | Legislature designation (XII, XIII, XIV, XV, XVI, XVII) |
| CeDes | string | Constituency (electoral circle) |
| ParSigla | string | Party acronym |
| ParDes | string | Party full name |
| GpSigla | string | Parliamentary group acronym |
| GpDes | string | Parliamentary group full name |
| urlVideoBiografia | string | URL to biography video |

### 3. DadosHabilitacoes (Educational Qualifications)

| Field | Type | Description |
|-------|------|-------------|
| HabId | unsignedShort | Qualification ID |
| HabDes | string | Qualification description (field of study) |
| HabTipoId | unsignedByte | Qualification type ID |
| HabEstado | string | Status (C = Concluded/Completed) |

### 4. DadosCargosFuncoes (Positions and Functions)

| Field | Type | Description |
|-------|------|-------------|
| FunId | unsignedShort | Function ID |
| FunDes | string | Function/Position description |
| FunOrdem | unsignedByte | Order/priority |
| FunAntiga | string | Is historical/past (S = Yes, N = No/Current) |

### 5. DadosCondecoracoes (Decorations and Awards)

| Field | Type | Description |
|-------|------|-------------|
| CodId | unsignedShort | Decoration ID |
| CodDes | string | Decoration description |
| CodOrdem | unsignedByte | Order/priority |

### 6. DadosTitulos (Titles)

| Field | Type | Description |
|-------|------|-------------|
| TitId | unsignedShort | Title ID |
| TitDes | string | Title description |
| TitOrdem | unsignedInt | Order/priority |

### 7. DadosObrasPublicadas (Published Works)

| Field | Type | Description |
|-------|------|-------------|
| PubId | unsignedShort | Publication ID |
| PubDes | string | Publication description (title, publisher, year) |
| PubOrdem | unsignedByte | Order/priority |

### 8. DadosOrgaos (Parliamentary Bodies/Committees)

| Field | Type | Description |
|-------|------|-------------|
| orgId | unsignedShort | Body ID |
| orgDes | string | Body/committee description |
| orgSigla | string | Body/committee acronym |
| legDes | string | Legislature |
| timDes | string | Member status (Efetivo = Effective, Suplente = Alternate) |
| cargoDes | complex | Position within the body (optional) |

### 9. RegistoInteresses (Declaration of Interests)

The schema also includes multiple versions of interest declarations:

**RegistoInteressesV1:**
- rgiActividades - Professional activities
- rgiApoiosBeneficios - Support and benefits received
- rgiServicosPrestados - Services provided
- rgiOutrasSituacoes - Other situations
- rgiCargosSociais - Social positions (board roles)
- rgiSociedades - Companies/societies with participation

**RegistoInteressesV2:**
- Similar to V1 plus:
- rgiRegimeBensDes - Matrimonial property regime

**RegistoInteressesV3:**
- RecordInterests with:
- Activities - Professional activities
- SocialPositions - Social positions
- Supports - Support received
- ServicesProvided - Services provided
- Societies - Companies
- OtherSituations - Other situations

**RegistoInteressesV5:**
- FactoDeclaracao - Declaration facts
- CargoFuncao - Position/function
- DataInicioFuncao - Start date
- DataAlteracaoFuncao - Modification date
- DataCessacaoFuncao - End date
- ChkDeclaracao - Declaration check

## Key Observations

1. This data represents Portuguese parliament members (Deputados) from the Assembly of the Republic (Assembleia da República)
2. Data spans multiple legislatures (XII through XVII - approximately 1991-2026)
3. Includes rich biographical information: personal details, education, career positions, publications, decorations
4. Contains declarations of interests required for transparency
5. Tracks parliamentary activity in committees and working groups

## Data Source

- Source: Portuguese Assembly of the Republic (Assembleia da República)
- Dataset: Registo Biográfico dos Deputados
- Purpose: Public transparency and historical record of parliamentarians
