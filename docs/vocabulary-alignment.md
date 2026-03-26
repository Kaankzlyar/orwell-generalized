# Vocabulary Alignment - RegistoBiografico

This document maps the fields from the input data to existing vocabulary terms from established ontologies and controlled vocabularies.

## Input Fields to Vocabulary Mapping

| Input Field | EPVOC | UK Parliament | POWER | FOAF | ORG | Schema.org | Recommended Term |
|-------------|-------|---------------|-------|------|-----|------------|-----------------|
| **DadosRegistoBiografico (Person/Parliamentarian)** |||||
| CadId | - | - | - | - | org:identifier | identifier | `org:identifier` |
| CadNomeCompleto | epvoc:person | - | foaf:name | foaf:name | - | schema:name | `foaf:name` |
| CadDtNascimento | - | - | - | foaf:birthday | - | schema:birthDate | `foaf:birthday` |
| CadSexo | - | - | - | foaf:gender | - | schema:gender | `schema:gender` |
| CadProfissao | - | - | - | - | - | schema:jobTitle | `schema:jobTitle` |
| **DadosDeputadoLegis (Parliamentary Term)** |||||
| DepNomeParlamentar | - | - | - | foaf:nick | - | schema:alternateName | `foaf:nick` |
| LegDes | epvoc:parliamentaryTerm | - | - | - | - | - | `epvoc:parliamentaryTerm` |
| CeDes | epvoc:constituency | - | - | - | - | schema:address | `epvoc:constituency` |
| ParSigla | epvoc:politicalParty | - | power:PoliticalParty | - | org:memberOf | schema:memberOf | `org:memberOf` → Party |
| ParDes | epvoc:politicalParty | - | power:PoliticalParty | - | org:memberOf | schema:memberOf | `org:memberOf` → Party |
| GpSigla | epvoc:parliamentaryGroup | - | power:ParliamentaryGroup | - | org:memberOf | schema:memberOf | `epvoc:parliamentaryGroup` |
| GpDes | epvoc:parliamentaryGroup | - | power:ParliamentaryGroup | - | org:memberOf | schema:memberOf | `epvoc:parliamentaryGroup` |
| urlVideoBiografia | - | - | - | - | - | schema:video | `schema:video` |
| **DadosHabilitacoes (Education)** |||||
| HabId | - | - | - | - | - | schema:identifier | `schema:identifier` |
| HabDes | - | - | - | - | - | schema:educationalAlignment | `schema:educationalLevel` (or SKOS concept) |
| HabTipoId | - | - | - | - | - | - | Map to education type |
| HabEstado | - | - | - | - | - | schema:completion | `schema:completion` (Completed) |
| **DadosCargosFuncoes (Positions/Functions)** |||||
| FunId | - | - | - | - | - | schema:identifier | `schema:identifier` |
| FunDes | epvoc:capacityRole | - | power:Mandate | foaf:topic_interest | org:role | schema:role | `org:role` or `power:Mandate` |
| FunOrdem | - | - | - | - | - | schema:position | `schema:position` |
| FunAntiga | - | - | - | - | - | schema:endTime | `schema:endTime` (historical/current) |
| **DadosCondecoracoes (Decorations)** |||||
| CodId | - | - | - | - | - | schema:identifier | `schema:identifier` |
| CodDes | - | - | - | - | - | schema:award | `schema:award` |
| CodOrdem | - | - | - | - | - | schema:position | `schema:position` |
| **DadosTitulos (Titles)** |||||
| TitId | - | - | - | - | - | schema:identifier | `schema:identifier` |
| TitDes | - | - | - | - | - | schema:honorificPrefix | `schema:honorificPrefix` |
| TitOrdem | - | - | - | - | - | schema:position | `schema:position` |
| **DadosObrasPublicadas (Published Works)** |||||
| PubId | - | - | - | - | - | schema:identifier | `schema:identifier` |
| PubDes | - | - | - | - | - | schema:workExample | `schema:workExample` / `bibo:Document` |
| PubOrdem | - | - | - | - | - | schema:position | `schema:position` |
| **DadosOrgaos (Committees/Bodies)** |||||
| orgId | epvoc:body | - | power:PoliticalInstitution | - | org:Organization | schema:identifier | `org:Organization` |
| orgDes | epvoc:body | - | power:PoliticalInstitution | - | org:Organization | schema:name | `schema:name` |
| orgSigla | epvoc:body | - | power:PoliticalInstitution | - | org:alternateName | schema:alternateName | `org:alternateName` |
| legDes | epvoc:parliamentaryTerm | - | - | - | - | - | `epvoc:parliamentaryTerm` |
| timDes | epvoc:membershipStatus | - | - | - | org:member | schema:member | `epvoc:membershipStatus` (Efetivo/Suplente) |
| cargoDes | epvoc:capacityRole | - | power:Mandate | - | org:role | schema:role | `org:role` |
| **RegistoInteresses (Declaration of Interests)** |||||
| rgiCargoDes | epvoc:capacityRole | - | power:Mandate | - | org:role | schema:role | `org:role` |
| rgiActividades | - | - | power:Activity | - | - | schema:experience | `schema:experience` / `power:Activity` |
| rgiApoiosBeneficios | - | - | - | - | - | schema:award | `schema:award` (or custom) |
| rgiServicosPrestados | - | - | - | - | - | schema:service | `schema:service` |
| rgiCargosSociais | - | - | power:SocialPosition | - | org:role | schema:memberOf | `org:role` / `power:SocialPosition` |
| rgiSociedades | - | - | power:Company | - | org:Organization | schema:memberOf | `org:Organization` |

