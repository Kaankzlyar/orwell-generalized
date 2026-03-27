# Conceptual Model Diagrams

This document contains modular mermaid diagrams showing the conceptual model for the Portuguese Parliament Biographical Records (RegistoBiografico).

---

## Diagram 1: Core Person Entity

```mermaid
graph TD
    Person["Parliamentarian<br/>(foaf:Person)"] -->|hasIdentifier| pid["Person ID<br/>(org:identifier)"]
    Person -->|fullName| name["Full Name<br/>(foaf:name)"]
    Person -->|birthDate| dob["Date of Birth<br/>(foaf:birthday)"]
    Person -->|gender| gender["Sex/Gender<br/>(schema:gender)"]
    Person -->|profession| prof["Profession<br/>(schema:jobTitle)"]
    Person -->|hasMembership| Mem["Parliamentary Membership<br/>(org:Membership)"]
    Person -->|hasEducation| Edu["Education<br/>(schema:EducationalLevel)"]
    Person -->|hasPosition| Pos["Position/Function<br/>(org:Role)"]
    Person -->|hasAward| Award["Decoration<br/>(schema:award)"]
    Person -->|hasTitle| Title["Title<br/>(schema:honorificPrefix)"]
    Person -->|hasWork| Work["Published Work<br/>(bibo:Document)"]
```

---

## Diagram 2: Parliamentary Membership and Terms

```mermaid
graph TD
    Mem["Membership<br/>(org:Membership)"] -->|duringTerm| Term["Legislature<br/>(epvoc:parliamentaryTerm)"]
    Mem -->|inConstituency| Const["Constituency<br/>(epvoc:constituency)"]
    Mem -->|memberOfParty| Party["Political Party<br/>(org:Organization)"]
    Mem -->|memberOfGroup| Group["Parliamentary Group<br/>(epvoc:parliamentaryGroup)"]
    Mem -->|hasStatus| Status["Membership Status<br/>(epvoc:membershipStatus)"]
    Party -->|hasAcronym| pAc["Party Acronym"]
    Party -->|hasName| pName["Party Full Name"]
    Group -->|hasAcronym| gAc["Group Acronym"]
    Group -->|hasName| gName["Group Full Name"]
    Term -->|legislatureNumber| legNum["Legislature (XII-XVII)"]
```

---

## Diagram 3: Parliamentary Bodies and Committees

```mermaid
graph TD
    Person["Parliamentarian"] -->|participatesIn| Body["Parliamentary Body<br/>(org:Organization)"]
    Body -->|bodyName| bName["Body Name<br/>(schema:name)"]
    Body -->|bodyAcronym| bAc["Body Acronym<br/>(org:alternateName)"]
    Body -->|duringLegislature| Term["Legislature"]
    Body -->|hasCapacity| Capacity["Capacity/Role<br/>(epvoc:capacityRole)"]
    Body -->|hasMemberStatus| mStat["Member Status"]
    Body -->|isType| Comm["Committee"]
    Body -->|isType| WG["Working Group"]
    Body -->|isType| Int["Inquiry Committee"]
```

---

## Diagram 4: Positions and Functions

```mermaid
graph TD
    P4["Parliamentarian"] -->|holds| Pos["Position/Function<br/>(org:Role)"]
    Pos -->|positionDescription| pDes["Position Description"]
    Pos -->|hasOrder| pOrder["Priority Order"]
    Pos -->|isHistorical| pHist["Historical Status"]
    Pos -->|temporalScope| Temp["Temporal Information"]
    Temp -->|startDate| sdate["Start Date"]
    Temp -->|endDate| edate["End Date"]
```

---

## Diagram 5: Declarations of Interests

```mermaid
graph TD
    Person["Parliamentarian"] -->|submits| Decl["Declaration of Interests"]
    Decl -->|forLegislature| Term["Legislature"]
    Decl -->|version| ver["Version (V1-V5)"]
    Decl -->|declarationDate| date["Date"]
    Decl -->|forPosition| pos["Position Held"]
    Decl -->|containsActivity| Act["Professional Activity"]
    Act -->|activityDesc| aDes["Activity Description"]
    Act -->|isPaid| paid["Remunerated"]
    Decl -->|hasSocialPosition| SP["Social Position"]
    SP -->|position| sPos["Position/Role"]
    SP -->|entity| entity["Entity/Organization"]
    SP -->|area| area["Activity Area"]
    Decl -->|hasSociety| Soc["Company/Society"]
    Soc -->|companyName| cName["Entity Name"]
    Soc -->|headOffice| cLoc["Head Office Location"]
    Soc -->|share| share["Social Participation"]
    Decl -->|hasSupport| Sup["Support/Benefit"]
    Decl -->|hasService| Serv["Service Provided"]
    Decl -->|hasOtherSituation| Oth["Other Situation"]
```

---

## Diagram 6: Full Overview

```mermaid
graph TD
    P["Parliamentarian"] --> ID["Identifier"]
    P --> Name["Full Name"]
    P --> DOB["Date of Birth"]
    P --> Gender["Gender"]
    P --> Prof["Profession"]
    P --> Mem1["Parliamentary Membership"]
    P --> Edu["Education Qualifications"]
    P --> Role["Roles and Functions"]
    P --> Award["Decorations"]
    P --> Title["Titles"]
    P --> Work["Published Works"]
    P --> Decl["Declaration of Interests"]
    Mem1 --> Party["Political Party"]
    Mem1 --> Group["Parliamentary Group"]
    Mem1 --> Leg["Legislature"]
    Leg --> Const["Constituency"]
    P --> Part["Participation"]
    Part --> Body["Committee Body"]
    Body --> Role2["Committee Role"]
```

---

## Summary of Key Relationships

| Relationship | Source | Target | Property |
|--------------|--------|--------|----------|
| Person → Membership | foaf:Person | org:Membership | org:hasMembership |
| Membership → Legislature | org:Membership | epvoc:parliamentaryTerm | epvoc:memberDuring |
| Membership → Party | org:Membership | org:Organization | org:memberOf |
| Person → Position | foaf:Person | org:Role | org:holds |
| Person → Committee | foaf:Person | org:Organization | org:memberOf |
| Declaration → Person | Declaration | foaf:Person | schema:author |
| Education → Person | foaf:Person | skos:Concept | schema:educationalLevel |
