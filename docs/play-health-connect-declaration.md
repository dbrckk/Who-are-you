# Google Play Console — Health Connect READ_STEPS declaration

Updated: 2026-10-27. Applies to `com.whoareyou.app`.

## Select in Play Console
- **Health apps declaration** → **Health and fitness** → **Activity and Fitness**: the optional My Habits step tracker and walking goals. This is a separate form from Android health permissions and must also be completed.
- Android health permissions → Health Connect → **Fitness and wellness** (optional daily activity monitoring and personal step-count goals).
- Requested data type: **Steps / read** (`android.permission.health.READ_STEPS`) only. Do not declare write access, sleep, heart rate or medical data.
- Health privacy-policy URL: `https://dbrckk.github.io/Who-are-you/privacy/`.
- Update the Play Store listing with the matching EN/FR sections from `docs/store-listing-en.md` and `docs/store-listing-fr.md`. Upload only when these descriptions are actually published.
- Official guidance: [Google Play Health apps declaration](https://support.google.com/googleplay/android-developer/answer/14738291?hl=en) and [Android health permissions](https://support.google.com/googleplay/android-developer/answer/12991134?hl=en). The declarations must match the actual permission and use case shown in the app.

## Pasteable English explanation for permission review
> Who Are You? includes an optional fitness and wellness feature in My Habits. When a user chooses to enable Activity and grants Health Connect READ_STEPS, the app reads step-count totals to display daily step counts, 7- and 30-day activity averages and patterns, and progress on optional personal step-count goals. This benefits the user by supporting awareness of daily physical activity and self-set movement goals. Only READ_STEPS is requested; no steps are written. These observations and goals are processed and stored on-device only. Daily history covers the current day and preceding 30 days when the app is in use; older saved records are removed when the app next runs. Step data is not used for ads, uploaded or shared with third parties. Users can decline access without affecting quizzes, disable Activity to delete local step history, and revoke the permission in Android Health Connect settings.

## Explication française
> Who Are You? propose dans « Mes habitudes » un suivi facultatif de l’activité physique. Après activation volontaire et autorisation READ_STEPS dans Santé Connect, l’application affiche les pas du jour, les moyennes et tendances sur 7/30 jours et l’avancement des objectifs personnels de marche. Ce suivi sert à comprendre son activité physique et à progresser vers des objectifs librement choisis. Seule la lecture des pas est demandée. Les totaux et objectifs restent sur l’appareil ; l’historique couvre le jour courant et les 30 jours précédents lors de l’utilisation de l’application ; les anciennes données locales sont purgées à la prochaine ouverture. Les pas ne sont ni envoyés à un serveur ni partagés avec des annonceurs. L’utilisateur peut refuser sans empêcher les quiz, désactiver l’activité pour effacer son historique local, et révoquer l’accès dans les paramètres Android.

## Evidence for Play reviewer
1. Open the app → Profile → My Habits / Mes habitudes.
2. Find the Activity / Activité card with the explanation of step use, optional access and deletion.
3. Tap Enable / Activer → authorize **steps only** in Health Connect.
4. Return to My Habits → see today's step total and activity history, patterns and optional step goals when data is present.
5. Follow **Health Connect data & privacy policy** in My Habits or the Privacy policy link in the Health Connect permissions interface. Both lead to the same published policy.
6. Disable Activity to erase app-owned step history. Revoke permission separately in Android Settings.

## Additional Play Console checks before resubmission
- Publish and verify GitHub Pages privacy URL, including the explicit Health Connect section. The website must be publicly accessible with no login.
- Recheck Data safety for production AdMob, UMP, Billing and any enabled custom telemetry. Local step data alone does not justify marking all data practices as "no data collected".
- Keep the `READ_STEPS` declaration and Play descriptions aligned with the **actual signed AAB** and real feature screens.
- Check highest accepted `versionCode` in the existing app; increment only if needed, then build/sign with the **existing** Play upload keystore. No upload or publication is performed by this documentation change.
- If Google rejects the fitness/wellness rationale after the feature and disclosures have been made clear, investigate their specific reason before removing the permission; do not silently degrade the working Habits feature.