## Ontology Recommendations

### Primary Recommendations

1. **Person Entity**: Use `foaf:Person` as the base class for parliamentarians
   - Properties: `foaf:name`, `foaf:birthday`, `foaf:gender`, `foaf:nick`

2. **Membership/Role**: Use W3C `org:` ontology
   - `org:Membership` for parliamentary terms
   - `org:Role` for positions and functions
   - `org:Organization` for parties and committees

3. **Parliament-specific**: Use EPVOC where applicable
   - `epvoc:parliamentaryTerm` for legislatures
   - `epvoc:parliamentaryGroup` for parliamentary groups
   - `epvoc:capacityRole` for roles within committees

4. **Schema.org** for general properties:
   - `schema:jobTitle` for profession
   - `schema:identifier` for IDs
   - `schema:award` for decorations

### Portuguese-SpecificPOWER Ontology

The POWER ontology (from University of Lisbon) is specifically designed for Portuguese political entities and includes:
- `power:Politician`
- `power:PoliticalParty`
- `power:ParliamentaryGroup`
- `power:Mandate` - for positions/roles
- `power:Activity` - for professional activities
- `power:SocialPosition` - for social positions
- `power:Company` - for company holdings

**Recommendation**: Use POWER for Portuguese-specific concepts where available.

## External Vocabularies to Consider

| Vocabulary | Purpose | URL |
|------------|---------|-----|
| SKOS | Educational qualifications | https://www.w3.org/2004/02/skos/core# |
| BIBO | Published works | https://purl.org/ontology/bibo/ |
| DC | General metadata | https://purl.org/dc/elements/1.1/ |
| VCard | Contact info | https://www.w3.org/2006/vcard/ns# |

## Implementation Notes

1. The main entity should be a `foaf:Person` with `org:Membership` for each parliamentary term
2. Use `org:Organization` for parties (political parties) and committees
3. Multiple memberships should be modeled with separate `org:Membership` instances
4. Historical vs current positions can be distinguished using `schema:endTime` or temporal properties
5. The Portuguese legislature numbering (XII, XIII, etc.) maps to actual years in `epvoc:parliamentaryTerm`
