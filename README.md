# Thequilibre

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](#)
[![Java](https://img.shields.io/badge/Java-11-007396?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Gradle](https://img.shields.io/badge/Build-Gradle-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
[![Material](https://img.shields.io/badge/UI-Material%20Components-757575?logo=materialdesign)](https://m3.material.io/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-27-blue)](#)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-blue)](#)
[![Game](https://img.shields.io/badge/Game-Endless%20Dodging-orange)](#)
[![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-red)](#)

---


<details>
  <summary><h2>README Francais</h2></summary>

Thequilibre est un projet de jeu Android, **développé en 9 heures seulement** centre sur l'equilibre, la reactivite et une progression de difficulte infinie mais juste.

Le joueur doit eviter des carres obstacles tout en gardant le controle du baton. Les obstacles apparaissent dans 6 slots fixes, grandissent progressivement, puis deviennent dangereux a leur taille maximale.

---

## Fonctionnalites

- Selection de difficulte (Easy / Medium / Hard)
- 6 slots obstacles fixes (carres fantomes de reference)
- Animation de croissance progressive des obstacles (petit vers taille max)
- Etat dangereux active uniquement a la taille maximale
- Boucle de jeu infinie avec augmentation progressive de la difficulte
- Mode Hard avec apparition simultanee de 1, 2 ou 3 obstacles
- Controle du baton via gyroscope + deplacement tactile
- Ecran "About us" avec profils equipe (GitHub / LinkedIn)

---

## Plateforme supportee

- ✅ Android (smartphone, mode portrait)

---

## Stack technique

- Java 11
- Android SDK (minSdk 27, targetSdk 36)
- AndroidX AppCompat / Activity / ConstraintLayout
- Material Components
- Gradle (Kotlin DSL)

---

## Installation

### 1. Prerequis

- Android Studio (derniere version stable recommandee)
- Android SDK Platform 36 installe
- JDK 11

### 2. Cloner et ouvrir le projet

```bash
git clone <your-repo-url>
cd Thequilibre
```

Ouvrir ensuite le projet dans Android Studio.

### 3. Compiler l'APK debug

```bash
./gradlew :app:assembleDebug
```

### 4. Lancer les tests

```bash
./gradlew test
```

Tests instrumentes (optionnel):

```bash
./gradlew connectedAndroidTest
```

---

## Notes gameplay

- Les obstacles apparaissent dans des slots libres de maniere aleatoire.
- Au moins un slot reste toujours libre pour conserver une possibilite d'esquive.
- En mode Hard, un cycle de spawn peut creer 1, 2 ou 3 obstacles en meme temps (slots distincts).
- Chaque obstacle garde le meme comportement visuel: croissance progressive puis danger a taille max.

---

## A propos de l'equipe

Developpe par :

- **Corentin JERE**  
  [![GitHub](https://img.shields.io/badge/GitHub-CJs0800-yellow?logo=github)](https://github.com/CJs0800)
  [![LinkedIn](https://img.shields.io/badge/LinkedIn-Corentin_JERE-0A66C2?logo=linkedin)](https://www.linkedin.com/in/corentin-jere/)
  [![Email](https://img.shields.io/badge/Email-corentinjere@gmail.com-red?logo=gmail&logoColor=white)](mailto:corentinjere@gmail.com)

- **Alex LECOMTE**  
  [![GitHub](https://img.shields.io/badge/GitHub-alexLcmt-yellow?logo=github)](https://github.com/alexLcmt)
  [![LinkedIn](https://img.shields.io/badge/LinkedIn-Alex_LECOMTE-0A66C2?logo=linkedin)](https://www.linkedin.com/in/alex-lecomte-3a6503296/)
  [![Email](https://img.shields.io/badge/Email-alexlecomte144@gmail.com-red?logo=gmail&logoColor=white)](mailto:alexlecomte144@gmail.com)

- **Alyssia LECLERC**  
  [![GitHub](https://img.shields.io/badge/GitHub-Alyssl24-yellow?logo=github)](https://github.com/Alyssl24/)
  [![LinkedIn](https://img.shields.io/badge/LinkedIn-Alyssia_LECLERC-0A66C2?logo=linkedin)](https://www.linkedin.com/in/alyssia-leclerc-ab1839313/)
  [![Email](https://img.shields.io/badge/Email-ninaly.leclerc@gmail.com-red?logo=gmail&logoColor=white)](mailto:ninaly.leclerc@gmail.com)

</details>

---


<details open>
  <summary><h2>English README</h2></summary>

Thequilibre is an Android game project, **developed in only 9 hours**, focused on balance, responsiveness, and fair endless difficulty progression.

The player must avoid obstacle squares while keeping control of the baton. Obstacles appear in 6 fixed slots, grow progressively, and become dangerous at max size.

---

## Features

- Difficulty selection (Easy / Medium / Hard)
- 6 fixed obstacle slots (ghost reference zones)
- Progressive obstacle growth animation (small to full size)
- Danger state only at maximum obstacle size
- Endless gameplay loop with progressive scaling over time
- Hard mode simultaneous spawns (1, 2, or 3 obstacles at once)
- Gyroscope-based baton rotation + touch movement
- About screen with team profiles (GitHub / LinkedIn)

---

## Supported Platform

- ✅ Android (phone, portrait mode)

---

## Tech Stack

- Java 11
- Android SDK (minSdk 27, targetSdk 36)
- AndroidX AppCompat / Activity / ConstraintLayout
- Material Components
- Gradle (Kotlin DSL)

---

## Project Setup

### 1. Prerequisites

- Android Studio (latest stable recommended)
- Android SDK Platform 36 installed
- JDK 11

### 2. Clone and open

```bash
git clone <your-repo-url>
cd Thequilibre
```

Open the project in Android Studio.

### 3. Build debug APK

```bash
./gradlew :app:assembleDebug
```

### 4. Run tests

```bash
./gradlew test
```

Optional instrumentation tests:

```bash
./gradlew connectedAndroidTest
```

---

## Gameplay Notes

- Obstacles spawn over time in random free slots.
- At least one slot always stays free to preserve dodge possibilities.
- In Hard mode, a spawn tick may create 1, 2, or 3 distinct obstacles simultaneously.
- Each spawned obstacle keeps the same growth behavior and visual readability.

---

## Team

Developed by:

- **Corentin JERE**  
  [![GitHub](https://img.shields.io/badge/GitHub-CJs0800-yellow?logo=github)](https://github.com/CJs0800)
  [![LinkedIn](https://img.shields.io/badge/LinkedIn-Corentin_JERE-0A66C2?logo=linkedin)](https://www.linkedin.com/in/corentin-jere/)
  [![Email](https://img.shields.io/badge/Email-corentinjere@gmail.com-red?logo=gmail&logoColor=white)](mailto:corentinjere@gmail.com)

- **Alex LECOMTE**  
  [![GitHub](https://img.shields.io/badge/GitHub-alexLcmt-yellow?logo=github)](https://github.com/alexLcmt)
  [![LinkedIn](https://img.shields.io/badge/LinkedIn-Alex_LECOMTE-0A66C2?logo=linkedin)](https://www.linkedin.com/in/alex-lecomte-3a6503296/)
  [![Email](https://img.shields.io/badge/Email-alexlecomte144@gmail.com-red?logo=gmail&logoColor=white)](mailto:alexlecomte144@gmail.com)

- **Alyssia LECLERC**  
  [![GitHub](https://img.shields.io/badge/GitHub-Alyssl24-yellow?logo=github)](https://github.com/Alyssl24/)
  [![LinkedIn](https://img.shields.io/badge/LinkedIn-Alyssia_LECLERC-0A66C2?logo=linkedin)](https://www.linkedin.com/in/alyssia-leclerc-ab1839313/)
  [![Email](https://img.shields.io/badge/Email-ninaly.leclerc@gmail.com-red?logo=gmail&logoColor=white)](mailto:ninaly.leclerc@gmail.com)

</details>

---



## License

**© 2026 Alyssl24 CJs0800 alexLcmt. All Rights Reserved.**

This project is publicly accessible for viewing purposes only.  
No part of this code may be copied, modified, distributed, or reused without explicit permission from the authors.

