# Frigo Malin 🍽️

Application Android de gestion de stock alimentaire et de suggestion de repas, développée en Kotlin / Jetpack Compose.

## Fonctionnalités

- **Stock d'ingrédients** : quantités et dates de péremption, alertes sur les produits bientôt périmés
- **Base de recettes** : recettes pré-remplies + ajout de recettes personnelles
- **Suggestions intelligentes** : les recettes sont classées selon le pourcentage d'ingrédients disponibles, avec priorité à celles utilisant des produits proches de la péremption
- **Liste de courses** : générée automatiquement pour les ingrédients manquants d'une recette choisie
- **Planning repas** : organisation des recettes sur la semaine

## Stack technique

- Kotlin
- Jetpack Compose / Material3
- Room (persistance locale)
- DataStore (préférences)
- Architecture MVVM

## Build

Ce projet est développé et compilé exclusivement via GitHub :

- Les fichiers sont édités directement dans l'éditeur web GitHub
- Un workflow GitHub Actions (`.github/workflows/build.yml`) compile automatiquement un APK debug à chaque push sur `main`
- L'APK compilé est disponible dans la release **`latest-debug`** du repo (section *Releases*)

Aucun environnement Android Studio local n'est requis pour contribuer.

## Structure du projet