# Who Are You? — Mise à jour Google Play 0.3.0 (5)

## Statut vérifié

- Dépôt : `dbrckk/Who-are-you`, branche `main`.
- Package **inchangé** : `com.whoareyou.app`.
- Version du code source : `versionName = "0.3.0"`, `versionCode = 5`, `minSdk = 26`, `targetSdk = 36`.
- Android CI, M59, Android APK et M56 Release Candidate ont réussi sur le commit `d31f1ba7547c050e9a678f87c2871897b58c8832` le 9 octobre 2026. Le commit `9db8537c326dd37d57fb91ea16619b6a06037a69` suivant ne modifie que le contexte généré.
- **Non vérifiés / non exécutés pour cette version** : construction du `playRelease` signé avec la clé actuelle ; état, versionCode maximal et certificat d'upload de l'application dans Play Console ; envoi en piste Internal ; validation d'une mise à jour par-dessus l'ancienne version distribuée par Google Play ; publication Production.
- Un APK Candidate/RC signé avec une clé de débogage et un AAB `release` non signé **ne doivent jamais être envoyés à Google Play**.

## Avant tout upload : contrôler l'identité de la mise à jour

1. Dans **la fiche existante** de l'application Play Console (ne pas créer une nouvelle application), confirmer le package `com.whoareyou.app`.
2. Dans **App bundle explorer / versions déjà livrées**, relever le plus grand `versionCode` accepté sur toutes les pistes et vérifier qu'il est **strictement inférieur à 5**. Sinon, augmenter le versionCode puis reconstruire et revalider (ne pas réutiliser 5).
3. Dans **Intégrité de l'application / Play App Signing**, confirmer que l'upload keystore fourni au workflow correspond au **certificat d'upload** enregistré. Ne pas générer un nouveau keystore pour remplacer l'ancien. Si la clé d'upload est perdue, suivre la procédure officielle de réinitialisation de la clé d'upload via Play Console.
4. Confirmer que l'application installée à mettre à jour vient bien de **Google Play** et utilise la même identité de signature. Un APK Candidate/debug installé manuellement peut ne pas accepter la mise à jour Play par-dessus ; **ne pas désinstaller avant d'avoir protégé les données locales**.
5. Contrôler les déclarations Play Console (Data safety, publicité, audience, politique de confidentialité), l'état de `remove_ads_lifetime`, la compatibilité des librairies natives et l'absence de blocage politique.

## 1. Générer le bundle signé (aucune publication)

Depuis [GitHub Actions — Play Candidate](https://github.com/dbrckk/Who-are-you/actions/workflows/play-candidate.yml) : choisir `main`, puis **Run workflow**, avec `telemetry_enabled=false` sauf décision spécifique de production.

Le job `play-internal` exige les secrets protégés (AdMob production et **keystore d'upload existant**) et doit produire `who-are-you-play-candidate-0.3.0-5` contenant notamment l'AAB signé, `play-candidate.sha256`, l'inventaire des bibliothèques natives, le budget du bundle et `mapping.txt`.

Vérifier : toutes les étapes vertes, `jarsigner` réussi, bundle non vide, budget accepté et empreinte de l'**upload certificate** compatible avec Play Console. Ne jamais envoyer les secrets dans un ticket, un chat ou un commit.

## 2. Publier sur Internal testing

Depuis [GitHub Actions — Play Internal Publish](https://github.com/dbrckk/Who-are-you/actions/workflows/play-internal-publish.yml), sur `main` :

- `publish_to_play=true`
- `release_status=completed` **uniquement une fois les prérequis confirmés** ; sinon `draft`
- `telemetry_enabled=false` si aucun traitement externe n'a été validé

Le workflow reconstruit `bundlePlayRelease` avec la clé d'upload, vérifie la signature et utilise l'API Google Play pour déposer `versionCode=5` sur la piste `qa` (Internal testing). Il doit laisser l'artefact `play-internal-publish-result.json` avec `committed=true` et `versionCode=5`.

Si un prérequis externe ou secret manque, **ne pas tenter un contournement** : configurer l'environnement protégé `play-internal` puis relancer.

## 3. Confirmer la mise à jour sans perte de données

Sur un téléphone **testeur Play** possédant la version précédente installée par Google Play :

1. Conserver l'application et ses données (ne pas utiliser « effacer les données »).
2. Depuis Play Store, installer la mise à jour 0.3.0 (5) sur cette installation existante.
3. Vérifier au minimum : profil et résultats précédents, questionnaire repris, progression et objectifs/habitudes locaux déjà enregistrés, permission refusée/révoquée, lancement à froid, relancement après arrêt du processus, FR/EN.
4. Tester avec la version Play signée : consentement UMP/AdMob, absence de publicité pendant un quiz, achat/restauration `remove_ads_lifetime`.
5. Lire les rapports Android Vitals et Pre-launch report, ainsi que le budget AAB, les bibliothèques natives et le mapping R8.

La validation M777 debug-vers-candidate du dépôt fournit une preuve de non-régression des DataStores, **pas** la preuve d'une mise à jour depuis une installation Google Play existante.

## 4. Promouvoir progressivement en Production

Après vérification réelle des points ci-dessus, utiliser [GitHub Actions — Play Track Promotion](https://github.com/dbrckk/Who-are-you/actions/workflows/play-promote.yml) :

- `target_track=production`
- `release_status=inProgress`, `user_fraction=0.05` (déploiement initial de 5 %)
- `confirm_production=true`
- `confirm_vitals_review=true` et `confirm_prelaunch_report=true` **seulement après les deux revues**
- `commit_edit=true`

Le script promeut le **bundle déjà téléversé** (versionCode 5) : pas de reconstruction, pas de nouvelle clé. Vérifier le reçu `play-promotion-result.json`, l'état de publication dans Play Console et l'absence de blocage par Managed publishing/review. Étendre le rollout après observation des indicateurs réels.

## Notes de version destinées aux utilisateurs

**fr-FR**

> Cette mise à jour ajoute un suivi facultatif des habitudes, des tendances consultables sur l'appareil et des objectifs personnalisés. Elle améliore également l'expérience des quiz, la lisibilité, l'accessibilité et la stabilité lors des mises à jour. Les données de profil et d'habitudes restent stockées localement ; les sources d'activité nécessitent une activation explicite.

**en-US**

> This update adds optional habit tracking, on-device trends and personal goals. It also improves the quiz experience, readability, accessibility and upgrade stability. Profile and habit information stays on the device; activity sources require explicit opt-in.

## Critère de fin

**Succès** uniquement si : bundle Play signé sur le commit voulu, upload key compatible, versionCode supérieur au maximum accepté, publication Internal confirmée par reçu API, upgrade réel depuis Play sans perte de données, rapports et déclarations examinés, puis promotion Production observée. Le seul fait de construire un APK RC ou d'avoir des tests CI verts ne démontre pas la publication.
